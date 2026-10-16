package com.example.liminalworld.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AbyssLayerManager {

    public static final int TOTAL_LAYERS = 7;
    public static final int BIOMES_PER_LAYER = 10;
    public static final int STRUCTURES_PER_LAYER = 10;
    public static final int MOBS_PER_LAYER = 20;

    // Высота Бедроковой комнаты спавна на самом верху Бездны
    public static final int BEDROCK_VAULT_Y = 1920;

    private static long cachedSeed = Long.MIN_VALUE;
    private static final Map<Integer, List<Holder<Biome>>> LAYER_BIOMES = new HashMap<>();
    private static final Map<Integer, List<Holder<Structure>>> LAYER_STRUCTURES = new HashMap<>();
    private static final Map<Integer, List<EntityType<?>>> LAYER_MOBS = new HashMap<>();

    /**
     * Определяет номер Слоя Бездны (1..7) по вертикальной координате Y (от +1920 до -1968)
     */
    public static int getLayerForY(int y) {
        if (y >= 1400) return 1; // Слой 1: Край Бездны (+1400 .. +1920)
        if (y >= 850)  return 2; // Слой 2: Лес Искушений и Перевернутый Лес (+850 .. +1400)
        if (y >= 300)  return 3; // Слой 3: Великий Разлом (+300 .. +850)
        if (y >= -250) return 4; // Слой 4: Кубки Гигантов (-250 .. +300)
        if (y >= -800) return 5; // Слой 5: Море Трупов (-800 .. -250)
        if (y >= -1400) return 6; // Слой 6: Столица Невернувшихся (-1400 .. -800)
        return 7;                // Слой 7: Финальный Водоворот (-1968 .. -1400)
    }

    public static int getLayerFloorY(int layer) {
        return switch (layer) {
            case 1 -> 1400;
            case 2 -> 850;
            case 3 -> 300;
            case 4 -> -250;
            case 5 -> -800;
            case 6 -> -1400;
            default -> -1960;
        };
    }

    public static int getLayerCeilingY(int layer) {
        return switch (layer) {
            case 1 -> 1916;
            case 2 -> 1400;
            case 3 -> 850;
            case 4 -> 300;
            case 5 -> -250;
            case 6 -> -800;
            default -> -1400;
        };
    }

    public static String getLayerName(int layer) {
        return switch (layer) {
            case 1 -> "Слой 1: Край Бездны (Edge of the Abyss)";
            case 2 -> "Слой 2: Лес Искушений (Forest of Temptation)";
            case 3 -> "Слой 3: Великий Разлом (The Great Fault)";
            case 4 -> "Слой 4: Кубки Гигантов (The Goblets of Giants)";
            case 5 -> "Слой 5: Море Трупов (Sea of Corpses)";
            case 6 -> "Слой 6: Столица Невернувшихся (Capital of the Unreturned)";
            default -> "Слой 7: Финальный Водоворот (The Final Maelstrom)";
        };
    }

    public static String getLayerCurseDescription(int layer) {
        return switch (layer) {
            case 1 -> "Легкое головокружение и тошнота";
            case 2 -> "Сильная тошнота, головная боль и онемение конечностей";
            case 3 -> "Пространственные галлюцинации и слуховые искажения";
            case 4 -> "Острейшая боль во всем теле и внутреннее кровотечение";
            case 5 -> "Полная потеря всех органов чувств и дезориентация";
            case 6 -> "Утрата человечности или смерть (Искажение 6-го слоя)";
            default -> "Необратимое поглощение Финальным Водоворотом";
        };
    }

    /**
     * Инициализирует для каждого из 7 слоев:
     * - по 10 случайных биомов (ванильных + модовых из Registries.BIOME)
     * - по 10 случайных структур (ванильных + модовых из Registries.STRUCTURE)
     * - по 20 случайных мобов (ванильных + модовых из ForgeRegistries.ENTITY_TYPES)
     */
    public static synchronized void ensureInitialized(RegistryAccess registryAccess, long seed) {
        if (cachedSeed == seed && !LAYER_BIOMES.isEmpty() && !LAYER_STRUCTURES.isEmpty() && !LAYER_MOBS.isEmpty()) {
            return;
        }
        cachedSeed = seed;
        LAYER_BIOMES.clear();
        LAYER_STRUCTURES.clear();
        LAYER_MOBS.clear();

        // 1. Собираем ВСЕ зарегистрированные биомы (ванильные + Biomes O' Plenty, Alex's Caves, Terralith и др.)
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
        List<Holder<Biome>> allBiomes = new ArrayList<>();
        biomeRegistry.holders().forEach(allBiomes::add);

        for (int layer = 1; layer <= TOTAL_LAYERS; layer++) {
            List<Holder<Biome>> shuffledBiomes = new ArrayList<>(allBiomes);
            Collections.shuffle(shuffledBiomes, new Random(seed ^ (layer * 0x9E3779B97F4A7C15L)));
            List<Holder<Biome>> selectedBiomes = new ArrayList<>();
            for (int i = 0; i < BIOMES_PER_LAYER && i < shuffledBiomes.size(); i++) {
                selectedBiomes.add(shuffledBiomes.get(i));
            }
            LAYER_BIOMES.put(layer, selectedBiomes);
        }

        // 2. Собираем ВСЕ зарегистрированные структуры (ванильные + модовые из Registries.STRUCTURE)
        Registry<Structure> structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);
        List<Holder<Structure>> allStructures = new ArrayList<>();
        structureRegistry.holders().forEach(allStructures::add);

        for (int layer = 1; layer <= TOTAL_LAYERS; layer++) {
            List<Holder<Structure>> shuffledStructures = new ArrayList<>(allStructures);
            Collections.shuffle(shuffledStructures, new Random((seed ^ 0x517CC1B727220A95L) + (layer * 7919L)));
            List<Holder<Structure>> selectedStructures = new ArrayList<>();
            for (int i = 0; i < STRUCTURES_PER_LAYER && i < shuffledStructures.size(); i++) {
                selectedStructures.add(shuffledStructures.get(i));
            }
            LAYER_STRUCTURES.put(layer, selectedStructures);
        }

        // 3. Собираем ВСЕХ зарегистрированных мобов (ванильных и из любых модов через ForgeRegistries)
        List<EntityType<?>> allValidMobs = new ArrayList<>();
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (id == null) continue;
            String path = id.getPath();
            if (path.contains("ender_dragon") || path.contains("wither")) continue;

            MobCategory cat = type.getCategory();
            if (cat != MobCategory.MISC) {
                allValidMobs.add(type);
            }
        }

        if (allValidMobs.isEmpty()) {
            allValidMobs.add(EntityType.ZOMBIE);
            allValidMobs.add(EntityType.SKELETON);
            allValidMobs.add(EntityType.SPIDER);
            allValidMobs.add(EntityType.ENDERMAN);
        }

        for (int layer = 1; layer <= TOTAL_LAYERS; layer++) {
            List<EntityType<?>> shuffledMobs = new ArrayList<>(allValidMobs);
            Collections.shuffle(shuffledMobs, new Random((seed ^ 0xCAFEBABEL) + (layer * 31337L)));
            List<EntityType<?>> selectedMobs = new ArrayList<>();
            for (int i = 0; i < MOBS_PER_LAYER && i < shuffledMobs.size(); i++) {
                selectedMobs.add(shuffledMobs.get(i));
            }
            LAYER_MOBS.put(layer, selectedMobs);
        }
    }

    /**
     * Записывает 10 случайных биомов каждого слоя напрямую в 3D-сетку биомов чанка
     */
    public static void applyLayerBiomesToChunk(RegistryAccess registryAccess, long seed, ChunkAccess chunk, RandomState randomState) {
        ensureInitialized(registryAccess, seed);

        chunk.fillBiomesFromNoise((quartX, quartY, quartZ, sampler) -> {
            int blockX = quartX << 2;
            int blockY = quartY << 2;
            int blockZ = quartZ << 2;
            int layer = getLayerForY(blockY);

            List<Holder<Biome>> layerBiomes = LAYER_BIOMES.get(layer);
            if (layerBiomes == null || layerBiomes.isEmpty()) {
                return chunk.getNoiseBiome(quartX, quartY, quartZ);
            }

            int cellX = Math.floorDiv(blockX, 64);
            int cellZ = Math.floorDiv(blockZ, 64);
            long h = hashCoords(seed + layer * 101L, cellX, layer, cellZ);
            int idx = (int) (Math.abs(h) % layerBiomes.size());
            return layerBiomes.get(idx);
        }, randomState.sampler());
    }

    public static List<Holder<Biome>> getBiomesForLayer(RegistryAccess registryAccess, long seed, int layer) {
        ensureInitialized(registryAccess, seed);
        return LAYER_BIOMES.getOrDefault(layer, Collections.emptyList());
    }

    public static List<Holder<Structure>> getStructuresForLayer(RegistryAccess registryAccess, long seed, int layer) {
        ensureInitialized(registryAccess, seed);
        return LAYER_STRUCTURES.getOrDefault(layer, Collections.emptyList());
    }

    public static List<EntityType<?>> getMobsForLayer(RegistryAccess registryAccess, long seed, int layer) {
        ensureInitialized(registryAccess, seed);
        return LAYER_MOBS.getOrDefault(layer, Collections.emptyList());
    }

    public static EntityType<?> pickRandomMobForLayer(RegistryAccess registryAccess, long seed, int layer, long localHash) {
        ensureInitialized(registryAccess, seed);
        List<EntityType<?>> mobs = LAYER_MOBS.get(layer);
        if (mobs == null || mobs.isEmpty()) return EntityType.ZOMBIE;
        int idx = (int) (Math.abs(localHash) % mobs.size());
        return mobs.get(idx);
    }

    public static void placeLayerMobSpawner(RegistryAccess registryAccess, ChunkAccess chunk, int localX, int y, int localZ, long seed, int layer) {
        BlockPos worldPos = new BlockPos(
                chunk.getPos().getMinBlockX() + localX,
                y,
                chunk.getPos().getMinBlockZ() + localZ
        );
        BlockState spawnerState = Blocks.SPAWNER.defaultBlockState();
        chunk.setBlockState(worldPos, spawnerState, false);

        SpawnerBlockEntity spawnerEntity = new SpawnerBlockEntity(worldPos, spawnerState);
        long localHash = hashCoords(seed, worldPos.getX(), y, worldPos.getZ());
        EntityType<?> chosenMob = pickRandomMobForLayer(registryAccess, seed, layer, localHash);
        spawnerEntity.setEntityId(chosenMob, RandomSource.create(localHash));
        chunk.setBlockEntity(spawnerEntity);
    }

    public static long hashCoords(long seed, int x, int y, int z) {
        long h = seed ^ (x * 374761393L) ^ (y * 668265263L) ^ (z * 2147483647L);
        h = (h ^ (h >> 13)) * 1274126177L;
        return h ^ (h >> 16);
    }
}
