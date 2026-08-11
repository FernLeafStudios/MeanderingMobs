package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MeanderingMobs.MODID, value = Dist.CLIENT)
public class MeanderingMobsKeybindsRegistry {

    public static final String CATEGORY = "key.category." + MeanderingMobs.MODID + ".mob_riding";

    public static final KeyMapping FLAP_KEY = new KeyMapping(
            "key." + MeanderingMobs.MODID + ".flap",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            CATEGORY
    );

    public static final KeyMapping DIVE_KEY = new KeyMapping(
            "key." + MeanderingMobs.MODID + ".dive",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FLAP_KEY);
        event.register(DIVE_KEY);
    }
}