package com.fernleaf.meanderingmobs.client.model.ruffian;

import net.minecraft.world.item.DyeColor;
import java.util.Arrays;
import java.util.Comparator;

public enum RuffianColor {
    WHITE(0, DyeColor.WHITE),
    ORANGE(1, DyeColor.ORANGE),
    MAGENTA(2, DyeColor.MAGENTA),
    LIGHT_BLUE(3, DyeColor.LIGHT_BLUE),
    YELLOW(4, DyeColor.YELLOW),
    LIME(5, DyeColor.LIME),
    PINK(6, DyeColor.PINK),
    GRAY(7, DyeColor.GRAY),
    LIGHT_GRAY(8, DyeColor.LIGHT_GRAY),
    CYAN(9, DyeColor.CYAN),
    PURPLE(10, DyeColor.PURPLE),
    BLUE(11, DyeColor.BLUE),
    BROWN(12, DyeColor.BROWN),
    GREEN(13, DyeColor.GREEN),
    RED(14, DyeColor.RED),
    BLACK(15, DyeColor.BLACK);

    private static final RuffianColor[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(RuffianColor::getId))
            .toArray(RuffianColor[]::new);

    private final int id;
    private final DyeColor dyeColor;

    RuffianColor(int id, DyeColor dyeColor) {
        this.id = id;
        this.dyeColor = dyeColor;
    }

    public int getId() { return this.id; }
    public String getName() { return this.dyeColor.getName(); }

    public static RuffianColor byId(int id) {
        if (id < 0 || id >= BY_ID.length) return BLUE;

        // TEMPORARY SAFETY: Map everything to BLUE, RED, or YELLOW until all 16 textures are made
        RuffianColor selected = BY_ID[id];
        if (selected == RED || selected == YELLOW) {
            return selected;
        }
        return BLUE; // Default missing colors to blue for testing
    }
}