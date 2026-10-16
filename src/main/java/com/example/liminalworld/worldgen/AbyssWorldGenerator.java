package com.example.liminalworld.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.EnumSet;

public class AbyssWorldGenerator {

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
    private static final BlockState GRASS_BLOCK = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState MOSS_BLOCK = Blocks.MOSS_BLOCK.defaultBlockState();
    private static final BlockState PODZOL = Blocks.PODZOL.defaultBlockState();
    private static final BlockState CALCITE = Blocks.CALCITE.defaultBlockState();
    private static final BlockState TUFF = Blocks.TUFF.defaultBlockState();
    private static final BlockState DRIPSTONE = Blocks.DRIPSTONE_BLOCK.defaultBlockState();
    private static final BlockState PRISMARINE = Blocks.PRISMARINE.defaultBlockState();
    private static final BlockState DARK_PRISMARINE = Blocks.DARK_PRISMARINE.defaultBlockState();
    private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    private static final BlockState SMOOTH_QUARTZ = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState GILDED_BLACKSTONE = Blocks.GILDED_BLACKSTONE.defaultBlockState();
    private static final BlockState BLACKSTONE = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState SCULK = Blocks.SCULK.defaultBlockState();
    private static final BlockState CRYING_OBSIDIAN = Blocks.CRYING_OBSIDIAN.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SEA_LANTERN = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState SHROOMLIGHT = Blocks.SHROOMLIGHT.defaultBlockState();
    private static final BlockState OCHRE_FROGLIGHT = Blocks.OCHRE_FROGLIGHT.defaultBlockState();
    private static final BlockState GLOWSTONE = Blocks.GLOWSTONE.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();

    /**
     * Вычисляет точную высоту поверхности (Y) выбранного Слоя Бездны (1..7) в мировых координатах (wx, wz).
     * Используется и генератором рельефа, и генератором 10 случайных структур каждого слоя!
     */
    public static int getExactLayerFloorY(int layer, int wx, int wz) {
        int layerFloorBase = AbyssLayerManager.getLayerFloorY(layer);
        int terrainWave = (int) (Math.sin(wx * 0.04) * 12.0 + Math.cos(wz * 0.04) * 12.0);
        int detailWave = (int) (Math.sin(wx * 0.11 + wz * 0.09) * 4.0);
        return layerFloorBase + 28 + terrainWave + detailWave;
    }

    /**
     * Проверяет, находится ли точка (wx, wz) на данном Слое Бездны внутри сквозного кратера/разлома.
     */
    public static boolean isLayerPlungeHoleAt(int layer, int wx, int wz) {
        double distFromOrigin = Math.sqrt((double) wx * wx + (double) wz * wz);
        if (distFromOrigin < 28.0) return true;

        double chasmWave = Math.sin(wx * 0.018) * Math.cos(wz * 0.018)
                + 0.5 * Math.sin((wx + wz) * 0.035);
        if (chasmWave > 0.88) return true;

        double layerHoleWave = Math.sin(wx * 0.025 + layer * 1.7) * Math.cos(wz * 0.025 - layer * 1.3);
        return layerHoleWave > 0.72;
    }

    public static void buildAbyssChunk(
            RegistryAccess registryAccess,
            RandomState randomState,
            ChunkAccess chunk,
            long seed
    ) {
        // 1. Применяем систему 10 случайных биомов на каждый из 7 слоев Бездны (ванильные + модовые!)
        AbyssLayerManager.applyLayerBiomesToChunk(registryAccess, seed, chunk, randomState);

        FastChunkWriter writer = new FastChunkWriter(chunk);
        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();

        // 2. Очищаем стандартный ванильный ландшафт
        writer.clearVanillaTerrain();

        // 3. Генерируем необычный многослойный рельеф всех 7 Слоев Бездны (от +1916 до -1968)
        for (int lx = 0; lx < 16; lx++) {
            int wx = chunkMinX + lx;
            for (int lz = 0; lz < 16; lz++) {
                int wz = chunkMinZ + lz;

                double distFromOrigin = Math.sqrt((double) wx * wx + (double) wz * wz);
                boolean inCentralShaft = (distFromOrigin < 28.0);
                int detailWave = (int) (Math.sin(wx * 0.11 + wz * 0.09) * 4.0);

                for (int layer = 1; layer <= AbyssLayerManager.TOTAL_LAYERS; layer++) {
                    int layerFloorBase = AbyssLayerManager.getLayerFloorY(layer);
                    int layerCeilingBase = AbyssLayerManager.getLayerCeilingY(layer);

                    Holder<Biome> biome = chunk.getNoiseBiome(lx >> 2, (layerFloorBase + 16) >> 2, lz >> 2);
                    BlockState topSurface = getSurfaceBlockForLayerAndBiome(layer, biome, wx, wz);
                    BlockState subSurface = getSubSurfaceBlockForLayer(layer);
                    BlockState coreRock = (layerFloorBase < 0) ? DEEPSLATE : STONE;

                    int floorSurfaceY = getExactLayerFloorY(layer, wx, wz);
                    int underSlabBottomY = layerFloorBase - 14 + detailWave;

                    double layerHoleWave = Math.sin(wx * 0.025 + layer * 1.7) * Math.cos(wz * 0.025 - layer * 1.3);
                    boolean isLayerPlungeHole = isLayerPlungeHoleAt(layer, wx, wz);

                    if (!isLayerPlungeHole) {
                        for (int y = underSlabBottomY; y <= floorSurfaceY; y++) {
                            if (y == floorSurfaceY) {
                                writer.set(lx, y, lz, topSurface);
                            } else if (y >= floorSurfaceY - 3) {
                                writer.set(lx, y, lz, subSurface);
                            } else if (y == underSlabBottomY && ((wx * 7 + wz * 13 + layer) % 29 == 0)) {
                                writer.set(lx, y, lz, getCeilingLightForLayer(layer));
                            } else {
                                writer.set(lx, y, lz, coreRock);
                            }
                        }

                        if (layer == 5 && floorSurfaceY < -740) {
                            for (int wy = floorSurfaceY + 1; wy <= -740; wy++) {
                                writer.set(lx, wy, lz, (wy == -740 && ((wx + wz) % 11 == 0)) ? PACKED_ICE : WATER);
                            }
                        }

                        if (layer == 4 && floorSurfaceY < -218) {
                            for (int wy = floorSurfaceY + 1; wy <= -218; wy++) {
                                writer.set(lx, wy, lz, WATER);
                            }
                        }
                    } else if (!inCentralShaft && Math.abs(layerHoleWave - 0.72) < 0.04) {
                        if (((wx + wz) & 7) == 0) {
                            for (int wy = layerFloorBase - 20; wy <= floorSurfaceY; wy++) {
                                writer.set(lx, wy, lz, WATER);
                            }
                        }
                    }

                    buildLayerSpecificGeoFeatures(writer, layer, lx, lz, wx, wz, floorSurfaceY, layerCeilingBase, coreRock);
                }
            }
        }

        // 4. Генерируем ОГРОМНЫЕ ДЕРЕВЬЯ БЕЗДНЫ (высотой от 200 до 1000 блоков!)
        ColossalTreeGenerator.generateColossalTreesInChunk(writer, chunkX, chunkZ, seed);

        // 5. Генерируем авторские структуры Made in Abyss (Лагеря Искателей, Кубки Гигантов, Идофронт, Золотой Город)
        AbyssStructureBuilder.generateLayerStructures(writer, chunk, registryAccess, chunkX, chunkZ, seed);

        // 6. Если это центральный чанк (0, 0) — возводим на вершине мира Бедроковую Комнату со сквозной дырой вниз!
        if (chunkX == 0 && chunkZ == 0) {
            BedrockSpawnVaultBuilder.buildBedrockSpawnRoom(writer, chunk, seed);
        }

        // 7. Обновляем карты высот для корректного спавна структур и мобов из модов
        Heightmap.primeHeightmaps(chunk, EnumSet.of(
                Heightmap.Types.WORLD_SURFACE_WG,
                Heightmap.Types.OCEAN_FLOOR_WG,
                Heightmap.Types.MOTION_BLOCKING,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
        ));
    }

    private static void buildLayerSpecificGeoFeatures(
            FastChunkWriter writer, int layer,
            int lx, int lz, int wx, int wz,
            int floorY, int ceilingY, BlockState coreRock
    ) {
        if (layer == 3) {
            for (int bridgeY = 400; bridgeY <= 760; bridgeY += 120) {
                double bridgeBand = Math.sin(wx * 0.03 + bridgeY) + Math.cos(wz * 0.03 - bridgeY);
                if (Math.abs(bridgeBand) < 0.16) {
                    writer.set(lx, bridgeY, lz, coreRock);
                    writer.set(lx, bridgeY + 1, lz, MOSS_BLOCK);
                }
            }
        } else if (layer == 6) {
            if (((wx * 19 + wz * 31) & 255) == 0) {
                for (int y = floorY + 1; y <= floorY + 45; y++) {
                    writer.set(lx, y, lz, (y % 6 == 0) ? OCHRE_FROGLIGHT : GILDED_BLACKSTONE);
                }
            }
        } else if (layer == 7) {
            for (int y = -1850; y <= -1480; y += 90) {
                double spiral = Math.sin(wx * 0.04 + y * 0.05) + Math.cos(wz * 0.04 + y * 0.05);
                if (spiral > 1.55) {
                    writer.set(lx, y, lz, CRYING_OBSIDIAN);
                    writer.set(lx, y + 1, lz, SCULK);
                }
            }
        }
    }

    private static BlockState getSurfaceBlockForLayerAndBiome(int layer, Holder<Biome> biome, int wx, int wz) {
        if (biome.is(BiomeTags.IS_BADLANDS) || biome.is(BiomeTags.HAS_DESERT_PYRAMID)) {
            if (((wx + wz) & 3) == 0) return SAND;
        }
        if (biome.value().coldEnoughToSnow(new net.minecraft.core.BlockPos(wx, 100, wz))) {
            if (((wx + wz) & 3) == 0) return SNOW_BLOCK;
        }

        return switch (layer) {
            case 1 -> ((wx + wz) % 5 == 0) ? MOSS_BLOCK : GRASS_BLOCK;
            case 2 -> ((wx + wz) % 3 == 0) ? PODZOL : MOSS_BLOCK;
            case 3 -> ((wx + wz) % 4 == 0) ? DRIPSTONE : TUFF;
            case 4 -> ((wx + wz) % 3 == 0) ? CALCITE : MOSS_BLOCK;
            case 5 -> ((wx + wz) % 3 == 0) ? PACKED_ICE : DARK_PRISMARINE;
            case 6 -> ((wx + wz) % 5 == 0) ? GILDED_BLACKSTONE : SMOOTH_QUARTZ;
            default -> ((wx + wz) % 4 == 0) ? CRYING_OBSIDIAN : SCULK;
        };
    }

    private static BlockState getSubSurfaceBlockForLayer(int layer) {
        return switch (layer) {
            case 1, 2 -> DIRT;
            case 3 -> TUFF;
            case 4 -> CALCITE;
            case 5 -> PRISMARINE;
            case 6 -> BLACKSTONE;
            default -> DEEPSLATE;
        };
    }

    private static BlockState getCeilingLightForLayer(int layer) {
        return switch (layer) {
            case 1, 2 -> OCHRE_FROGLIGHT;
            case 3, 4 -> SHROOMLIGHT;
            case 5, 6 -> SEA_LANTERN;
            default -> GLOWSTONE;
        };
    }

    /**
     * Возвращает точную высоту поверхности для спавна структур:
     * - Если активен генератор структур конкретного слоя (ACTIVE_GENERATION_LAYER), возвращает точный пол этого слоя.
     * - Иначе распределяет структуры по всем 7 Слоям Бездны.
     */
    public static int getDistributedAbyssStructureY(int blockX, int blockZ) {
        Integer activeLayer = AbyssLayerStructurePlacer.ACTIVE_GENERATION_LAYER.get();
        if (activeLayer != null) {
            return getExactLayerFloorY(activeLayer, blockX, blockZ) + 1;
        }
        long h = AbyssLayerManager.hashCoords(123456789L, blockX >> 6, 0, blockZ >> 6);
        int layer = 1 + (int) (Math.abs(h) % AbyssLayerManager.TOTAL_LAYERS);
        return getExactLayerFloorY(layer, blockX, blockZ) + 1;
    }
}
