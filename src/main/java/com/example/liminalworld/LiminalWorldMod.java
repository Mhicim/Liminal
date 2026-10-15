package com.example.liminalworld;

import com.example.liminalworld.worldgen.MegastructureGenerator;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(LiminalWorldMod.MODID)
public class LiminalWorldMod {
    public static final String MODID = "liminalworld";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LiminalWorldMod() {
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Liminal Megastructure Worldgen Initialized!");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension() == Level.OVERWORLD) {
                CompoundTag data = player.getPersistentData();
                if (!data.getBoolean("liminal_spawned")) {
                    data.putBoolean("liminal_spawned", true);
                    // Телепортируем игрока внутрь безопасного этажа мегаструктуры (Y = 67)
                    int targetY = MegastructureGenerator.MIN_Y + (15 * MegastructureGenerator.FLOOR_HEIGHT) + 3;
                    player.teleportTo(
                            player.serverLevel(),
                            player.getX(),
                            targetY,
                            player.getZ(),
                            player.getYRot(),
                            player.getXRot()
                    );
                }
            }
        }
    }
}
