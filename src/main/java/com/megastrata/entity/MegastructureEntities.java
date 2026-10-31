package com.megastrata.entity;

import com.megastrata.content.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;

public class MegastructureEntities {

    public enum BehaviorState {
        REST,
        FORAGE,
        MOVE,
        LIGHT_REACTION,
        AVOID_DANGER,
        DEFEND_TERRITORY,
        INTERACT,
        ENV_REACTION
    }

    public static abstract class BaseMegastructureMob extends PathfinderMob {
        protected BehaviorState currentState = BehaviorState.MOVE;

        protected BaseMegastructureMob(EntityType<? extends PathfinderMob> type, Level level) {
            super(type, level);
        }

        public BehaviorState getBehaviorState() { return currentState; }

        public static AttributeSupplier.Builder createDefaultAttributes() {
            return Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 24.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.25D)
                    .add(Attributes.FOLLOW_RANGE, 32.0D);
        }
    }

    public static class ServiceDroneEntity extends BaseMegastructureMob {
        public ServiceDroneEntity(EntityType<? extends PathfinderMob> type, Level level) {
            super(type, level);
        }

        @Override
        public void aiStep() {
            super.aiStep();
            if (!level().isClientSide && tickCount % 100 == 0) {
                BlockPos below = blockPosition().below();
                if (level().getBlockState(below).is(Blocks.COBBLESTONE)) {
                    this.currentState = BehaviorState.ENV_REACTION;
                    level().setBlock(below, ModRegistry.STRUCTURAL_CONCRETE.get().defaultBlockState(), 3);
                } else {
                    this.currentState = BehaviorState.MOVE;
                }
            }
        }
    }

    public static class WallClimberEntity extends BaseMegastructureMob {
        public WallClimberEntity(EntityType<? extends PathfinderMob> type, Level level) {
            super(type, level);
        }

        @Override
        public void tick() {
            super.tick();
            if (horizontalCollision) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.22D, this.getDeltaMovement().z);
            }
            int blockLight = level().getBrightness(LightLayer.BLOCK, blockPosition());
            if (blockLight > 9) {
                this.currentState = BehaviorState.LIGHT_REACTION;
            } else if (this.getLastHurtByMob() != null) {
                this.currentState = BehaviorState.AVOID_DANGER;
            } else {
                this.currentState = BehaviorState.FORAGE;
            }
        }
    }

    public static class FilterOrganismEntity extends BaseMegastructureMob {
        public FilterOrganismEntity(EntityType<? extends PathfinderMob> type, Level level) {
            super(type, level);
        }

        @Override
        public void aiStep() {
            super.aiStep();
            if (!level().isClientSide && tickCount % 200 == 0) {
                this.currentState = BehaviorState.ENV_REACTION;
                level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(8.0D))
                        .forEach(p -> p.setAirSupply(Math.min(p.getMaxAirSupply(), p.getAirSupply() + 60)));
            }
        }
    }
}
