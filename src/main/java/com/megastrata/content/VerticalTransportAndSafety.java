package com.megastrata.content;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VerticalTransportAndSafety {

    public static class SafetyHarnessItem extends Item {
        public SafetyHarnessItem(Properties props) {
            super(props.stacksTo(1).durability(512));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (hasSafetyHarness(player) && player.fallDistance > 6.0f) {
            BlockPos pos = player.blockPosition();
            boolean nearLedge = !player.level().isEmptyBlock(pos.north()) || !player.level().isEmptyBlock(pos.south())
                    || !player.level().isEmptyBlock(pos.east()) || !player.level().isEmptyBlock(pos.west());

            if (nearLedge && player.isShiftKeyDown()) {
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x * 0.2, -0.08, motion.z * 0.2);
                player.fallDistance = 0.0f;
                player.displayClientMessage(Component.literal("[Страховка]: Зацеп за панельный выступ активен").withStyle(ChatFormatting.GREEN), true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && hasSafetyHarness(player)) {
            event.setDamageMultiplier(0.15f);
            ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
            if (offhand.is(ModRegistry.SAFETY_HARNESS.get())) {
                offhand.hurtAndBreak(4, player, p -> p.broadcastBreakEvent(EquipmentSlot.OFFHAND));
            }
        }
    }

    private static boolean hasSafetyHarness(Player player) {
        return player.getOffhandItem().is(ModRegistry.SAFETY_HARNESS.get())
                || player.getMainHandItem().is(ModRegistry.SAFETY_HARNESS.get());
    }
}
