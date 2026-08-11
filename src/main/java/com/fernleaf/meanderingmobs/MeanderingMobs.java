package com.fernleaf.meanderingmobs;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(MeanderingMobs.MODID)
public class MeanderingMobs {
    public static final String MODID = "meanderingmobs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MeanderingMobs(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        MeanderingMobsEntityRegistry.register(modEventBus);
        MeanderingMobsItemRegistry.register(modEventBus);

        LOGGER.info("Meandering Mobs initialized with FernFrame!");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Meandering Mobs Common Setup Complete.");
    }
}