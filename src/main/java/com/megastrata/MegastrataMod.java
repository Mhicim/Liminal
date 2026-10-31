package com.megastrata;

import com.megastrata.compat.MaidIntegrationLayer;
import com.megastrata.config.MegastrataConfig;
import com.megastrata.content.ModRegistry;
import com.megastrata.entity.MegastructureEntities;
import com.megastrata.world.HudAndMysterySystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MegastrataMod.MODID)
public class MegastrataMod {
    public static final String MODID = "megastrata";

    public MegastrataMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MegastrataConfig.SPEC);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerAttributes);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(MaidIntegrationLayer::init);
    }

    private void registerAttributes(final EntityAttributeCreationEvent event) {
        event.put(ModRegistry.SERVICE_DRONE.get(), MegastructureEntities.BaseMegastructureMob.createDefaultAttributes().build());
        event.put(ModRegistry.WALL_CLIMBER.get(), MegastructureEntities.BaseMegastructureMob.createDefaultAttributes().build());
        event.put(ModRegistry.FILTER_ORGANISM.get(), MegastructureEntities.BaseMegastructureMob.createDefaultAttributes().build());
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("megastrata_hud", HudAndMysterySystem.MEGASTRATA_HUD);
        }
    }
}
