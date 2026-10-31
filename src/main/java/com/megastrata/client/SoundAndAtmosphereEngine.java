package com.megastrata.client;

import com.megastrata.world.LayerProfile;
import com.megastrata.world.MegastructurePlanner;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoundAndAtmosphereEngine {
    private static final Random RAND = new Random();

    // Постоянный холодный белый туман как в первой версии и на концептах BLAME! / Girls' Last Tour
    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.dimension().location().getNamespace().equals("megastrata")) {
            event.setRed(0.86f);
            event.setGreen(0.89f);
            event.setBlue(0.92f);
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.dimension().location().getNamespace().equals("megastrata")) {
            event.setNearPlaneDistance(12.0f);
            event.setFarPlaneDistance(112.0f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || mc.isPaused()) return;
        if (!mc.level.dimension().location().getNamespace().equals("megastrata")) return;

        BlockPos pos = player.blockPosition();
        LayerProfile profile = MegastructurePlanner.getLayerAt(1337420L, pos.getX(), pos.getY(), pos.getZ());

        if (profile.lighting() == LayerProfile.Lighting.PITCH_DARK) {
            mc.getMusicManager().stopPlaying();
        }

        if (player.tickCount % 160 == 0 && RAND.nextFloat() < 0.4f) {
            mc.level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.AMBIENT, 0.25f, 0.5f, false);
        }

        if (player.tickCount % 2400 == 0 && RAND.nextFloat() < 0.25f) {
            int eventType = RAND.nextInt(3);
            switch (eventType) {
                case 0 -> mc.level.playLocalSound(pos.getX() + 80, pos.getY() - 40, pos.getZ() + 80,
                        SoundEvents.ANVIL_LAND, SoundSource.AMBIENT, 0.8f, 0.3f, true);
                case 1 -> player.displayClientMessage(Component.literal(
                        "[Дальний сектор]: Сквозь белый туман вдалеке медленно движется колоссальный силуэт между панельными корпусами..."
                ).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), true);
                case 2 -> mc.level.playLocalSound(pos.getX() - 60, pos.getY(), pos.getZ() - 60,
                        SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.AMBIENT, 0.6f, 0.4f, true);
            }
        }
    }
}
