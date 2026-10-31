package com.megastrata.world;

import com.megastrata.content.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Random;

public class HudAndMysterySystem {

    public static final IGuiOverlay MEGASTRATA_HUD = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.level.dimension().location().getNamespace().equals("megastrata")) return;

        BlockPos pos = mc.player.blockPosition();
        LayerProfile profile = MegastructurePlanner.getLayerAt(1337420L, pos.getX(), pos.getY(), pos.getZ());
        boolean inSnowGap = MegastructurePlanner.isSnowyPlainTransitionZone(pos.getX(), pos.getZ());

        String heightText = String.format("Высота Y: %+d м", pos.getY());
        String sectorText = inSnowGap
                ? "Сектор: ПЕРЕХОДНАЯ СНЕЖНАЯ РАВНИНА [МЕЖКОРПУСНЫЙ БУФЕР]"
                : "Сектор: ГИГАХРУЩ / " + profile.type().id.toUpperCase() + " [" + profile.state().name() + "]";
        guiGraphics.drawString(mc.font, heightText, 10, 10, 0xE0E0E0, true);
        guiGraphics.drawString(mc.font, sectorText, 10, 22, 0x80C0FF, true);

        if (profile.type() == MegastructureType.ZERO_LAYER || profile.state() == LayerProfile.State.FLOODED) {
            guiGraphics.drawString(mc.font, "! ВНИМАНИЕ: Агрессивная среда сектора", 10, 34, 0xFF5555, true);
        }
    };

    public static void tickAutonomousSelfMaintenance(ServerLevel level, ServerPlayer player) {
        if (level.getGameTime() % 1200 != 0) return;
        Random rand = new Random(level.getGameTime() ^ player.getUUID().getLeastSignificantBits());
        if (rand.nextFloat() > 0.15f) return;

        BlockPos target = player.blockPosition().offset(
                (rand.nextBoolean() ? 1 : -1) * (24 + rand.nextInt(24)),
                rand.nextInt(16) - 8,
                (rand.nextBoolean() ? 1 : -1) * (24 + rand.nextInt(24))
        );

        if (level.getBlockState(target).is(Blocks.IRON_BARS) || level.getBlockState(target).is(Blocks.COBBLESTONE)) {
            level.setBlock(target, ModRegistry.STRUCTURAL_CONCRETE.get().defaultBlockState(), 3);
        }
    }
}
