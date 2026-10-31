package com.megastrata.compat;

import com.megastrata.content.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShelterAndIntegrationSystem {

    public enum MaidRole {
        SHELTER_CARETAKER,
        TECHNICAL_ASSISTANT,
        EXPEDITION_COMPANION
    }

    public static void onPlayerReturnToShelter(Level level, Player player, BlockPos shelterPos, MaidRole activeRole) {
        if (level.isClientSide) return;

        boolean hasKettle = false;
        boolean hasLamp = false;
        for (BlockPos p : BlockPos.betweenClosed(shelterPos.offset(-6, -2, -6), shelterPos.offset(6, 4, 6))) {
            if (level.getBlockState(p).is(ModRegistry.KETTLE_BLOCK.get())) hasKettle = true;
            if (level.getBlockState(p).is(ModRegistry.PORTABLE_LAMP.get())) hasLamp = true;
        }

        if (hasKettle && hasLamp) {
            player.heal(6.0f);
            player.getFoodData().eat(4, 0.5f);
            player.displayClientMessage(Component.literal(
                    "[Убежище]: В квартире Гигахруща тепло, горит лампа и заварен чай. Помощница (" + activeRole.name() + ") поддерживала порядок."
            ).withStyle(ChatFormatting.GOLD), false);
        }
    }

    public static boolean canPlaceMegastructureEntranceInOverworld(Holder<Biome> biomeHolder) {
        return biomeHolder.unwrapKey()
                .map(key -> !key.location().getNamespace().equals("alexscaves"))
                .orElse(true);
    }

    public static class MegastructureEnergyAdapter implements ICapabilityProvider {
        private final EnergyStorage storage = new EnergyStorage(50000, 1000, 1000);
        private final LazyOptional<IEnergyStorage> optional = LazyOptional.of(() -> storage);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return ForgeCapabilities.ENERGY.orEmpty(cap, optional);
        }
    }
}
