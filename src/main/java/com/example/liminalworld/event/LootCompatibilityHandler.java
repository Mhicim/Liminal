package com.example.liminalworld.event;

import com.example.liminalworld.LiminalWorldMod;
import com.example.liminalworld.worldgen.LiminalLootPlacer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LiminalWorldMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootCompatibilityHandler {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();

        if (name.equals(new ResourceLocation("minecraft", "chests/simple_dungeon")) ||
            name.equals(new ResourceLocation("minecraft", "chests/abandoned_mineshaft"))) {

            LootPool liminalBonusPool = LootPool.lootPool()
                    .name("liminalworld_injected_supplies")
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootTableReference.lootTableReference(LiminalLootPlacer.LEVEL_0_SUPPLIES).setWeight(1))
                    .build();

            event.getTable().addPool(liminalBonusPool);
        }
    }
}
