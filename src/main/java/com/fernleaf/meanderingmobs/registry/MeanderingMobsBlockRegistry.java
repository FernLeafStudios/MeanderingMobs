package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MeanderingMobsBlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MeanderingMobs.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MeanderingMobs.MODID);

    // Existing Crystal Lamp[cite: 4]
    public static final DeferredHolder<Block, ChannelCrystalLampBlock> CHANNEL_CRYSTAL_LAMP = BLOCKS.register("channel_crystal_lamp",
            () -> new ChannelCrystalLampBlock(BlockBehaviour.Properties.of()
                    .strength(0.3F)
                    .sound(SoundType.GLASS)
            )
    );

    public static final DeferredHolder<Item, BlockItem> CHANNEL_CRYSTAL_LAMP_ITEM = ITEMS.register("channel_crystal_lamp",
            () -> new BlockItem(CHANNEL_CRYSTAL_LAMP.get(), new Item.Properties())
    );

    // 1. Solid Channel Crystal Block
    public static final DeferredHolder<Block, Block> CHANNEL_CRYSTAL_BLOCK = BLOCKS.register("channel_crystal_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()
            )
    );
    public static final DeferredHolder<Item, BlockItem> CHANNEL_CRYSTAL_BLOCK_ITEM = ITEMS.register("channel_crystal_block",
            () -> new BlockItem(CHANNEL_CRYSTAL_BLOCK.get(), new Item.Properties())
    );

    // 2. Channel Crystal Chain (Custom extensible chain class)
    public static final DeferredHolder<Block, ChannelCrystalChainBlock> CHANNEL_CRYSTAL_CHAIN = BLOCKS.register("channel_crystal_chain",
            () -> new ChannelCrystalChainBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.CHAIN)
                    .noOcclusion()
            )
    );
    public static final DeferredHolder<Item, BlockItem> CHANNEL_CRYSTAL_CHAIN_ITEM = ITEMS.register("channel_crystal_chain",
            () -> new BlockItem(CHANNEL_CRYSTAL_CHAIN.get(), new Item.Properties())
    );

    // 3. Unified Channel Crystal Cluster (Dripstone-style multi-shape block)
    public static final DeferredHolder<Block, ChannelCrystalClusterBlock> CHANNEL_CRYSTAL_CLUSTER = BLOCKS.register("channel_crystal_cluster",
            () -> new ChannelCrystalClusterBlock(BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(state -> 8)
                    .noOcclusion()
            )
    );
    public static final DeferredHolder<Item, BlockItem> CHANNEL_CRYSTAL_CLUSTER_ITEM = ITEMS.register("channel_crystal_cluster",
            () -> new BlockItem(CHANNEL_CRYSTAL_CLUSTER.get(), new Item.Properties())
    );

    // 4. Aurora Block
    public static final DeferredHolder<Block, AuroraBlock> AURORA_BLOCK = BLOCKS.register("aurora_block",
            () -> new AuroraBlock(BlockBehaviour.Properties.of()
                    .strength(0.2F)
                    .sound(SoundType.POWDER_SNOW)
                    .noOcclusion()
                    .noCollission()
            )
    );
    public static final DeferredHolder<Item, BlockItem> AURORA_BLOCK_ITEM = ITEMS.register("aurora_block",
            () -> new BlockItem(AURORA_BLOCK.get(), new Item.Properties())
    );

    public static final DeferredHolder<Block, CarvedStrippedSpruceLogBlock> CARVED_STRIPPED_SPRUCE_LOG = BLOCKS.register("carved_stripped_spruce_log",
            () -> new CarvedStrippedSpruceLogBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
            )
    );

    public static final DeferredHolder<Item, BlockItem> CARVED_STRIPPED_SPRUCE_LOG_ITEM = ITEMS.register("carved_stripped_spruce_log",
            () -> new BlockItem(CARVED_STRIPPED_SPRUCE_LOG.get(), new Item.Properties())
    );

    // Queuebox Block
    public static final DeferredHolder<Block, QueueboxBlock> QUEUEBOX = BLOCKS.register("queuebox_block",
            () -> new QueueboxBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
            )
    );

    public static final DeferredHolder<Block, RotatedPillarBlock> KOKESHI_LOG = BLOCKS.register("kokeshi_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
            )
    );
    public static final DeferredHolder<Item, BlockItem> KOKESHI_LOG_ITEM = ITEMS.register("kokeshi_log",
            () -> new BlockItem(KOKESHI_LOG.get(), new Item.Properties())
    );

    public static final DeferredHolder<Block, RotatedPillarBlock> STRIPPED_KOKESHI_LOG = BLOCKS.register("stripped_kokeshi_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
            )
    );
    public static final DeferredHolder<Item, BlockItem> STRIPPED_KOKESHI_LOG_ITEM = ITEMS.register("stripped_kokeshi_log",
            () -> new BlockItem(STRIPPED_KOKESHI_LOG.get(), new Item.Properties())
    );

    public static final DeferredHolder<Block, RotatedPillarBlock> KOKESHI_WOOD = BLOCKS.register("kokeshi_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
            )
    );
    public static final DeferredHolder<Item, BlockItem> KOKESHI_WOOD_ITEM = ITEMS.register("kokeshi_wood",
            () -> new BlockItem(KOKESHI_WOOD.get(), new Item.Properties())
    );

    public static final DeferredHolder<Block, RotatedPillarBlock> STRIPPED_KOKESHI_WOOD = BLOCKS.register("stripped_kokeshi_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
            )
    );
    public static final DeferredHolder<Item, BlockItem> STRIPPED_KOKESHI_WOOD_ITEM = ITEMS.register("stripped_kokeshi_wood",
            () -> new BlockItem(STRIPPED_KOKESHI_WOOD.get(), new Item.Properties())
    );

    // 2. Base Planks
    public static final DeferredHolder<Block, Block> KOKESHI_PLANKS = BLOCKS.register("kokeshi_planks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
            )
    );
    public static final DeferredHolder<Item, BlockItem> KOKESHI_PLANKS_ITEM = ITEMS.register("kokeshi_planks",
            () -> new BlockItem(KOKESHI_PLANKS.get(), new Item.Properties())
    );

    // 3. Stairs & Slabs
    public static final DeferredHolder<Block, StairBlock> KOKESHI_STAIRS = BLOCKS.register("kokeshi_stairs",
            () -> new StairBlock(KOKESHI_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(KOKESHI_PLANKS.get()))
    );
    public static final DeferredHolder<Item, BlockItem> KOKESHI_STAIRS_ITEM = ITEMS.register("kokeshi_stairs",
            () -> new BlockItem(KOKESHI_STAIRS.get(), new Item.Properties())
    );

    public static final DeferredHolder<Block, SlabBlock> KOKESHI_SLAB = BLOCKS.register("kokeshi_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(KOKESHI_PLANKS.get()))
    );
    public static final DeferredHolder<Item, BlockItem> KOKESHI_SLAB_ITEM = ITEMS.register("kokeshi_slab",
            () -> new BlockItem(KOKESHI_SLAB.get(), new Item.Properties())
    );

    // 4. Fences & Fence Gates
    public static final DeferredHolder<Block, FenceBlock> KOKESHI_FENCE = BLOCKS.register("kokeshi_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(KOKESHI_PLANKS.get()))
    );
    public static final DeferredHolder<Item, BlockItem> KOKESHI_FENCE_ITEM = ITEMS.register("kokeshi_fence",
            () -> new BlockItem(KOKESHI_FENCE.get(), new Item.Properties())
    );

    public static final DeferredHolder<Block, FenceGateBlock> KOKESHI_FENCE_GATE = BLOCKS.register("kokeshi_fence_gate",
            () -> new FenceGateBlock(WoodType.CHERRY, BlockBehaviour.Properties.ofFullCopy(KOKESHI_PLANKS.get()))
    );
    public static final DeferredHolder<Item, BlockItem> KOKESHI_FENCE_GATE_ITEM = ITEMS.register("kokeshi_fence_gate",
            () -> new BlockItem(KOKESHI_FENCE_GATE.get(), new Item.Properties())
    );

    // 5. Doors & Trapdoors
    public static final DeferredHolder<Block, DoorBlock> KOKESHI_DOOR = BLOCKS.register("kokeshi_door",
            () -> new DoorBlock(BlockSetType.CHERRY, BlockBehaviour.Properties.ofFullCopy(KOKESHI_PLANKS.get()).noOcclusion())
    );
    public static final DeferredHolder<Item, BlockItem> KOKESHI_DOOR_ITEM = ITEMS.register("kokeshi_door",
            () -> new BlockItem(KOKESHI_DOOR.get(), new Item.Properties())
    );

    public static final DeferredHolder<Block, TrapDoorBlock> KOKESHI_TRAPDOOR = BLOCKS.register("kokeshi_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.CHERRY, BlockBehaviour.Properties.ofFullCopy(KOKESHI_PLANKS.get()).noOcclusion())
    );
    public static final DeferredHolder<Item, BlockItem> KOKESHI_TRAPDOOR_ITEM = ITEMS.register("kokeshi_trapdoor",
            () -> new BlockItem(KOKESHI_TRAPDOOR.get(), new Item.Properties())
    );

    // 6. Buttons & Pressure Plates
    public static final DeferredHolder<Block, ButtonBlock> KOKESHI_BUTTON = BLOCKS.register("kokeshi_button",
            () -> new ButtonBlock(BlockSetType.CHERRY, 30, BlockBehaviour.Properties.ofFullCopy(KOKESHI_PLANKS.get()).noCollission())
    );
    public static final DeferredHolder<Item, BlockItem> KOKESHI_BUTTON_ITEM = ITEMS.register("kokeshi_button",
            () -> new BlockItem(KOKESHI_BUTTON.get(), new Item.Properties())
    );

    public static final DeferredHolder<Block, PressurePlateBlock> KOKESHI_PRESSURE_PLATE = BLOCKS.register("kokeshi_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.CHERRY, BlockBehaviour.Properties.ofFullCopy(KOKESHI_PLANKS.get()).noCollission())
    );
    public static final DeferredHolder<Item, BlockItem> KOKESHI_PRESSURE_PLATE_ITEM = ITEMS.register("kokeshi_pressure_plate",
            () -> new BlockItem(KOKESHI_PRESSURE_PLATE.get(), new Item.Properties())
    );

    public static final DeferredHolder<Item, BlockItem> QUEUEBOX_ITEM = ITEMS.register("queuebox_block",
            () -> new BlockItem(QUEUEBOX.get(), new Item.Properties())
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}