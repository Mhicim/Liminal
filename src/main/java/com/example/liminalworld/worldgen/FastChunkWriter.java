package com.example.liminalworld.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class FastChunkWriter {
    public static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final ChunkAccess chunk;
    private final int minBuildHeight;
    private final int maxBuildHeight;
    private final LevelChunkSection[] sections;

    public FastChunkWriter(ChunkAccess chunk) {
        this.chunk = chunk;
        this.minBuildHeight = chunk.getMinBuildHeight();
        this.maxBuildHeight = chunk.getMaxBuildHeight();
        this.sections = chunk.getSections();
    }

    public int getMinY() {
        return this.minBuildHeight;
    }

    public int getMaxY() {
        return this.maxBuildHeight;
    }

    public ChunkAccess getChunk() {
        return this.chunk;
    }

    public void clearVanillaTerrain() {
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section != null && !section.hasOnlyAir()) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            section.setBlockState(x, y, z, AIR, false);
                        }
                    }
                }
            }
        }
    }

    public void set(int localX, int y, int localZ, BlockState state) {
        if (localX < 0 || localX > 15 || localZ < 0 || localZ > 15) return;
        if (y < minBuildHeight || y >= maxBuildHeight) return;

        int sectionIndex = (y - minBuildHeight) >> 4;
        if (sectionIndex < 0 || sectionIndex >= sections.length) return;

        LevelChunkSection section = sections[sectionIndex];
        if (section == null) return;
        if (state == AIR && section.hasOnlyAir()) return;

        section.setBlockState(localX, y & 15, localZ, state, false);
    }

    public BlockState get(int localX, int y, int localZ) {
        if (localX < 0 || localX > 15 || localZ < 0 || localZ > 15) return AIR;
        if (y < minBuildHeight || y >= maxBuildHeight) return AIR;

        int sectionIndex = (y - minBuildHeight) >> 4;
        if (sectionIndex < 0 || sectionIndex >= sections.length) return AIR;

        LevelChunkSection section = sections[sectionIndex];
        if (section == null || section.hasOnlyAir()) return AIR;

        return section.getBlockState(localX, y & 15, localZ);
    }

    public void fillBox(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        int minX = Math.max(0, Math.min(x1, x2));
        int maxX = Math.min(15, Math.max(x1, x2));
        int minZ = Math.max(0, Math.min(z1, z2));
        int maxZ = Math.min(15, Math.max(z1, z2));
        int minY = Math.max(minBuildHeight, Math.min(y1, y2));
        int maxY = Math.min(maxBuildHeight - 1, Math.max(y1, y2));

        for (int y = minY; y <= maxY; y++) {
            int sectionIndex = (y - minBuildHeight) >> 4;
            LevelChunkSection section = sections[sectionIndex];
            if (section == null) continue;
            if (state == AIR && section.hasOnlyAir()) continue;
            int localY = y & 15;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    section.setBlockState(x, localY, z, state, false);
                }
            }
        }
    }
}
