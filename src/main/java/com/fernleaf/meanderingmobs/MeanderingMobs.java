package com.fernleaf.meanderingmobs;

import com.fernleaf.meanderingmobs.config.MeanderingMobsConfig;
import com.fernleaf.meanderingmobs.registry.*;
import com.fernleaf.meanderingmobs.server.command.RuffianInspectCommand;
import com.fernleaf.meanderingmobs.server.datagen.MeanderingMobsBiomeTagProvider;
import com.fernleaf.meanderingmobs.server.datagen.MeanderingMobsBlockTagProvider;
import com.fernleaf.meanderingmobs.server.datagen.MeanderingMobsEntityTypeTagProvider;
import com.fernleaf.meanderingmobs.server.datagen.MeanderingMobsItemTagProvider;
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
        modEventBus.addListener(this::gatherData);

        modContainer.registerConfig(ModConfig.Type.COMMON, MeanderingMobsConfig.COMMON_SPEC);

        // Register Registries
        MeanderingMobsEntityRegistry.register(modEventBus);
        MeanderingMobsItemRegistry.register(modEventBus);
        MeanderingMobsBlockRegistry.register(modEventBus);
        MeanderingMobsBlockEntityRegistry.register(modEventBus);
        MeanderingMobsEffectsRegistry.register(modEventBus);
        MeanderingMobsPotionRegistry.register(modEventBus);
        MeanderingMobsSoundsRegistry.SOUND_EVENTS.register(modEventBus);
        MeanderingMobsCreativeTabRegistry.register(modEventBus);
        RuffianMemoryModuleTypes.register(modEventBus);
        RuffianActivities.register(modEventBus);

        // Register Game/Server Events (Commands, Level Events, etc.)
        NeoForge.EVENT_BUS.addListener(MeanderingMobs::onRegisterCommands);

        LOGGER.info("Meandering Mobs initialized with FernFrame!");
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        RuffianInspectCommand.register(event.getDispatcher());
    }

    private void gatherData(GatherDataEvent event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        MeanderingMobsBlockTagProvider blockTagsProvider =
                new MeanderingMobsBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        event.getGenerator().addProvider(event.includeServer(), blockTagsProvider);

        event.getGenerator().addProvider(event.includeServer(),
                new MeanderingMobsItemTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));

        event.getGenerator().addProvider(event.includeServer(),
                new MeanderingMobsEntityTypeTagProvider(packOutput, lookupProvider, existingFileHelper));

        event.getGenerator().addProvider(event.includeServer(),
                new MeanderingMobsBiomeTagProvider(packOutput, lookupProvider, existingFileHelper));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Meandering Mobs Common Setup Complete.");
    }
}