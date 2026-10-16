package com.example.liminalworld.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class BedrockSpawnVaultBuilder {

    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    private static final BlockState POLISHED_BLACKSTONE_BRICKS = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState CHISELED_BLACKSTONE = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState GILDED_BLACKSTONE = Blocks.GILDED_BLACKSTONE.defaultBlockState();
    private static final BlockState DEEPSLATE_TILES = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private static final BlockState IRON_BARS = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState CHAIN_Y = Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
    private static final BlockState SOUL_LANTERN = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    private static final BlockState SEA_LANTERN = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SMOOTH_QUARTZ = Blocks.SMOOTH_QUARTZ.defaultBlockState();

    /**
     * Строит неприступную Бедроковую Комнату Искателей на самом верху мира (в чанке 0, 0 на высоте Y = 1920..1934),
     * в центре пола которой зияет вертикальная ДЫРА ВНИЗ (4x4), ведущая в 1-й Слой Бездны!
     */
    public static void buildBedrockSpawnRoom(FastChunkWriter writer, ChunkAccess chunk, long seed) {
        int baseY = AbyssLayerManager.BEDROCK_VAULT_Y; // Y = 1920
        int topY = baseY + 14;                         // Y = 1934

        // 1. Внешний герметичный саркофаг из БЕДРОКА (потолок, стены и пол), чтобы единственный выход был через дыру в полу!
        writer.fillBox(0, baseY, 0, 15, topY, 15, BEDROCK);
        // Внутреннее пространство комнаты (1..14, baseY+2 .. topY-2, 1..14)
        writer.fillBox(2, baseY + 2, 2, 13, topY - 2, 13, FastChunkWriter.AIR);

        // Внутренняя отделка пола и стен древним полированным и золоченым черным камнем поверх бедрока
        for (int x = 1; x <= 14; x++) {
            for (int z = 1; z <= 14; z++) {
                writer.set(x, baseY + 1, z, ((x + z) % 3 == 0) ? GILDED_BLACKSTONE : DEEPSLATE_TILES);
                writer.set(x, topY - 1, z, POLISHED_BLACKSTONE_BRICKS);
            }
        }

        // Монументальные угловые колонны и сводчатые ребра внутри Бедроковой комнаты
        int[][] pillars = {{2, 2}, {13, 2}, {2, 13}, {13, 13}, {2, 7}, {13, 7}, {7, 2}, {7, 13}};
        for (int[] p : pillars) {
            writer.fillBox(p[0], baseY + 2, p[1], p[0], topY - 2, p[1], CHISELED_BLACKSTONE);
        }

        // Освещение под потолком (Фонари душ на цепях и морские фонари в своде)
        int[][] lights = {{4, 4}, {11, 4}, {4, 11}, {11, 11}};
        for (int[] l : lights) {
            writer.set(l[0], topY - 1, l[1], SEA_LANTERN);
            writer.set(l[0], topY - 2, l[1], CHAIN_Y);
            writer.set(l[0], topY - 3, l[1], CHAIN_Y);
            writer.set(l[0], topY - 4, l[1], SOUL_LANTERN);
        }

        // 2. ПРОРЕЗАЕМ ЦЕНТРАЛЬНУЮ ДЫРУ ВНИЗ (x = 6..9, z = 6..9) СКВОЗЬ БЕДРОКОВЫЙ ПОЛ ДО САМОГО ПЕРВОГО СЛОЯ!
        int landingY = baseY - 75; // Y = 1845 — Приемная гондола Искателей под шахтой
        writer.fillBox(6, landingY + 3, 6, 9, baseY + 5, 9, FastChunkWriter.AIR);

        // Обрамление вокруг дыры в Бедроковой комнате (бортик с проходами, чтобы случайно не упасть в первую секунду)
        for (int x = 5; x <= 10; x++) {
            for (int z = 5; z <= 10; z++) {
                if (x == 5 || x == 10 || z == 5 || z == 10) {
                    writer.set(x, baseY + 1, z, CHISELED_BLACKSTONE);
                    boolean isEntryGap = (x == 5 && (z == 7 || z == 8)) || (x == 10 && (z == 7 || z == 8))
                            || (z == 5 && (x == 7 || x == 8)) || (z == 10 && (x == 7 || x == 8));
                    if (!isEntryGap) {
                        writer.set(x, baseY + 2, z, IRON_BARS);
                    }
                }
            }
        }

        // Вертикальные цепи внутри шахты спуска (по углам дыры 6,6 и 9,9), по которым можно плавно спускаться или ориентироваться
        for (int y = landingY + 4; y <= baseY + 3; y++) {
            writer.set(6, y, 6, CHAIN_Y);
            writer.set(9, y, 9, CHAIN_Y);
            if (y % 6 == 0) {
                writer.set(6, y, 9, SEA_LANTERN);
                writer.set(9, y, 6, SEA_LANTERN);
            }
        }

        // 3. ПОДВЕСНАЯ ПЛАТФОРМА-ГОНДОЛА С ГЛУБОКИМ БАССЕЙНОМ ВОДЫ ВНИЗУ ШАХТЫ (Y = 1842..1845),
        // чтобы прыжок в дыру из Бедроковой комнаты гарантированно приземлял игрока в воду на краю 1-го Слоя Бездны!
        writer.fillBox(3, landingY, 3, 12, landingY + 1, 12, DEEPSLATE_TILES);
        for (int x = 3; x <= 12; x++) {
            for (int z = 3; z <= 12; z++) {
                if (x == 3 || x == 12 || z == 3 || z == 12) {
                    writer.set(x, landingY + 2, z, CHISELED_BLACKSTONE);
                } else {
                    // Водная подушка глубиной 2 блока прямо под дырой!
                    writer.set(x, landingY + 2, z, WATER);
                    writer.set(x, landingY + 3, z, WATER);
                }
            }
        }
        // Цепи подвеса гондолы
        writer.fillBox(3, landingY + 3, 3, 3, baseY - 1, 3, CHAIN_Y);
        writer.fillBox(12, landingY + 3, 3, 12, baseY - 1, 3, CHAIN_Y);
        writer.fillBox(3, landingY + 3, 12, 3, baseY - 1, 12, CHAIN_Y);
        writer.fillBox(12, landingY + 3, 12, 12, baseY - 1, 12, CHAIN_Y);

        // 4. Сундук со стартовым снаряжением Искателя Бездны рядом с точкой спавна игрока (x = 3, y = baseY + 2, z = 5)
        BlockPos chestPos = new BlockPos(
                chunk.getPos().getMinBlockX() + 3,
                baseY + 2,
                chunk.getPos().getMinBlockZ() + 4
        );
        BlockState chestState = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        chunk.setBlockState(chestPos, chestState, false);
        ChestBlockEntity chestEntity = new ChestBlockEntity(chestPos, chestState);
        chestEntity.setLootTable(LiminalLootPlacer.LEVEL_0_SUPPLIES, seed ^ 0xABCDEF99L);
        chunk.setBlockEntity(chestEntity);
    }
}
