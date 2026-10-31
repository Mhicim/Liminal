package com.megastrata.world;

import com.megastrata.content.ModRegistry;
import com.megastrata.world.builder.FullMegastructureBuilder;
import com.megastrata.world.builder.MicroAndVillageGenerator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MegastrataChunkGenerator extends ChunkGenerator {
    public static final Codec<MegastrataChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource)
            ).apply(instance, MegastrataChunkGenerator::new)
    );

    public MegastrataChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(() -> {
            generateMegastructureGeometry(chunk, 1337420L);
            return chunk;
        }, executor);
    }

    private void generateMegastructureGeometry(ChunkAccess chunk, long seed) {
        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();
        int minY = chunk.getMinBuildHeight(); // -2000
        int maxY = chunk.getMaxBuildHeight(); // +2000

        // Проверяем, находится ли чанк в огромной переходной Снежной Равнине между массивами Гигахруща
        if (MegastructurePlanner.isSnowyPlainTransitionZone(startX, startZ)) {
            FullMegastructureBuilder.buildTransitionalSnowyPlainBetweenBuildings(chunk, startX, startZ, seed);
            return;
        }

        BlockState structuralConcrete = ModRegistry.STRUCTURAL_CONCRETE.get().defaultBlockState();
        BlockState darkSteel = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState signalLight = Blocks.SEA_LANTERN.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // 1. Оригинальная генерация из первой версии (колонны диаметром 56 блоков, шахты 320 блоков, перекрытия 400 блоков, мосты каждые 80 блоков)
        for (int localX = 0; localX < 16; localX++) {
            int worldX = startX + localX;
            int gridX = Math.floorMod(worldX, 256);
            int shaftGridX = Math.floorMod(worldX, 512);

            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = startZ + localZ;
                int gridZ = Math.floorMod(worldZ, 256);
                int shaftGridZ = Math.floorMod(worldZ, 512);

                double distToPillarCenter = Math.hypot(gridX - 128, gridZ - 128);
                double distToShaftCenter = Math.hypot(shaftGridX - 256, shaftGridZ - 256);

                boolean isInsidePillar = distToPillarCenter <= 28.0;
                boolean isPillarHollowCore = distToPillarCenter <= 12.0;
                boolean isInsideGiantShaft = distToShaftCenter <= 160.0;
                boolean isBridgeX = Math.abs(gridZ - 128) <= 6;
                boolean isBridgeZ = Math.abs(gridX - 128) <= 6;

                for (int y = minY; y < maxY; y++) {
                    pos.set(localX, y, localZ);
                    LayerProfile profile = MegastructurePlanner.getLayerAt(seed, worldX, y, worldZ);

                    if (isInsideGiantShaft && !isInsidePillar) {
                        if (distToShaftCenter > 156.0 && (y % 8 != 0)) {
                            chunk.setBlockState(pos, darkSteel, false);
                        }
                        continue;
                    }

                    if (isInsidePillar) {
                        if (!isPillarHollowCore) {
                            chunk.setBlockState(pos, structuralConcrete, false);
                        } else if (y % 64 == 0 && (localX == 8 && localZ == 8)) {
                            chunk.setBlockState(pos, ModRegistry.ELEVATOR_TERMINAL.get().defaultBlockState(), false);
                        }
                        continue;
                    }

                    int modY400 = Math.floorMod(y, 400);
                    if (modY400 < 24) {
                        chunk.setBlockState(pos, darkSteel, false);
                        continue;
                    }

                    int modY80 = Math.floorMod(y, 80);
                    if ((isBridgeX || isBridgeZ) && modY80 < 4) {
                        chunk.setBlockState(pos, structuralConcrete, false);
                        if (modY80 == 3 && (Math.abs(gridZ - 128) == 6 || Math.abs(gridX - 128) == 6) && (worldX % 32 == 0 || worldZ % 32 == 0)) {
                            chunk.setBlockState(pos, signalLight, false);
                        }
                        continue;
                    }

                    if (profile.type() == MegastructureType.DEAD_OCEAN && y < profile.baseMinY() + 180) {
                        chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
                    }
                }
            }
        }

        // 2. Все оригинальные структуры первой версии (8 типов мегаструктур, 3-платформенные станции, аномалии, квартиры, лабиринт)
        for (int verticalIdx = 0; verticalIdx < 5; verticalIdx++) {
            int sampleY = -1600 + verticalIdx * 800;
            LayerProfile layerProfile = MegastructurePlanner.getLayerAt(seed, startX, sampleY, startZ);
            FullMegastructureBuilder.buildSpecializedLayerContent(chunk, layerProfile, chunk.getPos().x, chunk.getPos().z);
        }

        // 3. Панельный ГИГАХРУЩ, пронизывающий всю мегаструктуру от -2000 до +2000 (подъезды, мусоропроводы, панельные швы, висячие блоки на сваях)
        MicroAndVillageGenerator.buildPiercingGigakhrushch(chunk, startX, startZ, seed);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {}

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState random, ChunkAccess chunk) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {}

    @Override
    public int getGenDepth() { return 4000; }

    @Override
    public int getSeaLevel() { return -600; }

    @Override
    public int getMinY() { return -2000; }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return 8;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        return new NoiseColumn(height.getMinBuildHeight(), new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        LayerProfile profile = MegastructurePlanner.getLayerAt(1337420L, pos.getX(), pos.getY(), pos.getZ());
        boolean inSnowGap = MegastructurePlanner.isSnowyPlainTransitionZone(pos.getX(), pos.getZ());
        info.add("[MEGASTRATA v3.0] Сектор: " + profile.type().id + " | Гигахрущ: " + (!inSnowGap) + " | Снежная равнина: " + inSnowGap);
    }
}
