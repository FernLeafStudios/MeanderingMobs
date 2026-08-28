package com.fernleaf.meanderingmobs.compat.alexsmobs;

import net.neoforged.fml.ModList;

public class AlexsMobsCompat {
    public static final String MODID = "alexsmobs";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MODID);
    }
}