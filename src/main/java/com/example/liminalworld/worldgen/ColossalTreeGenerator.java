package com.example.liminalworld.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ColossalTreeGenerator {

    private static final BlockState DARK_OAK_WOOD = Blocks.DARK_OAK_WOOD.defaultBlockState();
    private static final BlockState SPRUCE_WOOD = Blocks.SPRUCE_WOOD.defaultBlockState();
    private static final BlockState JUNGLE_WOOD = Blocks.JUNGLE_WOOD.defaultBlockState();
    private static final BlockState MANGROVE_WOOD = Blocks.MANGROVE_WOOD.defaultBlockState();
    private static final BlockState CRIMSON_HYPHAE = Blocks.CRIMSON_HYPHAE.defaultBlockState();
    private static final BlockState WARPED_HYPHAE = Blocks.WARPED_HYPHAE.defaultBlockState();
    private static final BlockState BASALT_BARK = Blocks.POLISHED_BASALT.defaultBlockState();

    // Листва с persistent = true, чтобы гигантские кроны никогда не осыпались!
    private static final BlockState OAK_LEAVES = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState JUNGLE_LEAVES = Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState AZALEA_LEAVES = Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState CHERRY_LEAVES = Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState MANGROVE_LEAVES = Blocks.MANGROVE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    private static final BlockState WARPED_WART = Blocks.WARPED_WART_BLOCK.defaultBlockState();

    private static final BlockState SHROOMLIGHT = Blocks.SHROOMLIGHT.defaultBlockState();
    private static final BlockState OCHRE_FROGLIGHT = Blocks.OCHRE_FROGLIGHT.defaultBlockState();
    private static final BlockState VERDANT_FROGLIGHT = Blocks.VERDANT_FROGLIGHT.defaultBlockState();
    private static final BlockState MOSS_BLOCK = Blocks.MOSS_BLOCK.defaultBlockState();
    private static final BlockState GLOW_LICHEN = Blocks.SHROOMLIGHT.defaultBlockState();
    private static final BlockState SPRUCE_PLANKS = Blocks.SPRUCE_PLANKS.defaultBlockState();

    /**
     * Генерирует Исполинские Деревья Бездны высотой от 200 до 1000 блоков!
     * Использует детерминированную макро-сетку (ячейки по 96x96 блоков), поэтому гигантские корни,
     * стволы, дупла с винтовыми лестницами, многоярусные ветви и светящиеся кроны бесшовно
     * соединяются через границы соседних чанков!
     */
    public static void generateColossalTreesInChunk(FastChunkWriter writer, int chunkX, int chunkZ, long seed) {
        // Не спавним ствол прямо поверх центральной шахты Бедроковой Комнаты в чанке (0, 0)
        int chunkMinX = chunkX * 16;
        int chunkMinZ = chunkZ * 16;

        // Проверяем ячейки макро-сетки (каждая ячейка = 6x6 чанков = 96x96 блоков)
        int gridX = Math.floorDiv(chunkX, 6);
        int gridZ = Math.floorDiv(chunkZ, 6);

        for (int gx = gridX - 1; gx <= gridX + 1; gx++) {
            for (int gz = gridZ - 1; gz <= gridZ + 1; gz++) {
                long cellHash = AbyssLayerManager.hashCoords(seed + 777111L, gx, 0, gz);
                // В 65% макро-ячеек растет Исполинское Дерево Бездны
                if (Math.abs(cellHash % 100) >= 65) continue;

                // Центр дерева внутри макро-ячейки 96x96
                int treeCenterX = (gx * 96) + 24 + (int) (Math.abs(cellHash >> 8) % 48);
                int treeCenterZ = (gz * 96) + 24 + (int) (Math.abs(cellHash >> 16) % 48);

                // Защита спавн-шахты в (0..15, 0..15)
                if (Math.abs(treeCenterX - 8) < 36 && Math.abs(treeCenterZ - 8) < 36) {
                    continue;
                }

                // Быстрая проверка: пересекает ли радиус кроны/корней (44 блока) текущий чанк
                int closestX = Math.max(chunkMinX, Math.min(treeCenterX, chunkMinX + 15));
                int closestZ = Math.max(chunkMinZ, Math.min(treeCenterZ, chunkMinZ + 15));
                int distSqToChunk = (closestX - treeCenterX) * (closestX - treeCenterX)
                        + (closestZ - treeCenterZ) * (closestZ - treeCenterZ);
                if (distSqToChunk > 44 * 44) continue;

                // Выбираем тип и высоту дерева (от 200 до 1000 блоков!)
                boolean isTitanWorldTree = (Math.abs(cellHash >> 24) % 100) < 35;
                int treeHeight;
                int baseY;
                int baseRadius;

                if (isTitanWorldTree) {
                    // Древо-Титан, пронзающее сразу несколько Слоев Бездны: высота от 550 до 1000 блоков!
                    treeHeight = 550 + (int) (Math.abs(cellHash >> 12) % 451); // 550..1000 блоков
                    int startLayer = 3 + (int) (Math.abs(cellHash >> 20) % 4); // Корни на 3, 4, 5 или 6 слое
                    baseY = AbyssLayerManager.getLayerFloorY(startLayer) + 4;
                    baseRadius = 8 + (int) (Math.abs(cellHash >> 6) % 4); // Радиус ствола 8..11 (диаметр до 22 блоков!)
                } else {
                    // Региональное Огромное Дерево Слоя: высота от 200 до 520 блоков!
                    treeHeight = 200 + (int) (Math.abs(cellHash >> 12) % 321); // 200..520 блоков
                    int startLayer = 1 + (int) (Math.abs(cellHash >> 20) % 5); // Слой 1..5
                    baseY = AbyssLayerManager.getLayerFloorY(startLayer) + 4;
                    baseRadius = 5 + (int) (Math.abs(cellHash >> 6) % 4); // Радиус ствола 5..8
                }

                int topY = Math.min(writer.getMaxY() - 20, baseY + treeHeight);
                if (topY <= baseY + 100) continue;

                int style = (int) (Math.abs(cellHash >> 30) % 5);
                BlockState bark = getBarkForStyle(style);
                BlockState leaves = getLeavesForStyle(style);
                BlockState glowBlock = (style % 2 == 0) ? SHROOMLIGHT : ((style == 1) ? VERDANT_FROGLIGHT : OCHRE_FROGLIGHT);

                renderColossalTreeIntoChunk(
                        writer, chunkMinX, chunkMinZ,
                        treeCenterX, treeCenterZ,
                        baseY, topY, treeHeight, baseRadius,
                        bark, leaves, glowBlock, cellHash
                );
            }
        }
    }

    private static void renderColossalTreeIntoChunk(
            FastChunkWriter writer,
            int chunkMinX, int chunkMinZ,
            int treeCenterX, int treeCenterZ,
            int baseY, int topY, int treeHeight, int baseRadius,
            BlockState bark, BlockState leaves, BlockState glowBlock,
            long treeHash
    ) {
        int rootZoneHeight = Math.min(110, treeHeight / 5);
        int branchSpacing = 75; // Ярусы гигантских ветвей и крон каждые 75 блоков по вертикали!

        for (int y = baseY - 25; y <= topY + 18; y++) {
            if (y < writer.getMinY() + 8 || y >= writer.getMaxY() - 8) continue;

            int dy = y - baseY;
            double progress = Math.max(0.0, Math.min(1.0, (double) dy / (double) treeHeight));

            // Плавный органический изгиб ствола по мере роста вверх на сотни блоков
            double swayX = Math.sin(y * 0.015 + (treeHash & 255)) * 5.0;
            double swayZ = Math.cos(y * 0.015 + ((treeHash >> 8) & 255)) * 5.0;
            double currentCenterX = treeCenterX + swayX;
            double currentCenterZ = treeCenterZ + swayZ;

            // Радиус ствола плавно сужается к вершине, но расширяется у корней и под ярусами крон
            double trunkRadius = baseRadius * (1.0 - 0.45 * progress);
            if (dy < 0) {
                trunkRadius = baseRadius * 1.3;
            }

            // Проверяем, находится ли текущая высота Y вблизи яруса гигантских ветвей или верхушечного купола
            int dyFromTop = topY - y;
            boolean isTopCanopy = (dyFromTop >= -16 && dyFromTop <= 28);
            int branchMod = Math.floorMod(dy, branchSpacing);
            boolean isTieredBranchZone = (dy > 80 && dy < treeHeight - 30) && (branchMod <= 18);
            int branchTierIndex = dy / branchSpacing;

            for (int lx = 0; lx < 16; lx++) {
                int wx = chunkMinX + lx;
                double dx = wx - currentCenterX;

                for (int lz = 0; lz < 16; lz++) {
                    int wz = chunkMinZ + lz;
                    double dz = wz - currentCenterZ;
                    double distSq = dx * dx + dz * dz;
                    double dist = Math.sqrt(distSq);
                    double angle = Math.atan2(dz, dx);

                    // 1. МОЩНЫЕ ИЗВИЛИСТЫЕ КОРНИ (в нижних 80-110 блоках дерева + уходящие на 25 блоков вглубь породы)
                    if (dy <= rootZoneHeight) {
                        double rootFactor = 1.0 - ((double) Math.max(0, dy) / (double) rootZoneHeight);
                        double maxRootReach = baseRadius + 28.0 * rootFactor;
                        if (dist <= maxRootReach && dist >= trunkRadius - 1.5) {
                            // 6 главных корневых лап, расходящихся лучами с легким закручиванием
                            double rootWave = Math.cos(angle * 6.0 + dy * 0.04 + (treeHash & 15));
                            if (rootWave > 0.72 - (0.28 * rootFactor)) {
                                writer.set(lx, y, lz, bark);
                                if (rootWave > 0.92 && ((y + wx + wz) & 3) == 0) {
                                    writer.set(lx, y + 1, lz, MOSS_BLOCK);
                                }
                            }
                        }
                    }

                    // 2. ГЛАВНЫЙ СТВОЛ ИСПОЛИНСКОГО ДЕРЕВА (с корой, дуплом и винтовой лестницей внутри!)
                    if (dy >= 0 && y <= topY) {
                        // Ребристая кора дерева
                        double barkRidge = Math.cos(angle * 8.0 + y * 0.03) * 0.85;
                        double effectiveOuterR = trunkRadius + barkRidge;
                        double hollowInnerR = Math.max(0.0, trunkRadius - 2.4);

                        if (dist <= effectiveOuterR) {
                            if (dist >= hollowInnerR || trunkRadius < 4.5) {
                                // Стенка ствола с арочными окнами-дуплами каждые 40 блоков
                                boolean isHollowWindow = (dy % 40 >= 2 && dy % 40 <= 6) && (Math.abs(dx) <= 2.0 || Math.abs(dz) <= 2.0);
                                if (isHollowWindow) {
                                    writer.set(lx, y, lz, FastChunkWriter.AIR);
                                } else {
                                    writer.set(lx, y, lz, bark);
                                }
                            } else {
                                // Внутренняя полость гигантского дерева: винтовая деревянная тропа и биолюминесцентное ядро!
                                if (dist <= 1.4) {
                                    // Центральная жила дерева со светящимися плодами
                                    writer.set(lx, y, lz, (y % 5 == 0) ? glowBlock : bark);
                                } else {
                                    double stairAngle = (y * 0.35) % (Math.PI * 2.0);
                                    double angleDiff = Math.atan2(Math.sin(angle - stairAngle), Math.cos(angle - stairAngle));
                                    if (Math.abs(angleDiff) < 0.55) {
                                        writer.set(lx, y, lz, SPRUCE_PLANKS);
                                    } else {
                                        writer.set(lx, y, lz, FastChunkWriter.AIR);
                                    }
                                }
                            }
                            continue;
                        }
                    }

                    // 3. МНОГОЯРУСНЫЕ ВЕТВИ И СВЕТЯЩИЕСЯ КРОНЫ ВДОЛЬ ВСЕЙ ВЫСОТЫ (200..1000 БЛОКОВ)
                    if (isTieredBranchZone) {
                        double tierAngleOffset = branchTierIndex * 1.1;
                        // 5 толстых горизонтальных ветвей на каждом ярусе
                        double branchWave = Math.cos(angle * 5.0 + tierAngleOffset);
                        double maxBranchLen = Math.min(40.0, baseRadius + 24.0);

                        if (branchMod <= 5 && dist <= maxBranchLen && branchWave > 0.78) {
                            // Древесина ветви
                            writer.set(lx, y, lz, bark);
                        } else if (branchMod > 4 && branchMod <= 16 && dist <= maxBranchLen + 4.0 && branchWave > 0.45) {
                            // Подушка листвы и светящихся плодов поверх ветви
                            if (((wx + wz + y) & 15) == 0) {
                                writer.set(lx, y, lz, glowBlock);
                            } else {
                                writer.set(lx, y, lz, leaves);
                            }
                        }
                    }

                    // 4. ГИГАНТСКИЙ ВЕРХУШЕЧНЫЙ КУПОЛ КРОНЫ НА ВЕРШИНЕ ДЕРЕВА
                    if (isTopCanopy) {
                        double canopyRadius = Math.min(42.0, baseRadius * 3.8);
                        double verticalScale = (dyFromTop < 0) ? (dyFromTop / 16.0) : (dyFromTop / 28.0);
                        double ellipsoid = (distSq / (canopyRadius * canopyRadius)) + (verticalScale * verticalScale);

                        if (ellipsoid <= 1.0) {
                            if (dyFromTop >= 0 && dyFromTop <= 4 && dist <= canopyRadius * 0.75 && Math.cos(angle * 6.0) > 0.75) {
                                writer.set(lx, y, lz, bark); // Верхние скелетные ветви купола
                            } else if (((wx * 3 + wz * 5 + y * 7) & 31) == 0) {
                                writer.set(lx, y, lz, glowBlock);
                            } else {
                                writer.set(lx, y, lz, leaves);
                            }
                        }
                    }
                }
            }
        }
    }

    private static BlockState getBarkForStyle(int style) {
        return switch (style) {
            case 0 -> DARK_OAK_WOOD;
            case 1 -> JUNGLE_WOOD;
            case 2 -> MANGROVE_WOOD;
            case 3 -> CRIMSON_HYPHAE;
            default -> SPRUCE_WOOD;
        };
    }

    private static BlockState getLeavesForStyle(int style) {
        return switch (style) {
            case 0 -> OAK_LEAVES;
            case 1 -> AZALEA_LEAVES;
            case 2 -> MANGROVE_LEAVES;
            case 3 -> CHERRY_LEAVES;
            default -> JUNGLE_LEAVES;
        };
    }
}
