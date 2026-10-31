package com.megastrata.content;

import com.megastrata.MegastrataMod;
import com.megastrata.entity.MegastructureEntities;
import com.megastrata.world.MegastrataChunkGenerator;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MegastrataMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MegastrataMod.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MegastrataMod.MODID);
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MegastrataMod.MODID);

    public static final RegistryObject<Block> STRUCTURAL_CONCRETE = BLOCKS.register("structural_concrete",
            () -> new Block(BlockBehaviour.Properties.of().strength(50.0f, 1200.0f).sound(SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> ELEVATOR_TERMINAL = BLOCKS.register("elevator_terminal",
            () -> new ElevatorTerminalBlock(BlockBehaviour.Properties.of().strength(15.0f, 600.0f).sound(SoundType.METAL)));
    public static final RegistryObject<Block> SHELTER_HEARTH = BLOCKS.register("shelter_hearth",
            () -> new Block(BlockBehaviour.Properties.of().strength(5.0f).lightLevel(s -> 12).sound(SoundType.LANTERN)));
    public static final RegistryObject<Block> WATER_PUMP_CONTROLLER = BLOCKS.register("water_pump_controller",
            () -> new LayerMechanisms.WaterPumpControllerBlock(BlockBehaviour.Properties.of().strength(12.0f)));
    public static final RegistryObject<Block> FACTORY_ASSEMBLER = BLOCKS.register("factory_assembler",
            () -> new LayerMechanisms.FactoryAssemblerBlock(BlockBehaviour.Properties.of().strength(15.0f)));
    public static final RegistryObject<Block> ARCHIVE_TERMINAL = BLOCKS.register("archive_terminal",
            () -> new LayerMechanisms.ArchiveTerminalBlock(BlockBehaviour.Properties.of().strength(10.0f).lightLevel(s -> 5)));
    public static final RegistryObject<Block> CONVEYOR_BELT = BLOCKS.register("conveyor_belt",
            () -> new Block(BlockBehaviour.Properties.of().strength(6.0f).speedFactor(1.4f)));
    public static final RegistryObject<Block> BIOMECH_SPORE_PLANT = BLOCKS.register("biomech_spore_plant",
            () -> new Block(BlockBehaviour.Properties.of().noCollission().lightLevel(s -> 10).sound(SoundType.FUNGUS)));
    public static final RegistryObject<Block> FOLDING_TABLE = BLOCKS.register("folding_table",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> KETTLE_BLOCK = BLOCKS.register("kettle_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.LANTERN)));
    public static final RegistryObject<Block> PORTABLE_LAMP = BLOCKS.register("portable_lamp",
            () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).lightLevel(s -> 14).sound(SoundType.LANTERN)));
    public static final RegistryObject<Block> RESONANCE_BEACON = BLOCKS.register("resonance_beacon",
            () -> new Block(BlockBehaviour.Properties.of().strength(8.0f).lightLevel(s -> 11).sound(SoundType.LODESTONE)));

    public static final RegistryObject<Item> STRUCTURAL_CONCRETE_ITEM = ITEMS.register("structural_concrete",
            () -> new BlockItem(STRUCTURAL_CONCRETE.get(), new Item.Properties()));
    public static final RegistryObject<Item> ELEVATOR_TERMINAL_ITEM = ITEMS.register("elevator_terminal",
            () -> new BlockItem(ELEVATOR_TERMINAL.get(), new Item.Properties()));
    public static final RegistryObject<Item> SHELTER_HEARTH_ITEM = ITEMS.register("shelter_hearth",
            () -> new BlockItem(SHELTER_HEARTH.get(), new Item.Properties()));
    public static final RegistryObject<Item> WATER_PUMP_CONTROLLER_ITEM = ITEMS.register("water_pump_controller",
            () -> new BlockItem(WATER_PUMP_CONTROLLER.get(), new Item.Properties()));
    public static final RegistryObject<Item> FACTORY_ASSEMBLER_ITEM = ITEMS.register("factory_assembler",
            () -> new BlockItem(FACTORY_ASSEMBLER.get(), new Item.Properties()));
    public static final RegistryObject<Item> ARCHIVE_TERMINAL_ITEM = ITEMS.register("archive_terminal",
            () -> new BlockItem(ARCHIVE_TERMINAL.get(), new Item.Properties()));
    public static final RegistryObject<Item> CONVEYOR_BELT_ITEM = ITEMS.register("conveyor_belt",
            () -> new BlockItem(CONVEYOR_BELT.get(), new Item.Properties()));
    public static final RegistryObject<Item> BIOMECH_SPORE_PLANT_ITEM = ITEMS.register("biomech_spore_plant",
            () -> new BlockItem(BIOMECH_SPORE_PLANT.get(), new Item.Properties()));
    public static final RegistryObject<Item> FOLDING_TABLE_ITEM = ITEMS.register("folding_table",
            () -> new BlockItem(FOLDING_TABLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> KETTLE_BLOCK_ITEM = ITEMS.register("kettle_block",
            () -> new BlockItem(KETTLE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> PORTABLE_LAMP_ITEM = ITEMS.register("portable_lamp",
            () -> new BlockItem(PORTABLE_LAMP.get(), new Item.Properties()));
    public static final RegistryObject<Item> RESONANCE_BEACON_ITEM = ITEMS.register("resonance_beacon",
            () -> new BlockItem(RESONANCE_BEACON.get(), new Item.Properties()));

    public static final RegistryObject<Item> STRUCTURAL_ALLOY = ITEMS.register("structural_alloy", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENERGY_CELL = ITEMS.register("energy_cell", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BIO_SPORE_FILTER = ITEMS.register("bio_spore_filter", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEMORY_MODULE = ITEMS.register("memory_module", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DRIVE_RELAY_UNIT = ITEMS.register("drive_relay_unit", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ARCHIVE_BOX_DECOR = ITEMS.register("archive_box_decor", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SECTOR_SCANNER = ITEMS.register("sector_scanner", () -> new SectorScannerItem(new Item.Properties()));
    public static final RegistryObject<Item> ARCHITECTURAL_JOURNAL = ITEMS.register("architectural_journal", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ROUTE_MARKER = ITEMS.register("route_marker", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> VERTICAL_PROFILER = ITEMS.register("vertical_profiler", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ARCHIVAL_MAP = ITEMS.register("archival_map", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SAFETY_HARNESS = ITEMS.register("safety_harness",
            () -> new VerticalTransportAndSafety.SafetyHarnessItem(new Item.Properties()));

    public static final RegistryObject<EntityType<MegastructureEntities.ServiceDroneEntity>> SERVICE_DRONE =
            ENTITIES.register("service_drone", () -> EntityType.Builder.of(MegastructureEntities.ServiceDroneEntity::new, MobCategory.AMBIENT)
                    .sized(0.8f, 0.8f).build("service_drone"));
    public static final RegistryObject<EntityType<MegastructureEntities.WallClimberEntity>> WALL_CLIMBER =
            ENTITIES.register("wall_climber", () -> EntityType.Builder.of(MegastructureEntities.WallClimberEntity::new, MobCategory.MONSTER)
                    .sized(0.9f, 0.9f).build("wall_climber"));
    public static final RegistryObject<EntityType<MegastructureEntities.FilterOrganismEntity>> FILTER_ORGANISM =
            ENTITIES.register("filter_organism", () -> EntityType.Builder.of(MegastructureEntities.FilterOrganismEntity::new, MobCategory.WATER_CREATURE)
                    .sized(1.2f, 1.2f).build("filter_organism"));

    public static final RegistryObject<Codec<? extends ChunkGenerator>> MEGASTRUCTURE_GENERATOR =
            CHUNK_GENERATORS.register("megastructure_generator", () -> MegastrataChunkGenerator.CODEC);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        CHUNK_GENERATORS.register(bus);
    }
}
