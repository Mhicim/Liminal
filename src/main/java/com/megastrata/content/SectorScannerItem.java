package com.megastrata.content;

import com.megastrata.world.LayerProfile;
import com.megastrata.world.MegastructurePlanner;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SectorScannerItem extends Item {
    public SectorScannerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            BlockPos pos = player.blockPosition();
            LayerProfile profile = MegastructurePlanner.getLayerAt(1337420L, pos.getX(), pos.getY(), pos.getZ());
            boolean inSnowyGap = MegastructurePlanner.isSnowyPlainTransitionZone(pos.getX(), pos.getZ());

            player.displayClientMessage(Component.literal("=== [СКАНЕР СЕКТОРА MEGASTRATA] ===").withStyle(ChatFormatting.GRAY), false);
            player.displayClientMessage(Component.literal("Высота (Y): " + pos.getY() + " м").withStyle(ChatFormatting.WHITE), false);
            player.displayClientMessage(Component.literal("Тип комплекса: " + profile.type().id.toUpperCase()).withStyle(ChatFormatting.AQUA), false);
            player.displayClientMessage(Component.literal("Состояние: " + profile.state().name() + " | Материал: " + profile.material().name()).withStyle(ChatFormatting.GRAY), false);

            if (inSnowyGap) {
                player.displayClientMessage(Component.literal("ЗОНА: Межкорпусная Снежная Равнина (переход между блоками Гигахруща)").withStyle(ChatFormatting.WHITE), false);
            } else {
                player.displayClientMessage(Component.literal("ЗОНА: Внутренний массив Панельного Гигахруща и Мегаструктуры").withStyle(ChatFormatting.YELLOW), false);
            }
            player.getCooldowns().addCooldown(this, 60);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
