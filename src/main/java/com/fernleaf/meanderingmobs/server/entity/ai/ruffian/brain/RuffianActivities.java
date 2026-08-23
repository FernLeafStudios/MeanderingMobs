package com.fernleaf.meanderingmobs.server.entity.ai.ruffian.brain;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RuffianActivities {
    public static final DeferredRegister<Activity> ACTIVITIES =
            DeferredRegister.create(Registries.ACTIVITY, MeanderingMobs.MODID);

    public static final DeferredHolder<Activity, Activity> CHORES =
            ACTIVITIES.register("chores", () -> new Activity("chores"));

    public static void register(IEventBus eventBus) {
        ACTIVITIES.register(eventBus);
    }
}