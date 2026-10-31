package com.megastrata.world;

import java.util.Random;

public record LayerProfile(
        MegastructureType type,
        Form form,
        MaterialPalette material,
        State state,
        Lighting lighting,
        int baseMinY,
        int baseMaxY,
        long sectorSeed
) {
    public enum Form { VERTICAL, HORIZONTAL, RING, RADIAL, FRAGMENTED }
    public enum MaterialPalette { CONCRETE, METAL, CERAMIC, BIOMECH, ANCIENT_STONE }
    public enum State { FUNCTIONAL, ABANDONED, FLOODED, RUINED, REPOPULATED }
    public enum Lighting { NATURAL_SHAFTS, ARTIFICIAL, EMERGENCY_RED, PITCH_DARK }

    public static LayerProfile generateDeterministic(long worldSeed, int sectorX, int sectorZ, int verticalIndex) {
        long combinedSeed = worldSeed ^ (sectorX * 341873128712L) ^ (sectorZ * 132897987541L) ^ (verticalIndex * 42317861L);
        Random rand = new Random(combinedSeed);

        int yCenter = -1600 + (verticalIndex * 800);
        MegastructureType selectedType;

        if (yCenter <= -1400) {
            selectedType = MegastructureType.ZERO_LAYER;
        } else if (yCenter >= 1000) {
            selectedType = rand.nextBoolean() ? MegastructureType.HORIZONTAL_SKIES : MegastructureType.ENDLESS_FRAME;
        } else {
            MegastructureType[] midTypes = {
                MegastructureType.ENDLESS_FRAME,
                MegastructureType.VERTICAL_HORIZON_CITY,
                MegastructureType.DEAD_OCEAN,
                MegastructureType.INDUSTRIAL_CONTINENT,
                MegastructureType.BIOMECHANICAL_FOREST,
                MegastructureType.BURIED_ARCHIVE
            };
            selectedType = midTypes[rand.nextInt(midTypes.length)];
        }

        Form form = Form.values()[rand.nextInt(Form.values().length)];
        MaterialPalette mat = switch (selectedType) {
            case ZERO_LAYER -> MaterialPalette.ANCIENT_STONE;
            case INDUSTRIAL_CONTINENT -> MaterialPalette.METAL;
            case BIOMECHANICAL_FOREST -> MaterialPalette.BIOMECH;
            case BURIED_ARCHIVE -> MaterialPalette.CERAMIC;
            default -> MaterialPalette.CONCRETE;
        };
        State state = (selectedType == MegastructureType.DEAD_OCEAN) ? State.FLOODED : State.values()[rand.nextInt(State.values().length)];
        Lighting light = (selectedType == MegastructureType.ZERO_LAYER) ? Lighting.PITCH_DARK : Lighting.values()[rand.nextInt(Lighting.values().length)];

        return new LayerProfile(selectedType, form, mat, state, light, yCenter - 350, yCenter + 350, combinedSeed);
    }
}
