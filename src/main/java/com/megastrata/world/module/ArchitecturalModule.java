package com.megastrata.world.module;

import com.megastrata.world.LayerProfile;
import com.megastrata.world.MegastructureType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class ArchitecturalModule {
    public enum ModuleConfigState {
        WORKING,
        ABANDONED,
        FLOODED,
        RUINED,
        OVERGROWN,
        REBUILT
    }

    public enum ScaleLevel {
        HUMAN,
        INDUSTRIAL,
        MONUMENTAL
    }

    public final String moduleId;
    public final int minSize;
    public final int maxSize;
    public final ScaleLevel scaleLevel;
    public final EnumSet<MegastructureType> compatibleLayers;
    public final List<Direction> connectors;
    public final float damageFactor;
    public final boolean allowsRareAnomalies;

    public ArchitecturalModule(String moduleId, int minSize, int maxSize, ScaleLevel scaleLevel,
                               EnumSet<MegastructureType> compatibleLayers, List<Direction> connectors,
                               float damageFactor, boolean allowsRareAnomalies) {
        this.moduleId = moduleId;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.scaleLevel = scaleLevel;
        this.compatibleLayers = compatibleLayers;
        this.connectors = connectors;
        this.damageFactor = damageFactor;
        this.allowsRareAnomalies = allowsRareAnomalies;
    }

    public ModuleInstance instantiate(long seed, BlockPos origin, LayerProfile profile) {
        Random rand = new Random(seed ^ origin.asLong());
        ModuleConfigState configState = switch (profile.state()) {
            case FUNCTIONAL -> ModuleConfigState.WORKING;
            case FLOODED -> ModuleConfigState.FLOODED;
            case RUINED -> ModuleConfigState.RUINED;
            case REPOPULATED -> ModuleConfigState.REBUILT;
            default -> rand.nextBoolean() ? ModuleConfigState.ABANDONED : ModuleConfigState.OVERGROWN;
        };
        Rotation rotation = Rotation.values()[rand.nextInt(Rotation.values().length)];
        Mirror mirror = Mirror.values()[rand.nextInt(Mirror.values().length)];
        int actualSize = minSize + rand.nextInt(Math.max(1, maxSize - minSize));

        return new ModuleInstance(this, origin, actualSize, configState, rotation, mirror);
    }

    public record ModuleInstance(
            ArchitecturalModule template,
            BlockPos origin,
            int size,
            ModuleConfigState state,
            Rotation rotation,
            Mirror mirror
    ) {}
}
