package com.fernleaf.meanderingmobs;

import com.fernleaf.meanderingmobs.config.MeanderingMobsConfig;
import com.fernleaf.meanderingmobs.registry.*;
import com.fernleaf.meanderingmobs.server.command.RuffianInspectCommand;
import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.brain.RuffianActivities;
import com.fernleaf.meanderingmobs.server.entity.ai.ruffian.brain.RuffianMemoryModuleTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(MeanderingMobs.MODID)
public class MeanderingMobs {
    public static final String MODID = "meanderingmobs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MeanderingMobs(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        modContainer.registerConfig(ModConfig.Type.COMMON, MeanderingMobsConfig.COMMON_SPEC);

        MeanderingMobsAttachmentRegistry.register(modEventBus);
        MeanderingMobsEntityRegistry.register(modEventBus);
        MeanderingMobsArmorMaterials.register(modEventBus);
        MeanderingMobsItemRegistry.register(modEventBus);
        MeanderingMobsBlockRegistry.register(modEventBus);
        MeanderingMobsBlockEntityRegistry.register(modEventBus);
        MeanderingMobsMenuRegistry.register(modEventBus);
        MeanderingMobsEffectsRegistry.register(modEventBus);
        MeanderingMobsPotionRegistry.register(modEventBus);
        MeanderingMobsSoundsRegistry.SOUND_EVENTS.register(modEventBus);
        MeanderingMobsCreativeTabRegistry.register(modEventBus);
        RuffianMemoryModuleTypes.register(modEventBus);
        RuffianActivities.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(MeanderingMobs::onRegisterCommands);

        LOGGER.info("Meandering Mobs initialized with FernFrame!");
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        RuffianInspectCommand.register(event.getDispatcher());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Meandering Mobs Common Setup Complete.");
    }
}