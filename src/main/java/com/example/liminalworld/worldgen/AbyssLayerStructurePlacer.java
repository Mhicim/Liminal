package com.example.liminalworld.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AbyssLayerStructurePlacer {

    /**
     * Позволяет getBaseHeight(...) в NoiseBasedChunkGeneratorMixin точно знать,
     * для какого именно Слоя Бездны (1..7) сейчас генерируется или размещается структура!
     */
    public static final ThreadLocal<Integer> ACTIVE_GENERATION_LAYER = new ThreadLocal<>();

    // Кэш сгенерированных StructureStart по ключу (layer, anchorChunkX, anchorChunkZ),
    // чтобы многочанковые структуры (деревни, древние города, данжи из модов) быстро и бесшовно собирались по чанкам
    private static final Map<Long, StructureStart> STRUCTURE_START_CACHE = new ConcurrentHashMap<>();
    private static volatile long cachedWorldSeed = Long.MIN_VALUE;

    // Шаг сетки спавна структур на каждом слое (1 структура на квадрат 5x5 чанков = 80x80 блоков на каждом из 7 слоев!)
    private static final int GRID_SPACING = 5;

    public static void placeLayerStructuresInChunk(
            WorldGenLevel level,
            ChunkAccess chunk,
            StructureManager structureManager,
            ChunkGenerator generator
    ) {
        ServerLevel serverLevel = level.getLevel();
        if (serverLevel == null || serverLevel.dimension() != net.minecraft.world.level.Level.OVERWORLD) {
            return;
        }

        long seed = level.getSeed();
        if (cachedWorldSeed != seed) {
            STRUCTURE_START_CACHE.clear();
            cachedWorldSeed = seed;
        }
        if (STRUCTURE_START_CACHE.size() > 512) {
            STRUCTURE_START_CACHE.clear();
        }

        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;

        // Защищаем центральную шахту Бедроковой комнаты (0, 0)
        if (Math.abs(chunkX) <= 1 && Math.abs(chunkZ) <= 1) {
            return;
        }

        RegistryAccess registryAccess = level.registryAccess();
        AbyssLayerManager.ensureInitialized(registryAccess, seed);

        StructureTemplateManager templateManager = serverLevel.getStructureManager();
        RandomState randomState = serverLevel.getChunkSource().randomState();

        BoundingBox chunkBox = new BoundingBox(
                chunkPos.getMinBlockX() + 1,
                level.getMinBuildHeight() + 8,
                chunkPos.getMinBlockZ() + 1,
                chunkPos.getMaxBlockX() - 1,
                level.getMaxBuildHeight() - 8,
                chunkPos.getMaxBlockZ() - 1
        );

        int baseGridX = Math.floorDiv(chunkX, GRID_SPACING);
        int baseGridZ = Math.floorDiv(chunkZ, GRID_SPACING);

        // Проходим по всем 7 Слоям Бездны, чтобы на каждом слое спавнились его 10 случайных структур!
        for (int layer = 1; layer <= AbyssLayerManager.TOTAL_LAYERS; layer++) {
            List<Holder<Structure>> layerStructures = AbyssLayerManager.getStructuresForLayer(registryAccess, seed, layer);
            if (layerStructures == null || layerStructures.isEmpty()) continue;

            // Проверяем текущую ячейку сетки и соседние (для крупных структур радиусом до 2 чанков)
            for (int gx = baseGridX - 1; gx <= baseGridX + 1; gx++) {
                for (int gz = baseGridZ - 1; gz <= baseGridZ + 1; gz++) {
                    long cellHash = AbyssLayerManager.hashCoords(seed + (layer * 77777L), gx, layer, gz);

                    // 65% ячеек сетки на каждом слое содержат структуру из пула 10 структур этого слоя
                    if (Math.abs(cellHash % 100) >= 65) continue;

                    int anchorChunkX = (gx * GRID_SPACING) + 1 + (int) (Math.abs(cellHash >> 8) % (GRID_SPACING - 2));
                    int anchorChunkZ = (gz * GRID_SPACING) + 1 + (int) (Math.abs(cellHash >> 16) % (GRID_SPACING - 2));

                    if (Math.abs(chunkX - anchorChunkX) > 2 || Math.abs(chunkZ - anchorChunkZ) > 2) {
                        continue;
                    }
                    if (Math.abs(anchorChunkX) <= 2 && Math.abs(anchorChunkZ) <= 2) {
                        continue;
                    }

                    int anchorBlockX = (anchorChunkX << 4) + 8;
                    int anchorBlockZ = (anchorChunkZ << 4) + 8;

                    // Проверяем, что точка спавна структуры на этом слое не попадает в сквозной провал/кратер
                    if (AbyssWorldGenerator.isLayerPlungeHoleAt(layer, anchorBlockX, anchorBlockZ)) {
                        continue;
                    }

                    try {
                        ACTIVE_GENERATION_LAYER.set(layer);

                        StructureStart start = getOrCreateLayerStructureStart(
                                registryAccess, generator, randomState, templateManager,
                                level, layerStructures, seed, layer,
                                anchorChunkX, anchorChunkZ, anchorBlockX, anchorBlockZ, cellHash
                        );

                        if (start != null && start.isValid()) {
                            BoundingBox startBox = start.getBoundingBox();
                            if (startBox.intersects(chunkBox)) {
                                // Строим каменную/мшистую опорную подушку под структурой, если край висит над склоном
                                buildSupportFoundationIfNeeded(chunk, startBox, chunkPos, layer);

                                RandomSource randomSource = RandomSource.create(
                                        seed ^ (chunkX * 341873128712L) ^ (chunkZ * 132897987541L) ^ (layer * 4231L)
                                );
                                start.placeInChunk(level, structureManager, generator, randomSource, chunkBox, chunkPos);
                            }
                        }
                    } catch (Throwable ignored) {
                        // Защита от нестандартных модовых структур, чтобы генерация чанка никогда не прерывалась
                    } finally {
                        ACTIVE_GENERATION_LAYER.remove();
                    }
                }
            }
        }
    }

    private static StructureStart getOrCreateLayerStructureStart(
            RegistryAccess registryAccess,
            ChunkGenerator generator,
            RandomState randomState,
            StructureTemplateManager templateManager,
            WorldGenLevel level,
            List<Holder<Structure>> layerStructures,
            long seed,
            int layer,
            int anchorChunkX,
            int anchorChunkZ,
            int anchorBlockX,
            int anchorBlockZ,
            long cellHash
    ) {
        long cacheKey = (((long) layer & 0xFL) << 56)
                | (((long) anchorChunkX & 0xFFFFFFFL) << 28)
                | ((long) anchorChunkZ & 0xFFFFFFFL);

        StructureStart cached = STRUCTURE_START_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        ChunkPos anchorPos = new ChunkPos(anchorChunkX, anchorChunkZ);
        int targetFloorY = AbyssWorldGenerator.getExactLayerFloorY(layer, anchorBlockX, anchorBlockZ);
        int layerCeilingY = AbyssLayerManager.getLayerCeilingY(layer);

        // Пробуем до 3 структур из 10 назначенных этому слою (на случай, если конкретная структура отклонит координаты)
        int totalCandidates = layerStructures.size();
        for (int attempt = 0; attempt < Math.min(3, totalCandidates); attempt++) {
            int structIdx = (int) ((Math.abs(cellHash >> 24) + attempt) % totalCandidates);
            Holder<Structure> structureHolder = layerStructures.get(structIdx);
            Structure structure = structureHolder.value();

            try {
                // Передаем (biome -> true), чтобы любая из 10 структур слоя генерировалась независимо от биомных ограничений!
                StructureStart start = structure.generate(
                        registryAccess,
                        generator,
                        generator.getBiomeSource(),
                        randomState,
                        templateManager,
                        seed + (layer * 1009L) + attempt,
                        anchorPos,
                        0,
                        level,
                        biome -> true
                );

                if (start != null && start.isValid()) {
                    List<StructurePiece> pieces = start.getPieces();
                    if (!pieces.isEmpty()) {
                        int minPieceY = Integer.MAX_VALUE;
                        int maxPieceY = Integer.MIN_VALUE;
                        for (StructurePiece piece : pieces) {
                            BoundingBox pb = piece.getBoundingBox();
                            minPieceY = Math.min(minPieceY, pb.minY());
                            maxPieceY = Math.max(maxPieceY, pb.maxY());
                        }

                        // Если структура использовала фиксированную высоту (например, Древний город на -51 или Бастион на 48),
                        // переносим все её части ровно на поверхность текущего Слоя Бездны ДО вычисления getBoundingBox()!
                        if (minPieceY < targetFloorY - 20 || minPieceY > targetFloorY + 45 || maxPieceY >= layerCeilingY - 10) {
                            int dy = targetFloorY - minPieceY;
                            for (StructurePiece piece : pieces) {
                                piece.move(0, dy, 0);
                            }
                        }

                        STRUCTURE_START_CACHE.put(cacheKey, start);
                        return start;
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private static void buildSupportFoundationIfNeeded(ChunkAccess chunk, BoundingBox startBox, ChunkPos chunkPos, int layer) {
        int minX = Math.max(chunkPos.getMinBlockX() + 1, startBox.minX());
        int maxX = Math.min(chunkPos.getMaxBlockX() - 1, startBox.maxX());
        int minZ = Math.max(chunkPos.getMinBlockZ() + 1, startBox.minZ());
        int maxZ = Math.min(chunkPos.getMaxBlockZ() - 1, startBox.maxZ());
        int baseSupportY = startBox.minY() - 1;

        if (baseSupportY <= chunk.getMinBuildHeight() + 10 || baseSupportY >= chunk.getMaxBuildHeight() - 10) {
            return;
        }

        BlockState supportBlock = (layer <= 2) ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                : (layer <= 4) ? Blocks.CALCITE.defaultBlockState()
                : Blocks.DEEPSLATE_BRICKS.defaultBlockState();

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int wx = minX; wx <= maxX; wx += 2) {
            for (int wz = minZ; wz <= maxZ; wz += 2) {
                mpos.set(wx, baseSupportY, wz);
                if (chunk.getBlockState(mpos).isAir()) {
                    for (int dy = 0; dy <= 6; dy++) {
                        mpos.set(wx, baseSupportY - dy, wz);
                        if (!chunk.getBlockState(mpos).isAir()) break;
                        chunk.setBlockState(mpos, supportBlock, false);
                    }
                }
            }
        }
    }
}
