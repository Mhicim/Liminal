package com.megastrata.world.builder;

import com.megastrata.content.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Random;

public class MicroAndVillageGenerator {

    // ПРОНИЗЫВАЮЩИЙ ГИГАХРУЩ (Самосбор / Панельки от Y=-2000 до Y=+2000 + Висячие панельные блоки на сваях как на референсе 1000111969.jpg)
    public static void buildPiercingGigakhrushch(ChunkAccess chunk, int startX, int startZ, long seed) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        Random rand = new Random(seed ^ ((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L));

        BlockState panelLight = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState panelSeam = Blocks.GRAY_CONCRETE.defaultBlockState();
        BlockState panelAged = Blocks.ANDESITE.defaultBlockState();
        BlockStatepodezdGreen = Blocks.CYAN_TERRACOTTA.defaultBlockState();
        BlockState podezdWhite = Blocks.CALCITE.defaultBlockState();
        BlockState floorSlab = Blocks.SMOOTH_STONE.defaultBlockState();
        BlockState windowGlass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState warmWindow = Blocks.OCHRE_FROGLIGHT.defaultBlockState();

        // 1. Сквозные панельные корпуса Гигахруща, пронизывающие всю мегаструктуру снизу доверху (каждый 3-й чанк внутри застроенной зоны)
        boolean isPiercingSpine = (Math.floorMod(chunkX, 3) == 0 && Math.floorMod(chunkZ, 3) == 0);
        if (isPiercingSpine) {
            for (int y = -1960; y <= 1920; y++) {
                int floorMod = Math.floorMod(y, 6); // Высота этажа панельки = 6 блоков
                for (int x = 1; x <= 14; x++) {
                    for (int z = 1; z <= 14; z++) {
                        boolean outerWall = (x == 1 || x == 14 || z == 1 || z == 14);
                        boolean innerCorridorWall = (x == 6 || x == 9);

                        if (outerWall) {
                            // Межпанельные швы каждые 6 блоков по высоте и каждые 4 блока по ширине
                            if (floorMod == 0 || x % 4 == 0 || z % 4 == 0) {
                                chunk.setBlockState(new BlockPos(x, y, z), panelSeam, false);
                            } else if ((floorMod == 2 || floorMod == 3) && (x + z) % 2 == 0) {
                                // Типовые окна панельки (часть светится тёплым светом)
                                chunk.setBlockState(new BlockPos(x, y, z), (y * x + z) % 19 == 0 ? warmWindow : windowGlass, false);
                            } else {
                                chunk.setBlockState(new BlockPos(x, y, z), (y % 17 == 0) ? panelAged : panelLight, false);
                            }
                            // Балконы и кондиционеры снаружи панельки
                            if (floorMod == 1 && x == 1 && z >= 5 && z <= 9 && (y % 12 == 0)) {
                                chunk.setBlockState(new BlockPos(0, y, z), floorSlab, false);
                                chunk.setBlockState(new BlockPos(0, y + 1, z), Blocks.IRON_BARS.defaultBlockState(), false);
                            }
                        } else if (floorMod == 0) {
                            // Межэтажное перекрытие (с проёмом под лестничную клетку подъезда)
                            if (!(x >= 7 && x <= 8 && z >= 2 && z <= 5)) {
                                chunk.setBlockState(new BlockPos(x, y, z), floorSlab, false);
                            }
                        } else if (innerCorridorWall && z >= 2 && z <= 13) {
                            // Стены подъезда Гигахруща: снизу покрашены советской сине-зелёной краской, сверху побелены!
                            if (z == 4 || z == 10) {
                                // Дверные проёмы в квартиры на этаже
                                if (floorMod >= 3) {
                                    chunk.setBlockState(new BlockPos(x, y, z), podezdWhite, false);
                                }
                            } else {
                                chunk.setBlockState(new BlockPos(x, y, z), (floorMod <= 2) ? BlockStatepodezdGreen : podezdWhite, false);
                            }
                        }
                    }
                }

                // Лестничный марш внутри подъезда, мусоропровод и тусклая лампа
                int stairZ = 2 + Math.min(3, floorMod);
                chunk.setBlockState(new BlockPos(7, y, stairZ), Blocks.STONE_BRICK_STAIRS.defaultBlockState(), false);
                if (floorMod == 1) {
                    chunk.setBlockState(new BlockPos(8, y, 2), Blocks.HOPPER.defaultBlockState(), false); // Клапан мусоропровода
                }
                if (floorMod == 5) {
                    chunk.setBlockState(new BlockPos(7, y, 8), Blocks.LANTERN.defaultBlockState(), false);
                }
                // На каждых 12 этажах — бытовая мебель в квартирах Гигахруща
                if (floorMod == 1 && Math.floorMod(y, 72) == 1) {
                    chunk.setBlockState(new BlockPos(3, y, 4), ModRegistry.FOLDING_TABLE.get().defaultBlockState(), false);
                    chunk.setBlockState(new BlockPos(3, y + 1, 4), ModRegistry.KETTLE_BLOCK.get().defaultBlockState(), false);
                    chunk.setBlockState(new BlockPos(11, y, 11), ModRegistry.PORTABLE_LAMP.get().defaultBlockState(), false);
                }
            }
        }

        // 2. Висячие панельные блоки на тонких сваях и изогнутых трубах с проводами в тумане (как на референсе 1000111969.jpg)
        if (!isPiercingSpine && rand.nextFloat() < 0.18f) {
            int baseAlt = -1200 + rand.nextInt(2800);
            int boxHeight = 36 + rand.nextInt(48);
            // Тонкая опорная свая под висячей панелькой
            for (int py = baseAlt - 120; py < baseAlt; py++) {
                for (int sx = 7; sx <= 8; sx++) {
                    for (int sz = 7; sz <= 8; sz++) {
                        chunk.setBlockState(new BlockPos(sx, py, sz), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), false);
                    }
                }
            }
            // Сама висячая панельная секция на свае
            for (int y = baseAlt; y <= baseAlt + boxHeight; y++) {
                int fm = Math.floorMod(y, 5);
                for (int x = 3; x <= 12; x++) {
                    for (int z = 3; z <= 12; z++) {
                        boolean wall = (x == 3 || x == 12 || z == 3 || z == 12);
                        if (wall) {
                            chunk.setBlockState(new BlockPos(x, y, z), (fm == 2 && (x + z) % 2 == 0) ? windowGlass : panelLight, false);
                        } else if (fm == 0) {
                            chunk.setBlockState(new BlockPos(x, y, z), floorSlab, false);
                        }
                    }
                }
            }
            // Провода (цепи), уходящие в стороны к соседним висячим панелькам
            for (int i = 0; i < 16; i++) {
                chunk.setBlockState(new BlockPos(i, baseAlt + 10, 8), Blocks.CHAIN.defaultBlockState(), false);
            }
        }
    }

    // Редкая Заброшенная Русская Деревенька на переходных снежных равнинах
    public static void buildAbandonedRussianVillage(ChunkAccess chunk, int surfaceY, long seed) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        int regionX = Math.floorDiv(chunkX, 20);
        int regionZ = Math.floorDiv(chunkZ, 20);
        Random regionRand = new Random(seed ^ (regionX * 982451653L) ^ (regionZ * 776531419L));
        int villageCenterChunkX = regionX * 20 + 6 + regionRand.nextInt(8);
        int villageCenterChunkZ = regionZ * 20 + 6 + regionRand.nextInt(8);

        int dx = chunkX - villageCenterChunkX;
        int dz = chunkZ - villageCenterChunkZ;
        if (Math.abs(dx) > 1 || Math.abs(dz) > 1) return;

        Random localRand = new Random(seed ^ ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L));

        if (dx == 0 && dz == 0) {
            buildRussianVillageWell(chunk, 6, surfaceY, 6);
            buildDirtPathCrossroad(chunk, surfaceY);
        } else {
            if (dx == 1 && dz == 0) {
                buildWoodenChapelRuin(chunk, 3, surfaceY, 3);
            } else if (localRand.nextFloat() < 0.75f) {
                buildRussianLogIzba(chunk, 2, surfaceY, 2, localRand);
            }
        }
    }

    private static void buildDirtPathCrossroad(ChunkAccess chunk, int y) {
        for (int i = 0; i < 16; i++) {
            for (int w = 6; w <= 9; w++) {
                chunk.setBlockState(new BlockPos(i, y, w), Blocks.DIRT_PATH.defaultBlockState(), false);
                chunk.setBlockState(new BlockPos(w, y, i), Blocks.DIRT_PATH.defaultBlockState(), false);
                chunk.setBlockState(new BlockPos(i, y + 1, w), Blocks.AIR.defaultBlockState(), false);
                chunk.setBlockState(new BlockPos(w, y + 1, i), Blocks.AIR.defaultBlockState(), false);
            }
            if (i % 3 != 0) {
                chunk.setBlockState(new BlockPos(i, y + 1, 4), Blocks.SPRUCE_FENCE.defaultBlockState(), false);
                chunk.setBlockState(new BlockPos(4, y + 1, i), Blocks.SPRUCE_FENCE.defaultBlockState(), false);
            }
        }
    }

    public static void buildRussianVillageWell(ChunkAccess chunk, int x0, int y0, int z0) {
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState cobble = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        BlockState fence = Blocks.SPRUCE_FENCE.defaultBlockState();
        BlockState slab = Blocks.SPRUCE_SLAB.defaultBlockState();

        for (int x = x0; x <= x0 + 3; x++) {
            for (int z = z0; z <= z0 + 3; z++) {
                boolean wall = (x == x0 || x == x0 + 3 || z == z0 || z == z0 + 3);
                for (int y = y0 - 8; y <= y0 + 1; y++) {
                    if (wall) {
                        chunk.setBlockState(new BlockPos(x, y, z), y > y0 - 2 ? log : cobble, false);
                    } else if (y < y0 - 3) {
                        chunk.setBlockState(new BlockPos(x, y, z), y == y0 - 4 ? Blocks.ICE.defaultBlockState() : Blocks.WATER.defaultBlockState(), false);
                    } else {
                        chunk.setBlockState(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), false);
                    }
                }
            }
        }
        for (int y = y0 + 2; y <= y0 + 3; y++) {
            chunk.setBlockState(new BlockPos(x0, y, z0), fence, false);
            chunk.setBlockState(new BlockPos(x0 + 3, y, z0), fence, false);
            chunk.setBlockState(new BlockPos(x0, y, z0 + 3), fence, false);
            chunk.setBlockState(new BlockPos(x0 + 3, y, z0 + 3), fence, false);
        }
        chunk.setBlockState(new BlockPos(x0 + 1, y0 + 3, z0 + 1), Blocks.CHAIN.defaultBlockState(), false);
        chunk.setBlockState(new BlockPos(x0 + 1, y0 + 2, z0 + 1), Blocks.CHAIN.defaultBlockState(), false);
        for (int x = x0 - 1; x <= x0 + 4; x++) {
            for (int z = z0 - 1; z <= z0 + 4; z++) {
                chunk.setBlockState(new BlockPos(x, y0 + 4, z), slab, false);
            }
        }
    }

    private static void buildRussianLogIzba(ChunkAccess chunk, int x0, int y0, int z0, Random rand) {
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState stripped = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
        BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState brick = Blocks.BRICKS.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState snow = Blocks.SNOW.defaultBlockState();

        for (int x = x0; x <= x0 + 10; x++) {
            for (int z = z0; z <= z0 + 8; z++) {
                chunk.setBlockState(new BlockPos(x, y0, z), Blocks.COBBLESTONE.defaultBlockState(), false);
                chunk.setBlockState(new BlockPos(x, y0 + 1, z), planks, false);
                for (int y = y0 + 2; y <= y0 + 8; y++) {
                    chunk.setBlockState(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), false);
                }
            }
        }

        for (int y = y0 + 1; y <= y0 + 4; y++) {
            for (int x = x0; x <= x0 + 10; x++) {
                for (int z = z0; z <= z0 + 8; z++) {
                    boolean borderX = (x == x0 + 1 || x == x0 + 9);
                    boolean borderZ = (z == z0 + 1 || z == z0 + 7);
                    if ((borderX && z >= z0 && z <= z0 + 8) || (borderZ && x >= x0 && x <= x0 + 10)) {
                        if (y == y0 + 4 && rand.nextFloat() < 0.12f) continue;
                        chunk.setBlockState(new BlockPos(x, y, z), (y % 2 == 0) ? log : stripped, false);
                    }
                }
            }
        }

        chunk.setBlockState(new BlockPos(x0 + 5, y0 + 2, z0 + 1), Blocks.AIR.defaultBlockState(), false);
        chunk.setBlockState(new BlockPos(x0 + 5, y0 + 3, z0 + 1), Blocks.AIR.defaultBlockState(), false);
        chunk.setBlockState(new BlockPos(x0 + 3, y0 + 3, z0 + 1), glass, false);
        chunk.setBlockState(new BlockPos(x0 + 7, y0 + 3, z0 + 1), glass, false);
        chunk.setBlockState(new BlockPos(x0 + 1, y0 + 3, z0 + 4), glass, false);
        chunk.setBlockState(new BlockPos(x0 + 9, y0 + 3, z0 + 4), glass, false);

        for (int step = 0; step <= 4; step++) {
            int ry = y0 + 5 + step;
            for (int x = x0; x <= x0 + 10; x++) {
                if (z0 + step <= z0 + 8 - step) {
                    chunk.setBlockState(new BlockPos(x, ry, z0 + step), planks, false);
                    chunk.setBlockState(new BlockPos(x, ry, z0 + 8 - step), planks, false);
                    if (rand.nextFloat() < 0.65f) {
                        chunk.setBlockState(new BlockPos(x, ry + 1, z0 + step), snow, false);
                        chunk.setBlockState(new BlockPos(x, ry + 1, z0 + 8 - step), snow, false);
                    }
                }
            }
        }

        int px = x0 + 7, pz = z0 + 5;
        for (int x = px; x <= px + 1; x++) {
            for (int z = pz; z <= pz + 1; z++) {
                chunk.setBlockState(new BlockPos(x, y0 + 2, z), brick, false);
                chunk.setBlockState(new BlockPos(x, y0 + 3, z), brick, false);
            }
        }
        chunk.setBlockState(new BlockPos(px, y0 + 2, pz), Blocks.SMOKER.defaultBlockState(), false);
        for (int cy = y0 + 4; cy <= y0 + 10; cy++) {
            chunk.setBlockState(new BlockPos(px + 1, cy, pz + 1), cy == y0 + 10 ? Blocks.COBBLESTONE_WALL.defaultBlockState() : brick, false);
        }
        chunk.setBlockState(new BlockPos(px + 1, y0 + 11, pz + 1), Blocks.COBWEB.defaultBlockState(), false);

        chunk.setBlockState(new BlockPos(x0 + 3, y0 + 2, z0 + 5), Blocks.CRAFTING_TABLE.defaultBlockState(), false);
        chunk.setBlockState(new BlockPos(x0 + 2, y0 + 4, z0 + 2), Blocks.COBWEB.defaultBlockState(), false);
        BlockPos lootPos = new BlockPos(x0 + 2, y0 + 2, z0 + 6);
        chunk.setBlockState(lootPos, Blocks.BARREL.defaultBlockState(), false);
    }

    private static void buildWoodenChapelRuin(ChunkAccess chunk, int x0, int y0, int z0) {
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState planks = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        for (int x = x0; x <= x0 + 8; x++) {
            for (int z = z0; z <= z0 + 8; z++) {
                boolean wall = (x == x0 || x == x0 + 8 || z == z0 || z == z0 + 8);
                chunk.setBlockState(new BlockPos(x, y0, z), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), false);
                for (int y = y0 + 1; y <= y0 + 7; y++) {
                    if (wall && !(x == x0 + 4 && z == z0 && y <= y0 + 2)) {
                        chunk.setBlockState(new BlockPos(x, y, z), log, false);
                    }
                }
            }
        }
        for (int y = y0 + 8; y <= y0 + 14; y++) {
            for (int x = x0 + 2; x <= x0 + 6; x++) {
                for (int z = z0 + 2; z <= z0 + 6; z++) {
                    if (x == x0 + 2 || x == x0 + 6 || z == z0 + 2 || z == z0 + 6) {
                        chunk.setBlockState(new BlockPos(x, y, z), y > y0 + 11 ? Blocks.SPRUCE_FENCE.defaultBlockState() : planks, false);
                    }
                }
            }
        }
        chunk.setBlockState(new BlockPos(x0 + 4, y0 + 12, z0 + 4), Blocks.BELL.defaultBlockState(), false);
        chunk.setBlockState(new BlockPos(x0 + 4, y0 + 15, z0 + 4), Blocks.LIGHTNING_ROD.defaultBlockState(), false);
    }

    public static void generatePlayableMicroStructures(ChunkAccess chunk, int floorY, long seed) {
        Random rand = new Random(seed ^ chunk.getPos().toLong() ^ (floorY * 31L));
        float roll = rand.nextFloat();

        if (roll < 0.05f) {
            buildStalkerTentCamp(chunk, 4, floorY + 1, 4, rand);
        } else if (roll < 0.09f) {
            buildUnfinishedOutpost(chunk, 3, floorY + 1, 3, rand);
        } else if (roll < 0.12f) {
            buildTechnicalWaterWell(chunk, 6, floorY, 6);
        }
    }

    private static void buildStalkerTentCamp(ChunkAccess chunk, int x0, int y0, int z0, Random rand) {
        BlockState tentCloth = rand.nextBoolean()
                ? Blocks.WHITE_WOOL.defaultBlockState()
                : Blocks.GREEN_TERRACOTTA.defaultBlockState();
        BlockState fence = Blocks.SPRUCE_FENCE.defaultBlockState();

        for (int z = z0; z <= z0 + 4; z++) {
            chunk.setBlockState(new BlockPos(x0, y0, z), tentCloth, false);
            chunk.setBlockState(new BlockPos(x0 + 4, y0, z), tentCloth, false);
            chunk.setBlockState(new BlockPos(x0 + 1, y0 + 1, z), tentCloth, false);
            chunk.setBlockState(new BlockPos(x0 + 3, y0 + 1, z), tentCloth, false);
            chunk.setBlockState(new BlockPos(x0 + 2, y0 + 2, z), tentCloth, false);
        }
        chunk.setBlockState(new BlockPos(x0 + 2, y0, z0), fence, false);
        chunk.setBlockState(new BlockPos(x0 + 2, y0 + 1, z0), fence, false);
        chunk.setBlockState(new BlockPos(x0 + 2, y0, z0 + 6), Blocks.CAMPFIRE.defaultBlockState(), false);
        chunk.setBlockState(new BlockPos(x0 + 1, y0, z0 + 2), Blocks.BARREL.defaultBlockState(), false);
        chunk.setBlockState(new BlockPos(x0 + 3, y0, z0 + 2), Blocks.CRAFTING_TABLE.defaultBlockState(), false);
    }

    private static void buildUnfinishedOutpost(ChunkAccess chunk, int x0, int y0, int z0, Random rand) {
        BlockState concrete = Blocks.GRAY_CONCRETE.defaultBlockState();
        BlockState rebar = Blocks.IRON_BARS.defaultBlockState();
        BlockState scaffold = Blocks.SCAFFOLDING.defaultBlockState();

        for (int x = x0; x <= x0 + 8; x++) {
            for (int z = z0; z <= z0 + 8; z++) {
                boolean wall = (x == x0 || x == x0 + 8 || z == z0 || z == z0 + 8);
                chunk.setBlockState(new BlockPos(x, y0, z), Blocks.STONE_BRICKS.defaultBlockState(), false);
                if (wall) {
                    int wallHeight = 1 + rand.nextInt(4);
                    for (int y = y0 + 1; y <= y0 + wallHeight; y++) {
                        chunk.setBlockState(new BlockPos(x, y, z), y == y0 + wallHeight ? rebar : concrete, false);
                    }
                }
            }
        }
        for (int y = y0 + 1; y <= y0 + 6; y++) {
            chunk.setBlockState(new BlockPos(x0 + 1, y, z0 + 1), scaffold, false);
            chunk.setBlockState(new BlockPos(x0 + 7, y, z0 + 7), rebar, false);
        }
        chunk.setBlockState(new BlockPos(x0 + 7, y0 + 7, z0 + 7), Blocks.SEA_LANTERN.defaultBlockState(), false);
        chunk.setBlockState(new BlockPos(x0 + 4, y0 + 1, z0 + 4), Blocks.BARREL.defaultBlockState(), false);
        chunk.setBlockState(new BlockPos(x0 + 5, y0 + 1, z0 + 4), Blocks.ANVIL.defaultBlockState(), false);
    }

    private static void buildTechnicalWaterWell(ChunkAccess chunk, int x0, int y0, int z0) {
        BlockState wallBlock = Blocks.POLISHED_ANDESITE.defaultBlockState();
        for (int x = x0; x <= x0 + 3; x++) {
            for (int z = z0; z <= z0 + 3; z++) {
                boolean edge = (x == x0 || x == x0 + 3 || z == z0 || z == z0 + 3);
                for (int y = y0 - 5; y <= y0 + 1; y++) {
                    if (edge) {
                        chunk.setBlockState(new BlockPos(x, y, z), wallBlock, false);
                    } else {
                        chunk.setBlockState(new BlockPos(x, y, z), y <= y0 - 1 ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), false);
                    }
                }
            }
        }
        chunk.setBlockState(new BlockPos(x0 + 1, y0 + 2, z0 + 1), Blocks.CHAIN.defaultBlockState(), false);
        chunk.setBlockState(new BlockPos(x0 + 1, y0 + 3, z0 + 1), Blocks.LANTERN.defaultBlockState(), false);
    }
}
