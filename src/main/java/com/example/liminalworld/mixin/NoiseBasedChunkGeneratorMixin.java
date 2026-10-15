package com.example.liminalworld.mixin;

import com.example.liminalworld.worldgen.MegastructureGenerator;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    @Shadow
    @Final
    private Holder<NoiseGeneratorSettings> settings;

    @Inject(method = "buildSurface", at = @At("RETURN"))
    private void liminalworld$onBuildSurface(
            WorldGenRegion level,
            StructureManager structureManager,
            RandomState random,
            ChunkAccess chunk,
            CallbackInfo ci
    ) {
        if (this.settings.is(NoiseGeneratorSettings.OVERWORLD)) {
            MegastructureGenerator.buildChunk(chunk, level.getSeed());
        }
    }

    @Inject(method = "applyCarvers", at = @At("HEAD"), cancellable = true)
    private void liminalworld$disableVanillaCaves(
            WorldGenRegion level,
            long seed,
            RandomState random,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk,
            GenerationStep.Carving step,
            CallbackInfo ci
    ) {
        if (this.settings.is(NoiseGeneratorSettings.OVERWORLD)) {
            ci.cancel();
        }
    }

    @Inject(method = "getBaseHeight", at = @At("HEAD"), cancellable = true)
    private void liminalworld$adjustBaseHeight(
            int x, int z,
            Heightmap.Types heightmap,
            LevelHeightAccessor level,
            RandomState random,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (this.settings.is(NoiseGeneratorSettings.OVERWORLD)) {
            cir.setReturnValue(MegastructureGenerator.MAX_Y + 1);
        }
    }
}
