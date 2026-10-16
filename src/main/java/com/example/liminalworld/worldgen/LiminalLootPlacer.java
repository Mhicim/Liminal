package com.example.liminalworld.worldgen;

import com.example.liminalworld.LiminalWorldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class LiminalLootPlacer {

    public static final ResourceLocation LEVEL_0_SUPPLIES =
            new ResourceLocation(LiminalWorldMod.MODID, "chests/level_0_supplies");
    public static final ResourceLocation POOLROOMS_SUPPLIES =
            new ResourceLocation(LiminalWorldMod.MODID, "chests/poolrooms_supplies");
    public static final ResourceLocation DEEP_BRUTALIST_SUPPLIES =
            new ResourceLocation(LiminalWorldMod.MODID, "chests/deep_brutalist_supplies");

    public static void tryPlaceRoomSupplies(
            ChunkAccess chunk,
            int localStartX, int baseY, int localStartZ,
            int worldCellX, int floor, int worldCellZ,
            long seed,
            boolean isDeadEnd,
            String themeName
    ) {
        long stashHash = hashCoords(seed ^ 0x5DEECE66DL, worldCellX, floor + baseY, worldCellZ);
        int chance = Math.abs((int) (stashHash % 100));

        int threshold = isDeadEnd ? 45 : 12;
        if (chance >= threshold) {
            return;
        }

        // Размещаем припасы либо на 1-м ярусе этажа (baseY + 3), либо на антресоли 2-го яруса (baseY + 8)
        boolean placeOnMezzanine = ((stashHash >> 5) & 1) == 1;
        int lootY = placeOnMezzanine ? (baseY + 8) : (baseY + 3);
        int centerX = 2;
        int centerZ = 2;

        // Проверяем, что под контейнером есть твердый пол
        BlockPos floorCheckPos = new BlockPos(
                chunk.getPos().getMinBlockX() + centerX,
                lootY - 1,
                chunk.getPos().getMinBlockZ() + centerZ
        );
        if (chunk.getBlockState(floorCheckPos).isAir()) {
            lootY = baseY + 3;
        }

        BlockPos palletPos = new BlockPos(
                chunk.getPos().getMinBlockX() + centerX,
                lootY,
                chunk.getPos().getMinBlockZ() + centerZ + 1
        );
        chunk.setBlockState(palletPos, Blocks.SCAFFOLDING.defaultBlockState(), false);

        if ((stashHash & 1) == 0) {
            BlockPos lanternPos = palletPos.above();
            chunk.setBlockState(lanternPos, Blocks.LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING, false), false);
        }

        BlockPos barrelPos = new BlockPos(
                chunk.getPos().getMinBlockX() + centerX,
                lootY,
                chunk.getPos().getMinBlockZ() + centerZ
        );
        placeLootBarrel(chunk, barrelPos, selectCustomLootTable(themeName, baseY), stashHash);

        if (isDeadEnd || (Math.abs(stashHash >> 8) % 100) < 45) {
            BlockPos chestPos = new BlockPos(
                    chunk.getPos().getMinBlockX() + centerX - 1,
                    lootY,
                    chunk.getPos().getMinBlockZ() + centerZ
            );
            placeModCompatibleChest(chunk, chestPos, selectVanillaHookTable(themeName, baseY), stashHash ^ 0x12345678L);
        }
    }

    private static void placeLootBarrel(ChunkAccess chunk, BlockPos worldPos, ResourceLocation lootTable, long lootSeed) {
        BlockState barrelState = Blocks.BARREL.defaultBlockState()
                .setValue(BarrelBlock.FACING, Direction.UP);
        chunk.setBlockState(worldPos, barrelState, false);

        BarrelBlockEntity barrelEntity = new BarrelBlockEntity(worldPos, barrelState);
        barrelEntity.setLootTable(lootTable, lootSeed);
        chunk.setBlockEntity(barrelEntity);
    }

    private static void placeModCompatibleChest(ChunkAccess chunk, BlockPos worldPos, ResourceLocation lootTable, long lootSeed) {
        BlockState chestState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, Direction.SOUTH);
        chunk.setBlockState(worldPos, chestState, false);

        ChestBlockEntity chestEntity = new ChestBlockEntity(worldPos, chestState);
        chestEntity.setLootTable(lootTable, lootSeed);
        chunk.setBlockEntity(chestEntity);
    }

    private static ResourceLocation selectCustomLootTable(String themeName, int y) {
        if (y < 0 || "INDUSTRIAL_PIPES".equals(themeName) || "ANOMALOUS_RED_HALLS".equals(themeName)) {
            return DEEP_BRUTALIST_SUPPLIES;
        }
        if ("POOLROOMS_AQUEDUCT".equals(themeName)) {
            return POOLROOMS_SUPPLIES;
        }
        return LEVEL_0_SUPPLIES;
    }

    private static ResourceLocation selectVanillaHookTable(String themeName, int y) {
        if (y < -400) {
            return BuiltInLootTables.ANCIENT_CITY;
        }
        if (y < 0) {
            return BuiltInLootTables.STRONGHOLD_CORRIDOR;
        }
        if ("ARCHIVE_CATACOMBS".equals(themeName)) {
            return BuiltInLootTables.STRONGHOLD_LIBRARY;
        }
        if ("POOLROOMS_AQUEDUCT".equals(themeName)) {
            return BuiltInLootTables.UNDERWATER_RUIN_BIG;
        }
        if ("OVERGROWN_CONCRETE".equals(themeName)) {
            return BuiltInLootTables.JUNGLE_TEMPLE;
        }
        return BuiltInLootTables.ABANDONED_MINESHAFT;
    }

    private static long hashCoords(long seed, int x, int y, int z) {
        long h = seed ^ (x * 374761393L) ^ (y * 668265263L) ^ (z * 2147483647L);
        h = (h ^ (h >> 13)) * 1274126177L;
        return h ^ (h >> 16);
    }
}
