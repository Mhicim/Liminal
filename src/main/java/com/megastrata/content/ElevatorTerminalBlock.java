package com.megastrata.content;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ElevatorTerminalBlock extends Block {
    public static final BooleanProperty REPAIRED = BooleanProperty.create("repaired");

    public ElevatorTerminalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(REPAIRED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(REPAIRED);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (!state.getValue(REPAIRED)) {
            ItemStack held = player.getItemInHand(hand);
            if (held.is(ModRegistry.ENERGY_CELL.get())) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.setBlock(pos, state.setValue(REPAIRED, true), 3);
                level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
                player.displayClientMessage(Component.literal("Лифтовой узел восстановлен и подключён к магистрали.").withStyle(ChatFormatting.GREEN), true);
            } else {
                player.displayClientMessage(Component.literal("Узел обесточен. Требуется Энергетическая ячейка для запуска.").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.CONSUME;
        }

        int targetY = player.isShiftKeyDown() ? pos.getY() - 64 : pos.getY() + 64;
        if (targetY >= level.getMinBuildHeight() && targetY < level.getMaxBuildHeight()) {
            player.teleportTo(pos.getX() + 0.5, targetY + 1.0, pos.getZ() + 0.5);
            level.playSound(null, pos.atY(targetY), SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 0.6f);
            player.displayClientMessage(Component.literal("Перемещение на отметку Y = " + targetY).withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.CONSUME;
    }
}
