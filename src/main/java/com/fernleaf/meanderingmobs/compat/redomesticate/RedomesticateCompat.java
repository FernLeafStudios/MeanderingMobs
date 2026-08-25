package com.fernleaf.meanderingmobs.compat.redomesticate;

import net.neoforged.fml.ModList;

public class RedomesticateCompat {
    public static final String MOD_ID = "redomesticate";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }
}