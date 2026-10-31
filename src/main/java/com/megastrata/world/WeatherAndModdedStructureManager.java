package com.megastrata.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WeatherAndModdedStructureManager {

    private static final Set<Long> PROCESSED_STRUCTURE_CHUNKS = new HashSet<>();

    // Структуры из Верхнего мира, которые периодически спавнятся на огромных переходных снежных равнинах между зданиями Гигахруща
    private static final ResourceLocation[] OVERWORLD_TEMPLATES = new ResourceLocation[] {
        new ResourceLocation("minecraft", "igloo/top"),
        new ResourceLocation("minecraft", "pillager_outpost/watchtower"),
        new ResourceLocation("minecraft", "ruined_portal/portal_1"),
        new ResourceLocation("minecraft", "village/snowy/houses/snowy_small_house_1"),
        new ResourceLocation("minecraft", "village/snowy/houses/snowy_medium_house_1"),
        new ResourceLocation("minecraft", "village/taiga/houses/taiga_medium_house_1")
    };

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;
        if (!(event.level instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().location().getNamespace().equals("megastrata")) return;

        for (ServerPlayer player : serverLevel.players()) {
            HudAndMysterySystem.tickAutonomousSelfMaintenance(serverLevel, player);

            if (serverLevel.getGameTime() % 160 == 0) {
                ChunkPos cp = player.chunkPosition();
                int bx = cp.getMinBlockX();
                int bz = cp.getMinBlockZ();
                // Спавним структуры Верхнего мира именно на переходных Снежных Равнинах между зданиями
                if (MegastructurePlanner.isSnowyPlainTransitionZone(bx, bz)
                        && Math.floorMod(cp.x, 8) == 0 && Math.floorMod(cp.z, 8) == 0
                        && PROCESSED_STRUCTURE_CHUNKS.add(cp.toLong())) {
                    tryPlaceOverworldStructureOnSnowyPlain(serverLevel, cp);
                }
            }
        }
    }

    private static void tryPlaceOverworldStructureOnSnowyPlain(ServerLevel level, ChunkPos chunkPos) {
        try {
            StructureTemplateManager manager = level.getStructureManager();
            Random rand = new Random(level.getSeed() ^ chunkPos.toLong());
            ResourceLocation templateId = OVERWORLD_TEMPLATES[rand.nextInt(OVERWORLD_TEMPLATES.length)];
            Optional<StructureTemplate> templateOpt = manager.get(templateId);

            if (templateOpt.isPresent()) {
                // Размещаем на уровнях снежных равнин между корпусами (Y = -400, Y = 0, Y = +400, Y = +800)
                int[] plainLevels = { -392, 8, 408, 808 };
                int targetY = plainLevels[rand.nextInt(plainLevels.length)];
                BlockPos placePos = new BlockPos(chunkPos.getMinBlockX() + 3, targetY, chunkPos.getMinBlockZ() + 3);
                templateOpt.get().placeInWorld(level, placePos, placePos, new StructurePlaceSettings(), level.random, 2);
            }
        } catch (Exception ignored) {
        }
    }
}
