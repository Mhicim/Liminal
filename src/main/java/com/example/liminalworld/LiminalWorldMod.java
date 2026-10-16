package com.example.liminalworld;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(LiminalWorldMod.MODID)
public class LiminalWorldMod {
    public static final String MODID = "liminalworld";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LiminalWorldMod() {
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Made in Abyss: Endless Chasm v5.0 (10 Biomes, 10 Structures, 20 Mobs per Layer) Initialized!");
    }
}
