package com.megastrata.world.builder;

import com.megastrata.content.ModRegistry;
import com.megastrata.world.LayerProfile;
import com.megastrata.world.MegastructureType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import java.util.Random;

public class FullMegastructureBuilder {

    public enum RareCombination {
        NONE,
        FLOODED_CITY_IN_INDUSTRIAL_FRAME,
        LIVING_PRODUCTION_LAYER,
        BURIED_INFRASTRUCTURE,
        CITY_INSIDE_CYLINDER_SHAFT
    }

    public record LayerPalette(BlockState primary, BlockState secondary, BlockState accent) {
        public static LayerPalette forType(MegastructureType type) {
            return switch (type) {
                case ENDLESS_FRAME -> new LayerPalette(
                        ModRegistry.STRUCTURAL_CONCRETE.get().defaultBlockState(),
                        Blocks.BLACKSTONE.defaultBlockState(),
                        Blocks.SEA_LANTERN.defaultBlockState());
                case VERTICAL_HORIZON_CITY -> new LayerPalette(
                        Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),
                        Blocks.IRON_BLOCK.defaultBlockState(),
                        Blocks.OCHRE_FROGLIGHT.defaultBlockState());
                case DEAD_OCEAN -> new LayerPalette(
                        Blocks.DARK_PRISMARINE.defaultBlockState(),
                        Blocks.CYAN_TERRACOTTA.defaultBlockState(),
                        Blocks.VERDANT_FROGLIGHT.defaultBlockState());
                case INDUSTRIAL_CONTINENT -> new LayerPalette(
                        Blocks.NETHERITE_BLOCK.defaultBlockState(),
                        Blocks.EXPOSED_COPPER.defaultBlockState(),
                        Blocks.YELLOW_CONCRETE.defaultBlockState());
                case BIOMECHANICAL_FOREST -> new LayerPalette(
                        Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
                        Blocks.MUD_BRICKS.defaultBlockState(),
                        Blocks.SHROOMLIGHT.defaultBlockState());
                case HORIZONTAL_SKIES -> new LayerPalette(
                        Blocks.SMOOTH_QUARTZ.defaultBlockState(),
                        Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(),
                        Blocks.GLOWSTONE.defaultBlockState());
                case BURIED_ARCHIVE -> new LayerPalette(
                        Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                        Blocks.BLUE_TERRACOTTA.defaultBlockState(),
                        Blocks.REDSTONE_LAMP.defaultBlockState());
                case ZERO_LAYER -> new LayerPalette(
                        Blocks.REINFORCED_DEEPSLATE.defaultBlockState(),
                        Blocks.TUFF.defaultBlockState(),
                        Blocks.END_ROD.defaultBlockState());
            };
        }
    }

    // Генерация огромной переходной Снежной Равнины между комплексами зданий Гигахруща
    public static void buildTransitionalSnowyPlainBetweenBuildings(ChunkAccess chunk, int startX, int startZ, long worldSeed) {
        int[] plainBaseLevels = { -400, 0, 400, 800 };
        for (int baseLevel : plainBaseLevels) {
            int centerSurfY = baseLevel + 6;
            for (int x = 0; x < 16; x++) {
                int wx = startX + x;
                for (int z = 0; z < 16; z++) {
                    int wz = startZ + z;
                    int wave = (int) (Math.sin(wx * 0.012) * 2.5 + Math.cos(wz * 0.012) * 2.5);
                    int surfY = baseLevel + 6 + wave;
                    if (x == 8 && z == 8) centerSurfY = surfY;

                    chunk.setBlockState(new BlockPos(x, baseLevel, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), false);
                    for (int y = baseLevel + 1; y <= surfY; y++) {
                        chunk.setBlockState(new BlockPos(x, y, z), y == surfY ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState(), false);
                    }
                    chunk.setBlockState(new BlockPos(x, surfY + 1, z), Blocks.SNOW.defaultBlockState(), false);
                }
            }
            // Спавн редкой русской деревеньки, палаток, колодцев и рухнувших панельных плит на переходной снежной равнине
            MicroAndVillageGenerator.buildAbandonedRussianVillage(chunk, centerSurfY, worldSeed ^ (baseLevel * 31L));
            MicroAndVillageGenerator.generatePlayableMicroStructures(chunk, centerSurfY, worldSeed ^ (baseLevel * 97L));
        }
    }

    // Возвращены ВСЕ оригинальные структуры из v1.0 + Лабиринт и трубопроводы
    public static void buildSpecializedLayerContent(ChunkAccess chunk, LayerProfile profile, int chunkX, int chunkZ) {
        Random rand = new Random(profile.sectorSeed() ^ ((long) chunkX * 31 + chunkZ));
        LayerPalette palette = LayerPalette.forType(profile.type());
        RareCombination rareCombo = rollRareCombination(rand);

        switch (profile.type()) {
            case VERTICAL_HORIZON_CITY -> buildVerticalCity(chunk, profile, palette, rand, rareCombo);
            case DEAD_OCEAN -> buildDeadOcean(chunk, profile, palette, rand);
            case INDUSTRIAL_CONTINENT -> buildIndustrialContinent(chunk, profile, palette, rand, rareCombo);
            case BIOMECHANICAL_FOREST -> buildBiomechanicalForest(chunk, profile, palette, rand);
            case HORIZONTAL_SKIES -> buildHorizontalSkies(chunk, profile, palette, rand);
            case BURIED_ARCHIVE -> buildBuriedArchiveAndLabyrinth(chunk, profile, palette, chunkX, chunkZ, rand);
            case ZERO_LAYER -> buildZeroLayer(chunk, profile, palette, rand);
            case ENDLESS_FRAME -> buildEndlessFrameMicroEvents(chunk, profile, palette, rand);
        }

        if (rand.nextFloat() < 0.003f) {
            generateRareAnomaly(chunk, profile.baseMinY() + 120, palette, rand);
        }
    }

    private static RareCombination rollRareCombination(Random rand) {
        float roll = rand.nextFloat();
        if (roll < 0.01f) return RareCombination.FLOODED_CITY_IN_INDUSTRIAL_FRAME;
        if (roll < 0.02f) return RareCombination.LIVING_PRODUCTION_LAYER;
        if (roll < 0.03f) return RareCombination.BURIED_INFRASTRUCTURE;
        if (roll < 0.04f) return RareCombination.CITY_INSIDE_CYLINDER_SHAFT;
        return RareCombination.NONE;
    }

    private static void buildVerticalCity(ChunkAccess chunk, LayerProfile profile, LayerPalette palette, Random rand, RareCombination combo) {
        int baseY = profile.baseMinY() + 60;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        if (rand.nextFloat() < 0.35f) {
            for (int y = baseY - 80; y <= baseY + 160; y++) {
                for (int x = 2; x <= 13; x++) {
                    for (int z = 2; z <= 13; z++) {
                        boolean wall = (x == 2 || x == 13 || z == 2 || z == 13);
                        boolean floor = (y % 6 == 0);
                        pos.set(x, y, z);
                        if (wall) {
                            chunk.setBlockState(pos, (y % 6 == 2 && rand.nextFloat() < 0.08f) ? palette.accent() : palette.primary(), false);
                        } else if (floor) {
                            chunk.setBlockState(pos, palette.secondary(), false);
                        }
                    }
                }
                if (y % 24 == 0) {
                    if (rand.nextBoolean()) {
                        chunk.setBlockState(new BlockPos(5, y + 1, 5), ModRegistry.FOLDING_TABLE.get().defaultBlockState(), false);
                        chunk.setBlockState(new BlockPos(5, y + 2, 5), ModRegistry.KETTLE_BLOCK.get().defaultBlockState(), false);
                        chunk.setBlockState(new BlockPos(12, y + 1, 6), Blocks.POTTED_FERN.defaultBlockState(), false);
                    } else {
                        chunk.setBlockState(new BlockPos(7, y + 1, 7), ModRegistry.PORTABLE_LAMP.get().defaultBlockState(), false);
                    }
                }
            }
        }

        if (rand.nextFloat() < 0.03f) {
            buildThreePlatformStation(chunk, baseY + 24, palette);
        }

        if (combo == RareCombination.BURIED_INFRASTRUCTURE) {
            for (int x = 0; x < 16; x++)
                for (int z = 0; z < 16; z++)
                    for (int y = baseY - 80; y < baseY - 40; y++)
                        chunk.setBlockState(new BlockPos(x, y, z), Blocks.SAND.defaultBlockState(), false);
        }
    }

    private static void buildThreePlatformStation(ChunkAccess chunk, int y, LayerPalette palette) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 4; z++) chunk.setBlockState(new BlockPos(x, y, z), palette.primary(), false);
            chunk.setBlockState(new BlockPos(x, y + 1, 4), Blocks.RAIL.defaultBlockState(), false);
            for (int z = 6; z < 10; z++) chunk.setBlockState(new BlockPos(x, y, z), palette.primary(), false);
            if (x < 9) {
                for (int z = 12; z < 15; z++) chunk.setBlockState(new BlockPos(x, y, z), palette.primary(), false);
            }
        }
        chunk.setBlockState(new BlockPos(2, y + 2, 1), ModRegistry.ARCHIVE_TERMINAL.get().defaultBlockState(), false);
    }

    private static void buildDeadOcean(ChunkAccess chunk, LayerProfile profile, LayerPalette palette, Random rand) {
        int waterTop = profile.baseMinY() + 180;
        if (rand.nextFloat() < 0.1f) {
            for (int x = 3; x <= 12; x++) {
                for (int z = 3; z <= 12; z++) {
                    for (int y = waterTop - 30; y <= waterTop - 20; y++) {
                        boolean shell = (x == 3 || x == 12 || z == 3 || z == 12 || y == waterTop - 30 || y == waterTop - 20);
                        chunk.setBlockState(new BlockPos(x, y, z), shell ? palette.primary() : Blocks.AIR.defaultBlockState(), false);
                    }
                }
            }
            chunk.setBlockState(new BlockPos(7, waterTop - 29, 7), ModRegistry.WATER_PUMP_CONTROLLER.get().defaultBlockState(), false);
        }
    }

    private static void buildIndustrialContinent(ChunkAccess chunk, LayerProfile profile, LayerPalette palette, Random rand, RareCombination combo) {
        int floorY = profile.baseMinY() + 100;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.setBlockState(new BlockPos(x, floorY, z), palette.primary(), false);
                if (z >= 6 && z <= 9) {
                    chunk.setBlockState(new BlockPos(x, floorY + 1, z), ModRegistry.CONVEYOR_BELT.get().defaultBlockState(), false);
                }
            }
        }
        if (rand.nextFloat() < 0.25f) {
            chunk.setBlockState(new BlockPos(8, floorY + 2, 4), ModRegistry.FACTORY_ASSEMBLER.get().defaultBlockState(), false);
        }
        if (rand.nextFloat() < 0.005f) {
            for (int y = floorY + 1; y < floorY + 35; y++) {
                chunk.setBlockState(new BlockPos(8, y, 8), palette.secondary(), false);
                if (y % 5 == 0) chunk.setBlockState(new BlockPos(7, y, 8), palette.accent(), false);
            }
        }
    }

    private static void buildBiomechanicalForest(ChunkAccess chunk, LayerProfile profile, LayerPalette palette, Random rand) {
        int baseY = profile.baseMinY() + 80;
        if (rand.nextFloat() < 0.3f) {
            for (int y = baseY; y < baseY + 160; y++) {
                for (int x = 5; x <= 10; x++) {
                    for (int z = 5; z <= 10; z++) {
                        boolean bark = (x == 5 || x == 10 || z == 5 || z == 10);
                        if (bark) {
                            chunk.setBlockState(new BlockPos(x, y, z), (y % 9 == 0) ? palette.accent() : palette.primary(), false);
                        }
                    }
                }
            }
            chunk.setBlockState(new BlockPos(4, baseY + 1, 4), ModRegistry.BIOMECH_SPORE_PLANT.get().defaultBlockState(), false);
        }
    }

    private static void buildHorizontalSkies(ChunkAccess chunk, LayerProfile profile, LayerPalette palette, Random rand) {
        int ceilingY = profile.baseMaxY() - 10;
        int platformY = profile.baseMinY() + 120;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (rand.nextFloat() < 0.85f) {
                    chunk.setBlockState(new BlockPos(x, ceilingY, z), palette.accent(), false);
                }
                chunk.setBlockState(new BlockPos(x, platformY, z), palette.primary(), false);
            }
        }
    }

    private static void buildBuriedArchiveAndLabyrinth(ChunkAccess chunk, LayerProfile profile, LayerPalette palette, int chunkX, int chunkZ, Random rand) {
        int baseY = profile.baseMinY() + 60;
        for (int x = 2; x < 14; x += 4) {
            for (int z = 2; z < 14; z++) {
                for (int y = baseY; y < baseY + 28; y++) {
                    chunk.setBlockState(new BlockPos(x, y, z), palette.primary(), false);
                }
            }
        }
        chunk.setBlockState(new BlockPos(3, baseY + 1, 8), ModRegistry.ARCHIVE_TERMINAL.get().defaultBlockState(), false);

        // Лабиринт с ценным лутом на верхнем подуровне Архива
        int labY = baseY + 80;
        for (int cellX = 0; cellX < 4; cellX++) {
            for (int cellZ = 0; cellZ < 4; cellZ++) {
                long cellSeed = ((long) (chunkX * 4 + cellX) * 73856093L) ^ ((long) (chunkZ * 4 + cellZ) * 19349663L);
                Random cellRand = new Random(cellSeed);
                boolean wallNorth = cellRand.nextBoolean();
                boolean wallWest = !wallNorth || cellRand.nextFloat() < 0.35f;
                int bx = cellX * 4, bz = cellZ * 4;
                for (int dx = 0; dx < 4; dx++) {
                    for (int dz = 0; dz < 4; dz++) {
                        chunk.setBlockState(new BlockPos(bx + dx, labY - 1, bz + dz), Blocks.SMOOTH_STONE.defaultBlockState(), false);
                        if ((dx == 0 && dz == 0) || (wallNorth && dz == 0) || (wallWest && dx == 0)) {
                            for (int y = labY; y <= labY + 14; y++) {
                                chunk.setBlockState(new BlockPos(bx + dx, y, bz + dz), Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(), false);
                            }
                        }
                    }
                }
            }
        }
        if (rand.nextFloat() < 0.06f) {
            BlockPos chestPos = new BlockPos(8, labY, 8);
            chunk.setBlockState(chestPos, Blocks.CHEST.defaultBlockState(), false);
            if (chunk.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest) {
                chest.setLootTable(BuiltInLootTables.ANCIENT_CITY, rand.nextLong());
            }
        }
    }

    private static void buildZeroLayer(ChunkAccess chunk, LayerProfile profile, LayerPalette palette, Random rand) {
        int baseY = profile.baseMinY() + 40;
        if (rand.nextFloat() < 0.015f) {
            for (int x = 5; x <= 10; x++) {
                for (int z = 5; z <= 10; z++) {
                    chunk.setBlockState(new BlockPos(x, baseY, z), palette.secondary(), false);
                }
            }
            chunk.setBlockState(new BlockPos(7, baseY, 7), Blocks.WATER.defaultBlockState(), false);
            chunk.setBlockState(new BlockPos(8, baseY + 1, 7), ModRegistry.PORTABLE_LAMP.get().defaultBlockState(), false);
        }
    }

    private static void buildEndlessFrameMicroEvents(ChunkAccess chunk, LayerProfile profile, LayerPalette palette, Random rand) {
        if (rand.nextFloat() < 0.02f) {
            int y = profile.baseMinY() + 160;
            chunk.setBlockState(new BlockPos(8, y, 8), ModRegistry.SHELTER_HEARTH.get().defaultBlockState(), false);
        }
    }

    private static void generateRareAnomaly(ChunkAccess chunk, int y, LayerPalette palette, Random rand) {
        int anomalyType = rand.nextInt(3);
        switch (anomalyType) {
            case 0 -> {
                for (int i = 0; i < 10; i++) {
                    chunk.setBlockState(new BlockPos(3 + i, y + i, 8), Blocks.STONE_BRICK_STAIRS.defaultBlockState(), false);
                }
                chunk.setBlockState(new BlockPos(13, y + 10, 8), Blocks.IRON_DOOR.defaultBlockState(), false);
            }
            case 1 -> {
                for (int x = 2; x <= 12; x += 2) {
                    for (int z = 2; z <= 12; z += 2) {
                        chunk.setBlockState(new BlockPos(x, y, z), palette.primary(), false);
                        chunk.setBlockState(new BlockPos(x, y + 1, z), Blocks.OAK_DOOR.defaultBlockState(), false);
                    }
                }
            }
            case 2 -> {
                for (int x = 6; x <= 10; x++) {
                    for (int z = 6; z <= 10; z++) {
                        chunk.setBlockState(new BlockPos(x, y, z), palette.primary(), false);
                        chunk.setBlockState(new BlockPos(x, y + 4, z), palette.primary(), false);
                    }
                }
                chunk.setBlockState(new BlockPos(8, y + 3, 8), Blocks.SEA_LANTERN.defaultBlockState(), false);
            }
        }
    }
}
