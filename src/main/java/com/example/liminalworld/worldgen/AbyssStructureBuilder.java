package com.example.liminalworld.worldgen;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class AbyssStructureBuilder {

    private static final BlockState STONE_BRICKS = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState MOSSY_STONE_BRICKS = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    private static final BlockState DEEPSLATE_TILES = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private static final BlockState POLISHED_BLACKSTONE = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState GILDED_BLACKSTONE = Blocks.GILDED_BLACKSTONE.defaultBlockState();
    private static final BlockState SMOOTH_QUARTZ = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState CALCITE = Blocks.CALCITE.defaultBlockState();
    private static final BlockState MOSS_BLOCK = Blocks.MOSS_BLOCK.defaultBlockState();
    private static final BlockState DARK_OAK_PLANKS = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState DARK_OAK_LOG = Blocks.DARK_OAK_LOG.defaultBlockState();
    private static final BlockState IRON_BLOCK = Blocks.IRON_BLOCK.defaultBlockState();
    private static final BlockState IRON_BARS = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState TINTED_GLASS = Blocks.TINTED_GLASS.defaultBlockState();
    private static final BlockState SEA_LANTERN = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState SHROOMLIGHT = Blocks.SHROOMLIGHT.defaultBlockState();
    private static final BlockState OCHRE_FROGLIGHT = Blocks.OCHRE_FROGLIGHT.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState MAGMA = Blocks.MAGMA_BLOCK.defaultBlockState();
    private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    private static final BlockState BLUE_ICE = Blocks.BLUE_ICE.defaultBlockState();
    private static final BlockState CRYING_OBSIDIAN = Blocks.CRYING_OBSIDIAN.defaultBlockState();
    private static final BlockState SCULK = Blocks.SCULK.defaultBlockState();
    private static final BlockState SOUL_LANTERN = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);

    /**
     * Генерирует уникальные структуры вселенной Made in Abyss на каждом из 7 слоев:
     * - Слой 1: Ветряные мельницы Орта и древние акведуки
     * - Слой 2: Перевернутый Лес (Inverted Forest) и Наблюдательный Лагерь Искателей (Seeker Camp)
     * - Слой 3: Парящие Острова Великого Разлома и гнезда тварей
     * - Слой 4: Кубки Гигантов (Goblets of Giants) — огромные вогнутые чаши с термальной водой
     * - Слой 5: Аванпост Идофронт (Ido Front) — ритуальная база Бондрюда посреди Моря Трупов
     * - Слой 6: Золотые Башни Столицы Невернувшихся и органические купола Ирубуру
     * - Слой 7: Кольцо Средоточия (Pivotal Ring) и Спирали Водоворота
     */
    public static void generateLayerStructures(
            FastChunkWriter writer, ChunkAccess chunk, RegistryAccess registryAccess,
            int chunkX, int chunkZ, long seed
    ) {
        // Не перезаписываем чанк (0, 0), где идет шахта из Бедроковой комнаты
        if (chunkX == 0 && chunkZ == 0) return;

        long chunkHash = AbyssLayerManager.hashCoords(seed + 54321L, chunkX, 0, chunkZ);
        int roll = (int) (Math.abs(chunkHash) % 100);

        // 1. СЛОЙ 1 (+1400..+1920): Руины Орта, подвесные гондолы и каменные мельницы на террасах
        if (roll < 14) {
            buildOrthWindmillRuin(writer, chunk, registryAccess, chunkX, chunkZ, 1440 + (int) (Math.abs(chunkHash >> 4) % 180), seed);
        }

        // 2. СЛОЙ 2 (+850..+1400): Перевернутый Лес под потолком слоя + Лагерь Искателей (Seeker Camp)
        buildInvertedForestCanopy(writer, chunkX, chunkZ, 1385, seed);
        if (roll >= 14 && roll < 26) {
            buildSeekerCampOutpost(writer, chunk, registryAccess, chunkX, chunkZ, 1120 + (int) (Math.abs(chunkHash >> 6) % 140), seed);
        }

        // 3. СЛОЙ 3 (+300..+850): Парящие скальные острова Великого Разлома на гигантских корнях
        if (roll >= 26 && roll < 42) {
            int islandY = 420 + (int) (Math.abs(chunkHash >> 8) % 340);
            buildGreatFaultFloatingIsland(writer, chunk, registryAccess, chunkX, chunkZ, islandY, seed);
        }

        // 4. СЛОЙ 4 (-250..+300): КУБКИ ГИГАНТОВ (Goblets of Giants) — гигантские чаши с термальными источниками!
        if (roll >= 42 && roll < 68) {
            int gobletY = -180 + (int) (Math.abs(chunkHash >> 10) % 400);
            buildGobletOfGiants(writer, chunkX, chunkZ, gobletY, chunkHash);
        }

        // 5. СЛОЙ 5 (-800..-250): АВАНПОСТ ИДОФРОНТ (Ido Front) и ледяные шпили Моря Трупов
        if (Math.floorMod(chunkX, 7) == 0 && Math.floorMod(chunkZ, 7) == 0) {
            buildIdoFrontBastion(writer, chunk, registryAccess, chunkX, chunkZ, -740, seed);
        } else if (roll >= 68 && roll < 82) {
            buildCorpseSeaIceSpire(writer, -750, chunkHash);
        }

        // 6. СЛОЙ 6 (-1400..-800): Руины Золотого Города (Capital of the Unreturned)
        if (roll >= 75 && roll < 92) {
            int goldenY = -1360 + (int) (Math.abs(chunkHash >> 12) % 320);
            buildGoldenCityRelicTower(writer, chunk, registryAccess, chunkX, chunkZ, goldenY, seed);
        }

        // 7. СЛОЙ 7 (-1968..-1400): Спиральные монолиты и Кольцо Средоточия Финального Водоворота
        if (Math.floorMod(chunkX, 6) == 0 && Math.floorMod(chunkZ, 6) == 0) {
            buildFinalMaelstromRing(writer, chunk, registryAccess, -1880, seed);
        }
    }

    /**
     * Слой 1: Древняя каменная мельница Орта и святилище Реликвий
     */
    private static void buildOrthWindmillRuin(
            FastChunkWriter writer, ChunkAccess chunk, RegistryAccess registryAccess,
            int chunkX, int chunkZ, int baseY, long seed
    ) {
        writer.fillBox(3, baseY, 3, 12, baseY + 1, 12, MOSSY_STONE_BRICKS);
        writer.fillBox(5, baseY + 2, 5, 10, baseY + 16, 10, STONE_BRICKS);
        writer.fillBox(6, baseY + 3, 6, 9, baseY + 14, 9, FastChunkWriter.AIR);

        // Лопасти ветряка из темного дуба и парусины (белой шерсти)
        writer.fillBox(1, baseY + 12, 7, 14, baseY + 12, 8, DARK_OAK_LOG);
        writer.fillBox(7, baseY + 6, 4, 8, baseY + 18, 4, Blocks.WHITE_WOOL.defaultBlockState());

        // Сундук с припасами Искателя и спавнер моба 1-го слоя
        LiminalLootPlacer.tryPlaceRoomSupplies(chunk, 0, baseY, 0, chunkX, 1, chunkZ, seed, true, "PRISTINE_UTOPIA");
        AbyssLayerManager.placeLayerMobSpawner(registryAccess, chunk, 7, baseY + 3, 7, seed, 1);
    }

    /**
     * Слой 2: Перевернутый Лес (Inverted Forest) — деревья, растущие корнями в потолок слоя и кронами вниз!
     */
    private static void buildInvertedForestCanopy(FastChunkWriter writer, int chunkX, int chunkZ, int ceilingY, long seed) {
        long h = AbyssLayerManager.hashCoords(seed + 2222L, chunkX, ceilingY, chunkZ);
        if ((h & 1) != 0) return;

        int treeLen = 35 + (int) (Math.abs(h >> 4) % 55); // Свисает вниз на 35..90 блоков!
        // Ствол, растущий сверху вниз
        writer.fillBox(6, ceilingY - treeLen, 6, 9, ceilingY, 9, DARK_OAK_LOG);

        // Перевернутая крона внизу свисающего ствола
        int canopyCenterY = ceilingY - treeLen;
        for (int dy = -8; dy <= 8; dy++) {
            for (int x = 1; x <= 14; x++) {
                for (int z = 1; z <= 14; z++) {
                    int distSq = (x - 7) * (x - 7) + (z - 7) * (z - 7) + (dy * dy);
                    if (distSq <= 45) {
                        writer.set(x, canopyCenterY + dy, z, ((x + z + dy) % 9 == 0) ? SHROOMLIGHT : MOSS_BLOCK);
                    }
                }
            }
        }
    }

    /**
     * Слой 2: Наблюдательный Лагерь Искателей (Seeker Camp) в кронах Перевернутого Леса
     */
    private static void buildSeekerCampOutpost(
            FastChunkWriter writer, ChunkAccess chunk, RegistryAccess registryAccess,
            int chunkX, int chunkZ, int baseY, long seed
    ) {
        // Массивная деревянная платформа, вплетенная в ветви
        writer.fillBox(1, baseY, 1, 14, baseY + 1, 14, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        writer.fillBox(2, baseY + 2, 2, 13, baseY + 7, 13, FastChunkWriter.AIR);

        // Стены и крыша хижины Наблюдателя
        for (int x = 2; x <= 13; x++) {
            for (int z = 2; z <= 13; z++) {
                if (x == 2 || x == 13 || z == 2 || z == 13) {
                    writer.fillBox(x, baseY + 2, z, x, baseY + 6, z, DARK_OAK_LOG);
                }
                writer.set(x, baseY + 7, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
        // Входные проемы и окна
        writer.fillBox(7, baseY + 2, 2, 8, baseY + 4, 2, FastChunkWriter.AIR);
        writer.fillBox(7, baseY + 2, 13, 8, baseY + 4, 13, FastChunkWriter.AIR);
        writer.set(7, baseY + 6, 7, SEA_LANTERN);

        LiminalLootPlacer.tryPlaceRoomSupplies(chunk, 0, baseY - 1, 0, chunkX, 2, chunkZ, seed, true, "ARCHIVE_CATACOMBS");
        AbyssLayerManager.placeLayerMobSpawner(registryAccess, chunk, 10, baseY + 2, 10, seed, 2);
    }

    /**
     * Слой 3: Парящий Остров Великого Разлома с гнездом фауны слоя
     */
    private static void buildGreatFaultFloatingIsland(
            FastChunkWriter writer, ChunkAccess chunk, RegistryAccess registryAccess,
            int chunkX, int chunkZ, int centerY, long seed
    ) {
        for (int dy = -10; dy <= 4; dy++) {
            double maxR = (dy <= 0) ? (7.5 * (1.0 + dy / 11.0)) : (7.5 - dy * 1.2);
            for (int x = 1; x < 15; x++) {
                for (int z = 1; z < 15; z++) {
                    double d = Math.sqrt((x - 7.5) * (x - 7.5) + (z - 7.5) * (z - 7.5));
                    if (d <= maxR) {
                        BlockState b = (dy == 2 || dy == 3) ? MOSS_BLOCK : ((dy < -4) ? DEEPSLATE_TILES : STONE_BRICKS);
                        writer.set(x, centerY + dy, z, b);
                    }
                }
            }
        }
        AbyssLayerManager.placeLayerMobSpawner(registryAccess, chunk, 7, centerY + 3, 7, seed, 3);
    }

    /**
     * Слой 4: КУБКИ ГИГАНТОВ (Goblets of Giants) — огромные вогнутые чаши на толстых ножках,
     * наполненные теплой термальной водой, светящимися водорослями и паром!
     */
    private static void buildGobletOfGiants(FastChunkWriter writer, int chunkX, int chunkZ, int baseY, long hash) {
        // 1. Ножка-стебель гигантского кубка (высотой 24 блока)
        writer.fillBox(5, baseY, 5, 10, baseY + 22, 10, CALCITE);

        // 2. Вогнутая чаша Кубка Гигантов (y = baseY + 22 .. baseY + 32)
        int cupBaseY = baseY + 22;
        for (int dy = 0; dy <= 10; dy++) {
            double outerR = 3.5 + dy * 0.45; // Чаша расширяется кверху до радиуса 8 блоков
            double innerR = outerR - 1.6;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    double d = Math.sqrt((x - 7.5) * (x - 7.5) + (z - 7.5) * (z - 7.5));
                    if (d <= outerR) {
                        if (dy <= 2 || d >= innerR) {
                            writer.set(x, cupBaseY + dy, z, (dy == 10) ? MOSS_BLOCK : CALCITE);
                        } else if (dy <= 8) {
                            // Термальная вода внутри чаши кубка + магма/свет на дне
                            writer.set(x, cupBaseY + dy, z, (dy == 3 && ((x + z) % 5 == 0)) ? SEA_LANTERN : WATER);
                        }
                    }
                }
            }
        }
    }

    /**
     * Слой 5: АВАНПОСТ ИДОФРОНТ (Ido Front) — монументальная ритуально-научная крепость Бондрюда
     * посреди замерзшего Моря Трупов с центральным Лифтом Благословения/Проклятия!
     */
    private static void buildIdoFrontBastion(
            FastChunkWriter writer, ChunkAccess chunk, RegistryAccess registryAccess,
            int chunkX, int chunkZ, int seaLevelY, long seed
    ) {
        // Массивный черный бастион высотой 48 блоков, поднимающийся прямо со дна Моря Трупов
        writer.fillBox(1, seaLevelY - 15, 1, 14, seaLevelY + 34, 14, POLISHED_BLACKSTONE);
        writer.fillBox(3, seaLevelY + 2, 3, 12, seaLevelY + 30, 12, FastChunkWriter.AIR);

        // 3 внутренних лабораторных этажа Идофронта
        for (int floor = 0; floor < 3; floor++) {
            int fy = seaLevelY + 1 + (floor * 10);
            writer.fillBox(2, fy, 2, 13, fy, 13, IRON_BLOCK);
            writer.set(4, fy, 4, SEA_LANTERN);
            writer.set(11, fy, 4, SEA_LANTERN);
            writer.set(4, fy, 11, SEA_LANTERN);
            writer.set(11, fy, 11, SEA_LANTERN);

            // Камеры содержания и спавнеры мобов 5-го слоя
            AbyssLayerManager.placeLayerMobSpawner(registryAccess, chunk, 3, fy + 1, 3, seed + floor, 5);
        }

        // Центральная стеклянная шахта Экспериментального Лифта Бондрюда (x = 6..9, z = 6..9)
        for (int y = seaLevelY + 1; y <= seaLevelY + 32; y++) {
            for (int x = 6; x <= 9; x++) {
                for (int z = 6; z <= 9; z++) {
                    boolean border = (x == 6 || x == 9 || z == 6 || z == 9);
                    if (x == 7 && z == 7) {
                        writer.set(x, y, z, WATER); // Капсула подъема (испытание Проклятием 6-го слоя!)
                    } else if (border && !(x == 7 && z == 6)) {
                        writer.set(x, y, z, (y % 5 == 0) ? OCHRE_FROGLIGHT : TINTED_GLASS);
                    } else {
                        writer.set(x, y, z, FastChunkWriter.AIR);
                    }
                }
            }
        }

        LiminalLootPlacer.tryPlaceRoomSupplies(chunk, 0, seaLevelY - 1, 0, chunkX, 5, chunkZ, seed, true, "DEEP_BRUTALIST_SUPPLIES");
    }

    private static void buildCorpseSeaIceSpire(FastChunkWriter writer, int seaY, long hash) {
        int height = 28 + (int) (Math.abs(hash % 35));
        for (int dy = -10; dy <= height; dy++) {
            double r = Math.max(1.0, 5.5 * (1.0 - (double) dy / (height + 5)));
            for (int x = 2; x <= 13; x++) {
                for (int z = 2; z <= 13; z++) {
                    if ((x - 7.5) * (x - 7.5) + (z - 7.5) * (z - 7.5) <= r * r) {
                        writer.set(x, seaY + dy, z, ((dy + x) % 4 == 0) ? BLUE_ICE : PACKED_ICE);
                    }
                }
            }
        }
    }

    /**
     * Слой 6: Башня Реликвий Столицы Невернувшихся (Golden City Relic Tower)
     */
    private static void buildGoldenCityRelicTower(
            FastChunkWriter writer, ChunkAccess chunk, RegistryAccess registryAccess,
            int chunkX, int chunkZ, int baseY, long seed
    ) {
        int height = 42;
        for (int dy = 0; dy <= height; dy++) {
            int y = baseY + dy;
            boolean isFloor = (dy % 8 == 0);
            for (int x = 3; x <= 12; x++) {
                for (int z = 3; z <= 12; z++) {
                    boolean wall = (x == 3 || x == 12 || z == 3 || z == 12);
                    if (wall) {
                        boolean window = (dy % 8 >= 3 && dy % 8 <= 5) && (x >= 6 && x <= 9 || z >= 6 && z <= 9);
                        writer.set(x, y, z, window ? FastChunkWriter.AIR : ((dy % 4 == 0) ? GILDED_BLACKSTONE : SMOOTH_QUARTZ));
                    } else if (isFloor) {
                        writer.set(x, y, z, ((x + z) % 4 == 0) ? OCHRE_FROGLIGHT : SMOOTH_QUARTZ);
                    } else {
                        writer.set(x, y, z, FastChunkWriter.AIR);
                    }
                }
            }
        }

        // Сокровищница Реликвий 6-го Слоя и страж
        LiminalLootPlacer.tryPlaceRoomSupplies(chunk, 0, baseY + 6, 0, chunkX, 6, chunkZ, seed, true, "DEEP_BRUTALIST_SUPPLIES");
        AbyssLayerManager.placeLayerMobSpawner(registryAccess, chunk, 7, baseY + 9, 7, seed, 6);
    }

    /**
     * Слой 7: Кольцо Средоточия Финального Водоворота (Pivotal Ring Sanctuary)
     */
    private static void buildFinalMaelstromRing(
            FastChunkWriter writer, ChunkAccess chunk, RegistryAccess registryAccess,
            int baseY, long seed
    ) {
        // Платформа из плачущего обсидиана и скалка
        writer.fillBox(2, baseY, 2, 13, baseY + 1, 13, CRYING_OBSIDIAN);
        // Вертикальное мистическое кольцо (Тороид) над алтарем
        for (int dy = 2; dy <= 18; dy++) {
            for (int x = 1; x <= 14; x++) {
                double d = Math.sqrt((x - 7.5) * (x - 7.5) + (dy - 10.0) * (dy - 10.0));
                if (d >= 4.5 && d <= 6.5) {
                    writer.set(x, baseY + dy, 7, ((x + dy) % 3 == 0) ? SEA_LANTERN : SCULK);
                    writer.set(x, baseY + dy, 8, CRYING_OBSIDIAN);
                }
            }
        }
        AbyssLayerManager.placeLayerMobSpawner(registryAccess, chunk, 7, baseY + 2, 7, seed, 7);
    }
}
