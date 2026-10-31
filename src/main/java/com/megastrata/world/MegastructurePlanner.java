package com.megastrata.world;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MegastructurePlanner {
    private static final int SECTOR_SIZE = 2048;
    private static final Map<Long, LayerProfile> CACHE = new ConcurrentHashMap<>();

    public static LayerProfile getLayerAt(long worldSeed, int blockX, int blockY, int blockZ) {
        int sectorX = Math.floorDiv(blockX, SECTOR_SIZE);
        int sectorZ = Math.floorDiv(blockZ, SECTOR_SIZE);
        int rawIndex = Math.floorDiv(blockY + 2000, 800);
        int verticalIndex = Math.max(0, Math.min(4, rawIndex));

        long key = ((long) sectorX & 0xFFFFFFL) | (((long) sectorZ & 0xFFFFFFL) << 24) | ((long) verticalIndex << 48);
        return CACHE.computeIfAbsent(key, k -> LayerProfile.generateDeterministic(worldSeed, sectorX, sectorZ, verticalIndex));
    }

    public static boolean isTransitionZone(int blockY) {
        int localY = Math.floorMod(blockY + 2000, 800);
        return localY < 50 || localY > 750;
    }

    // Определяет, находится ли координата (X, Z) в огромной переходной Снежной Равнине между массивами зданий Гигахруща.
    // Каждые 1024 блока: 560 блоков занимает колоссальный массив Гигахруща/Мегаструктур, а 464 блока — широкая Снежная Равнина между зданиями!
    public static boolean isSnowyPlainTransitionZone(int blockX, int blockZ) {
        int modX = Math.floorMod(blockX, 1024);
        int modZ = Math.floorMod(blockZ, 1024);
        return modX >= 560 || modZ >= 560;
    }
}
