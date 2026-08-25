package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.block.AuroraBlock;
import com.fernleaf.meanderingmobs.server.block.ChannelCrystalChainBlock;
import com.fernleaf.meanderingmobs.server.block.ChannelCrystalClusterBlock;
import com.fernleaf.meanderingmobs.server.block.ChannelCrystalLampBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}