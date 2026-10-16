package com.example.liminalworld.event;

import com.example.liminalworld.LiminalWorldMod;
import com.example.liminalworld.worldgen.AbyssLayerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = LiminalWorldMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AbyssEventAndCurseHandler {

    /**
     * 1. СПАВН ВСЕГДА НАЧИНАЕТСЯ В БЕДРОКОВОЙ КОМНАТЕ НА ВЕРШИНЕ МИРА (Чанк 0,0, Y = 1922),
     * откуда игроку предстоит спуститься вниз через центральную дыру!
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension() == Level.OVERWORLD) {
                CompoundTag data = player.getPersistentData();
                if (!data.getBoolean("made_in_abyss_spawned_v4")) {
                    data.putBoolean("made_in_abyss_spawned_v4", true);

                    ServerLevel serverLevel = player.serverLevel();
                    // Принудительно загружаем центральный чанк (0, 0), чтобы Бедроковая Комната уже стояла
                    serverLevel.getChunk(0, 0);

                    double spawnX = 3.5;
                    double spawnY = AbyssLayerManager.BEDROCK_VAULT_Y + 2.0; // Y = 1922 (на полу Бедроковой комнаты перед дырой)
                    double spawnZ = 8.5;

                    player.teleportTo(serverLevel, spawnX, spawnY, spawnZ, -90.0F, 25.0F);
                    data.putDouble("abyss_lowest_y", spawnY);
                    data.putInt("abyss_current_layer", 1);

                    // Выдаем Звездный Компас Искателя (при ПКМ показывает 10 биомов и 20 мобов текущего слоя!)
                    ItemStack starCompass = new ItemStack(Items.COMPASS);
                    starCompass.setHoverName(Component.literal("Звездный Компас Бездны (ПКМ — Инфо о Слое)")
                            .withStyle(ChatFormatting.GOLD));
                    player.getInventory().add(starCompass);

                    player.sendSystemMessage(Component.literal("========================================").withStyle(ChatFormatting.DARK_GRAY));
                    player.sendSystemMessage(Component.literal(" Вы очнулись в Бедроковом Бункере над Бездной (Y = 1922).").withStyle(ChatFormatting.GOLD));
                    player.sendSystemMessage(Component.literal(" В центре зала зияет сквозная ДЫРА ВНИЗ — прыгайте в шахту, чтобы начать спуск в 1-й Слой!").withStyle(ChatFormatting.YELLOW));
                    player.sendSystemMessage(Component.literal(" Нажмите ПКМ Звездным Компасом в руке, чтобы узнать 10 биомов, 10 структур и 20 мобов текущего слоя.").withStyle(ChatFormatting.AQUA));
                    player.sendSystemMessage(Component.literal("========================================").withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    /**
     * 2. Ежесекундная обработка:
     * - Оповещение о смене Слоя Бездны (1..7)
     * - Проклятие Бездны (Curse of the Abyss) при подъеме наверх
     * - Динамические События Бездны и спавн 20 случайных мобов каждого слоя
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().dimension() != Level.OVERWORLD) return;
        if (player.tickCount % 20 != 0) return; // Проверка раз в секунду

        ServerLevel level = player.serverLevel();
        double currentY = player.getY();
        int currentLayer = AbyssLayerManager.getLayerForY((int) currentY);

        CompoundTag data = player.getPersistentData();
        int lastLayer = data.getInt("abyss_current_layer");

        // А) Переход между Слоями Бездны
        if (currentLayer != lastLayer) {
            data.putInt("abyss_current_layer", currentLayer);
            announceLayerEntry(player, level, currentLayer);
        }

        // Б) Механика ПРОКЛЯТИЯ БЕЗДНЫ при подъеме вверх (для выживания/приключения)
        if (!player.isCreative() && !player.isSpectator()) {
            double lowestY = data.contains("abyss_lowest_y") ? data.getDouble("abyss_lowest_y") : currentY;
            if (currentY < lowestY) {
                data.putDouble("abyss_lowest_y", currentY);
            } else if (currentY - lowestY >= 16.0) {
                // Игрок поднялся более чем на 16 блоков вверх — срабатывает Проклятие текущего слоя!
                data.putDouble("abyss_lowest_y", currentY);
                applyCurseOfTheAbyss(player, level, currentLayer);
            }
        } else {
            data.putDouble("abyss_lowest_y", currentY);
        }

        // В) Экологический спавн 20 случайных мобов текущего слоя (каждые 14 секунд)
        if (player.tickCount % 280 == 0) {
            trySpawnLayerAssignedMobNearPlayer(player, level, currentLayer);
        }

        // Г) Глобальные События Бездны (каждые 90 секунд)
        if (player.tickCount % 1800 == 0) {
            triggerAbyssalWorldEvent(player, level, currentLayer);
        }
    }

    /**
     * 3. ПКМ Звездным Компасом или Часами выводит список всех 10 случайных биомов и 20 случайных мобов текущего слоя!
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().dimension() != Level.OVERWORLD) return;

        ItemStack stack = event.getItemStack();
        if (stack.is(Items.COMPASS) || stack.is(Items.CLOCK)) {
            ServerLevel level = player.serverLevel();
            int layer = AbyssLayerManager.getLayerForY((int) player.getY());
            long seed = level.getSeed();

            List<Holder<Biome>> biomes = AbyssLayerManager.getBiomesForLayer(level.registryAccess(), seed, layer);
            List<Holder<Structure>> structures = AbyssLayerManager.getStructuresForLayer(level.registryAccess(), seed, layer);
            List<EntityType<?>> mobs = AbyssLayerManager.getMobsForLayer(level.registryAccess(), seed, layer);

            String structureNames = structures.stream()
                    .map(h -> h.unwrapKey().map(k -> k.location().getPath()).orElse("structure"))
                    .collect(Collectors.joining(", "));

            String biomeNames = biomes.stream()
                    .map(h -> h.unwrapKey().map(k -> k.location().getPath()).orElse("unknown"))
                    .collect(Collectors.joining(", "));

            String mobNames = mobs.stream()
                    .map(t -> {
                        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(t);
                        return id != null ? id.getPath() : "mob";
                    })
                    .collect(Collectors.joining(", "));

            player.sendSystemMessage(Component.literal("=== [" + AbyssLayerManager.getLayerName(layer) + "] ===")
                    .withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("Проклятие при подъеме: " + AbyssLayerManager.getLayerCurseDescription(layer))
                    .withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.literal("10 Биомов этого слоя: ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(biomeNames).withStyle(ChatFormatting.WHITE)));
            player.sendSystemMessage(Component.literal("10 Структур этого слоя: ")
                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.literal(structureNames).withStyle(ChatFormatting.WHITE)));
            player.sendSystemMessage(Component.literal("20 Мобов этого слоя: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(mobNames).withStyle(ChatFormatting.WHITE)));
        }
    }

    private static void announceLayerEntry(ServerPlayer player, ServerLevel level, int layer) {
        String title = AbyssLayerManager.getLayerName(layer);
        String curse = AbyssLayerManager.getLayerCurseDescription(layer);

        player.displayClientMessage(
                Component.literal(">>> " + title + " | Проклятие: " + curse + " <<<").withStyle(ChatFormatting.GOLD),
                true
        );
        player.sendSystemMessage(Component.literal("Вы вошли в " + title).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("Эффект подъема: " + curse).withStyle(ChatFormatting.RED));

        level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 1.0F, 0.7F);
    }

    private static void applyCurseOfTheAbyss(ServerPlayer player, ServerLevel level, int layer) {
        player.displayClientMessage(
                Component.literal("⚠ ПРОКЛЯТИЕ БЕЗДНЫ (" + layer + "-й Слой): " + AbyssLayerManager.getLayerCurseDescription(layer))
                        .withStyle(ChatFormatting.DARK_RED),
                true
        );

        level.sendParticles(ParticleTypes.SCULK_SOUL, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.5, 0.8, 0.5, 0.03);
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.2F, 0.8F);

        switch (layer) {
            case 1 -> {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0));
            }
            case 2 -> {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 240, 1));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
            }
            case 3 -> {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 240, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0));
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0));
            }
            case 4 -> {
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
                player.hurt(level.damageSources().magic(), 4.0F);
            }
            case 5 -> {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 180, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 240, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 240, 2));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 240, 2));
            }
            case 6 -> {
                // Искажение 6-го слоя (утрата человечности / Благословение Нарехате): урон + мутация силы/прыгучести
                player.hurt(level.damageSources().magic(), 7.0F);
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 180, 2));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
            }
            default -> {
                player.hurt(level.damageSources().magic(), 10.0F);
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 240, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 300, 0));
            }
        }
    }

    /**
     * Динамически спавнит существ из списка 20 случайных мобов текущего слоя (включая модовых мобов!)
     */
    private static void trySpawnLayerAssignedMobNearPlayer(ServerPlayer player, ServerLevel level, int layer) {
        if (player.getY() >= AbyssLayerManager.BEDROCK_VAULT_Y - 5) return; // Не спавним внутри Бедроковой комнаты

        int dx = (level.random.nextBoolean() ? 1 : -1) * (14 + level.random.nextInt(14));
        int dz = (level.random.nextBoolean() ? 1 : -1) * (14 + level.random.nextInt(14));
        int targetX = player.getBlockX() + dx;
        int targetZ = player.getBlockZ() + dz;
        int baseY = player.getBlockY();

        // Ищем твердую поверхность поблизости по вертикали
        for (int dy = -8; dy <= 8; dy++) {
            BlockPos pos = new BlockPos(targetX, baseY + dy, targetZ);
            if (level.getBlockState(pos.below()).isSolid()
                    && level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.above()).isAir()) {

                long hash = level.random.nextLong();
                EntityType<?> chosenType = AbyssLayerManager.pickRandomMobForLayer(
                        level.registryAccess(), level.getSeed(), layer, hash
                );

                Entity entity = chosenType.create(level);
                if (entity instanceof Mob mob) {
                    mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
                    mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
                    level.addFreshEntity(mob);
                }
                break;
            }
        }
    }

    /**
     * Атмосферные События Бездны
     */
    private static void triggerAbyssalWorldEvent(ServerPlayer player, ServerLevel level, int layer) {
        int eventRoll = level.random.nextInt(3);

        if (eventRoll == 0) {
            // Событие 1: Восходящий поток Силового Поля (Forcefield Updraft)
            player.sendSystemMessage(Component.literal("✦ [Событие Бездны]: Восходящий поток Силового Поля наполняет воздух светящимися спорами и смягчает падение!")
                    .withStyle(ChatFormatting.AQUA));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0));
            level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 2.0, player.getZ(), 45, 4.0, 3.0, 4.0, 0.02);
        } else if (eventRoll == 1) {
            // Событие 2: Дождь Вечных Богатств (Eternal Fortunes)
            player.sendSystemMessage(Component.literal("✿ [Событие Бездны]: С верхних ярусов осыпаются лепестки Вечных Богатств, восстанавливая силы!")
                    .withStyle(ChatFormatting.YELLOW));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 0));
            level.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 6.0, player.getZ(), 60, 5.0, 4.0, 5.0, 0.05);
        } else {
            // Событие 3: Пробуждение Фауны Слоя (Layer Ecological Surge)
            player.sendSystemMessage(Component.literal("⚠ [Событие Бездны]: Гул в глубине " + layer + "-го Слоя! Местная фауна выходит на охоту...")
                    .withStyle(ChatFormatting.GOLD));
            trySpawnLayerAssignedMobNearPlayer(player, level, layer);
            trySpawnLayerAssignedMobNearPlayer(player, level, layer);
        }
    }
}
