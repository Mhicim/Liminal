package com.example.liminalworld.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.EnumSet;

public class MegastructureGenerator {

    // Параметры этажей мегаструктуры
    public static final int MIN_Y = -56;
    public static final int MAX_Y = 120; // Верхняя палуба на высоте 120
    public static final int FLOOR_HEIGHT = 8; // 5 блоков комната + 3 блока тех. перекрытие для руд

    public enum Theme {
        BACKROOMS_LEVEL_0, // Желтые обои, влажный ковер, гудящие лампы
        POOLROOMS,         // Белая плитка, теплая вода на полу, морские фонари
        OVERGROWN_ATRIUM,  // Заросший бетон, земля/трава для модовых растений и деревьев
        BRUTALIST_DEEP     // Темный базальт и бетон в стиле манги Blame! (ниже Y = 0)
    }

    public enum RoomType {
        OPEN_HALL,         // Открытый зал с редкими колоннами
        CORRIDOR_CROSS,    // Узкие коридоры с проемами
        STAIRWELL,         // Лестничный пролет на этаж выше
        BIO_ATRIUM,        // Высокий атриум (без потолка) для спавна деревьев и структур
        SOLID_CORE         // Массив камня/сланца для обильной генерации модовых руд
    }

    public static void buildChunk(ChunkAccess chunk, long seed) {
        ChunkPos chunkPos = chunk.getPos();
        int startX = chunkPos.getMinBlockX();
        int startZ = chunkPos.getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // 1. Очищаем чанк выше крыши мегаструктуры (убираем ванильные горы и океаны выше Y=120)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = MAX_Y + 1; y < chunk.getMaxBuildHeight(); y++) {
                    pos.set(startX + x, y, startZ + z);
                    chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                }
            }
        }

        // 2. Генерируем этажи снизу вверх по сетке ячеек 8x8
        int totalFloors = (MAX_Y - MIN_Y) / FLOOR_HEIGHT;

        for (int floor = 0; floor < totalFloors; floor++) {
            int baseY = MIN_Y + (floor * FLOOR_HEIGHT);

            // Чанк 16x16 содержит 2x2 ячейки размером 8x8
            for (int cellX = 0; cellX < 2; cellX++) {
                for (int cellZ = 0; cellZ < 2; cellZ++) {
                    int worldCellX = (chunkPos.x * 2) + cellX;
                    int worldCellZ = (chunkPos.z * 2) + cellZ;

                    Holder<Biome> biome = chunk.getNoiseBiome(
                            (cellX * 8 + 4) >> 2,
                            (baseY + 2) >> 2,
                            (cellZ * 8 + 4) >> 2
                    );

                    Theme theme = selectTheme(biome, baseY);
                    RoomType roomType = selectRoomType(seed, worldCellX, floor, worldCellZ, theme);

                    generateCell(chunk, pos, cellX * 8, baseY, cellZ * 8, worldCellX, floor, worldCellZ, seed, theme, roomType);
                }
            }
        }

        // 3. Оформляем верхнюю крышу (Y = MAX_Y), чтобы наземные модовые структуры и деревни могли спавниться
        buildRoofDeck(chunk, pos, startX, startZ);

        // 4. Пересчитываем карты высот для корректной работы структур и мобов из других модов
        Heightmap.primeHeightmaps(chunk, EnumSet.of(
                Heightmap.Types.WORLD_SURFACE_WG,
                Heightmap.Types.OCEAN_FLOOR_WG,
                Heightmap.Types.MOTION_BLOCKING,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
        ));
    }

    private static Theme selectTheme(Holder<Biome> biome, int y) {
        if (y < 0) {
            return Theme.BRUTALIST_DEEP;
        }
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER) || biome.is(BiomeTags.HAS_SWAMP_HUT)) {
            return Theme.POOLROOMS;
        }
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_JUNGLE)) {
            return Theme.OVERGROWN_ATRIUM;
        }
        return Theme.BACKROOMS_LEVEL_0;
    }

    private static RoomType selectRoomType(long seed, int cellX, int floor, int cellZ, Theme theme) {
        long hash = hashCoords(seed, cellX, floor, cellZ);
        int roll = (int) (Math.abs(hash) % 100);

        if (roll < 8) return RoomType.SOLID_CORE;    // 8% — технический массив камня для руд
        if (roll < 16) return RoomType.STAIRWELL;    // 8% — лестница наверх
        if (theme == Theme.OVERGROWN_ATRIUM && roll < 45) {
            return RoomType.BIO_ATRIUM;              // В лесных биомах больше высоких био-куполов
        }
        if (roll < 55) return RoomType.CORRIDOR_CROSS;
        return RoomType.OPEN_HALL;
    }

    private static void generateCell(
            ChunkAccess chunk, BlockPos.MutableBlockPos pos,
            int localStartX, int baseY, int localStartZ,
            int worldCellX, int floor, int worldCellZ,
            long seed, Theme theme, RoomType roomType
    ) {
        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();

        // Блок технического перекрытия (в нем генерируются модовые руды на этапе UNDERGROUND_ORES)
        BlockState structuralCore = (baseY < 0)
                ? Blocks.DEEPSLATE.defaultBlockState()
                : Blocks.STONE.defaultBlockState();

        BlockState floorBlock = getFloorBlock(theme);
        BlockState wallBlock = getWallBlock(theme);
        BlockState ceilingBlock = getCeilingBlock(theme);
        BlockState lightBlock = getLightBlock(theme);

        // Если это глухой технический отсек — заполняем камнем/сланцем для руд и пещерных модов
        if (roomType == RoomType.SOLID_CORE) {
            for (int dx = 0; dx < 8; dx++) {
                for (int dz = 0; dz < 8; dz++) {
                    for (int dy = 0; dy < FLOOR_HEIGHT; dy++) {
                        pos.set(chunkMinX + localStartX + dx, baseY + dy, chunkMinZ + localStartZ + dz);
                        chunk.setBlockState(pos, structuralCore, false);
                    }
                }
            }
            return;
        }

        boolean hasNorthWall = (hashCoords(seed + 1, worldCellX, floor, worldCellZ) & 3) == 0;
        boolean hasWestWall = (hashCoords(seed + 2, worldCellX, floor, worldCellZ) & 3) == 0;

        for (int dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 8; dz++) {
                for (int dy = 0; dy < FLOOR_HEIGHT; dy++) {
                    pos.set(chunkMinX + localStartX + dx, baseY + dy, chunkMinZ + localStartZ + dz);

                    // Слой 0 и 1: Техническая плита под полом (2 блока камня/сланца для руд)
                    if (dy == 0 || dy == 1) {
                        chunk.setBlockState(pos, structuralCore, false);
                        continue;
                    }

                    // Слой 2: Чистовой пол комнаты
                    if (dy == 2) {
                        chunk.setBlockState(pos, floorBlock, false);
                        continue;
                    }

                    // Слой 7 (верхний): Потолок со светильниками (пропускаем в атриумах и лестницах)
                    if (dy == FLOOR_HEIGHT - 1) {
                        if (roomType == RoomType.BIO_ATRIUM) {
                            chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                        } else if (roomType == RoomType.STAIRWELL && dx >= 2 && dx <= 5 && dz >= 2 && dz <= 5) {
                            chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                        } else if ((dx == 3 || dx == 4) && (dz == 3 || dz == 4)) {
                            chunk.setBlockState(pos, lightBlock, false);
                        } else {
                            chunk.setBlockState(pos, ceilingBlock, false);
                        }
                        continue;
                    }

                    // Слои 3..6: Внутреннее пространство комнат, стен и колонн
                    boolean isWall = false;

                    if (roomType == RoomType.CORRIDOR_CROSS) {
                        // Крестообразный коридор: углы ячейки заполнены камнем, обшитым стенами
                        if ((dx < 2 || dx > 5) && (dz < 2 || dz > 5)) {
                            boolean innerCore = (dx == 0 || dx == 7) && (dz == 0 || dz == 7);
                            chunk.setBlockState(pos, innerCore ? structuralCore : wallBlock, false);
                            continue;
                        }
                    } else if (roomType == RoomType.OPEN_HALL) {
                        // Несущая колонна в углу и случайные перегородки с дверными проемами
                        if (dx == 0 && dz == 0) {
                            isWall = true;
                        } else if (hasNorthWall && dz == 0 && (dx < 3 || dx > 4 || dy > 4)) {
                            isWall = true; // Стена с проемом 2x2
                        } else if (hasWestWall && dx == 0 && (dz < 3 || dz > 4 || dy > 4)) {
                            isWall = true;
                        }
                    }

                    if (isWall) {
                        chunk.setBlockState(pos, wallBlock, false);
                    } else if (roomType == RoomType.STAIRWELL && isStairStep(dx, dy, dz)) {
                        chunk.setBlockState(pos, wallBlock, false);
                    } else if (theme == Theme.POOLROOMS && dy == 3 && roomType != RoomType.STAIRWELL) {
                        // В Poolrooms на полу стоит вода по щиколотку
                        chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
                    } else {
                        chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
                    }
                }
            }
        }

        // Проверяем, является ли секция тупиком-складом
        boolean isDeadEnd = (roomType == RoomType.CORRIDOR_CROSS)
                && ((hashCoords(seed + 7, worldCellX, floor, worldCellZ) & 7) == 0);

        if (isDeadEnd) {
            for (int d = 2; d <= 5; d++) {
                for (int dy = 3; dy < FLOOR_HEIGHT - 1; dy++) {
                    pos.set(chunkMinX + localStartX + d, baseY + dy, chunkMinZ + localStartZ);
                    chunk.setBlockState(pos, wallBlock, false);
                }
            }
        }

        // Спавним паллету с припасами из LootTables (с поддержкой других модов)
        if (roomType != RoomType.SOLID_CORE && roomType != RoomType.STAIRWELL) {
            LiminalLootPlacer.tryPlaceRoomSupplies(
                    chunk,
                    localStartX, baseY, localStartZ,
                    worldCellX, floor, worldCellZ,
                    seed,
                    isDeadEnd,
                    theme.name()
            );
        }
    }

    private static boolean isStairStep(int dx, int dy, int dz) {
        if (dz == 3 || dz == 4) {
            int expectedDx = dy - 1;
            return dx >= expectedDx && dx <= 5;
        }
        return false;
    }

    private static void buildRoofDeck(ChunkAccess chunk, BlockPos.MutableBlockPos pos, int startX, int startZ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Holder<Biome> biome = chunk.getNoiseBiome(x >> 2, MAX_Y >> 2, z >> 2);
                boolean isGreen = biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_JUNGLE);

                pos.set(startX + x, MAX_Y - 1, startZ + z);
                chunk.setBlockState(pos, Blocks.STONE.defaultBlockState(), false);

                pos.set(startX + x, MAX_Y, startZ + z);
                chunk.setBlockState(pos, isGreen
                        ? Blocks.GRASS_BLOCK.defaultBlockState()
                        : Blocks.SMOOTH_STONE.defaultBlockState(), false);
            }
        }
    }

    private static BlockState getFloorBlock(Theme theme) {
        return switch (theme) {
            case BACKROOMS_LEVEL_0 -> Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            case POOLROOMS -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case OVERGROWN_ATRIUM -> Blocks.GRASS_BLOCK.defaultBlockState();
            case BRUTALIST_DEEP -> Blocks.POLISHED_BASALT.defaultBlockState();
        };
    }

    private static BlockState getWallBlock(Theme theme) {
        return switch (theme) {
            case BACKROOMS_LEVEL_0 -> Blocks.STRIPPED_BIRCH_WOOD.defaultBlockState();
            case POOLROOMS -> Blocks.QUARTZ_BRICKS.defaultBlockState();
            case OVERGROWN_ATRIUM -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            case BRUTALIST_DEEP -> Blocks.GRAY_CONCRETE.defaultBlockState();
        };
    }

    private static BlockState getCeilingBlock(Theme theme) {
        return switch (theme) {
            case BACKROOMS_LEVEL_0 -> Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            case POOLROOMS -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case OVERGROWN_ATRIUM -> Blocks.STONE_BRICKS.defaultBlockState();
            case BRUTALIST_DEEP -> Blocks.BLACKSTONE.defaultBlockState();
        };
    }

    private static BlockState getLightBlock(Theme theme) {
        return switch (theme) {
            case BACKROOMS_LEVEL_0 -> Blocks.OCHRE_FROGLIGHT.defaultBlockState();
            case POOLROOMS -> Blocks.SEA_LANTERN.defaultBlockState();
            case OVERGROWN_ATRIUM -> Blocks.SHROOMLIGHT.defaultBlockState();
            case BRUTALIST_DEEP -> Blocks.SEA_LANTERN.defaultBlockState();
        };
    }

    private static long hashCoords(long seed, int x, int y, int z) {
        long h = seed ^ (x * 374761393L) ^ (y * 668265263L) ^ (z * 2147483647L);
        h = (h ^ (h >> 13)) * 1274126177L;
        return h ^ (h >> 16);
    }
}
