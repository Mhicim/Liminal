package com.example.liminalworld.mixin;

import com.example.liminalworld.worldgen.AbyssLayerStructurePlacer;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {

    @Inject(method = "applyBiomeDecoration", at = @At("RETURN"), require = 0)
    private void liminalworld$onApplyBiomeDecoration(
            WorldGenLevel level,
            ChunkAccess chunk,
            StructureManager structureManager,
            CallbackInfo ci
    ) {
        ChunkGenerator self = (ChunkGenerator) (Object) this;
        if (self instanceof NoiseBasedChunkGenerator) {
            AbyssLayerStructurePlacer.placeLayerStructuresInChunk(level, chunk, structureManager, self);
        }
    }
}
