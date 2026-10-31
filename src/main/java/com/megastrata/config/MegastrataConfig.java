package com.megastrata.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class MegastrataConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public enum Density { RARE, BALANCED, DENSE }
    public enum HazardLevel { SAFE, ATMOSPHERIC, HIGH }
    public enum TransportMode { MANUAL, MIXED, AUTOMATED }
    public enum SurfaceConnection { RARE, MODERATE, FREQUENT }
    public enum EcologyDensity { SPARSE, BALANCED, DENSE }

    public static final ForgeConfigSpec.EnumValue<Density> MEGASTRUCTURE_DENSITY;
    public static final ForgeConfigSpec.EnumValue<HazardLevel> HAZARD_LEVEL;
    public static final ForgeConfigSpec.EnumValue<TransportMode> TRANSPORT_NETWORK;
    public static final ForgeConfigSpec.EnumValue<SurfaceConnection> SURFACE_CONNECTION;
    public static final ForgeConfigSpec.EnumValue<EcologyDensity> ECOLOGY_DENSITY;
    public static final ForgeConfigSpec.IntValue MAX_ACTIVE_MECHANISMS_PER_SECTOR;

    static {
        BUILDER.push("World Profile & Generation");

        MEGASTRUCTURE_DENSITY = BUILDER
                .comment("Megastructure Density: RARE, BALANCED, DENSE")
                .defineEnum("megastructureDensity", Density.BALANCED);

        HAZARD_LEVEL = BUILDER
                .comment("Hazard Level: SAFE, ATMOSPHERIC, HIGH")
                .defineEnum("hazardLevel", HazardLevel.ATMOSPHERIC);

        TRANSPORT_NETWORK = BUILDER
                .comment("Transport Network: MANUAL, MIXED, AUTOMATED")
                .defineEnum("transportNetwork", TransportMode.MIXED);

        SURFACE_CONNECTION = BUILDER
                .comment("Surface Connection: RARE, MODERATE, FREQUENT")
                .defineEnum("surfaceConnection", SurfaceConnection.RARE);

        ECOLOGY_DENSITY = BUILDER
                .comment("Ecology Density: SPARSE, BALANCED, DENSE")
                .defineEnum("ecologyDensity", EcologyDensity.BALANCED);

        BUILDER.pop();
        BUILDER.push("Performance Controller");

        MAX_ACTIVE_MECHANISMS_PER_SECTOR = BUILDER
                .comment("Maximum active mechanisms per sector")
                .defineInRange("maxActiveMechanisms", 32, 4, 256);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
