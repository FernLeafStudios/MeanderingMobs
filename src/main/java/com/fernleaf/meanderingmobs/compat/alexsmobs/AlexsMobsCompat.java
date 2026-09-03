package com.fernleaf.meanderingmobs.compat.alexsmobs;

import net.neoforged.fml.ModList;

public class AlexsMobsCompat {
    private static final String MOD_ID = "alexsmobs";
    private static Boolean loaded = null;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get().isLoaded(MOD_ID);
        }
        return loaded;
    }
}