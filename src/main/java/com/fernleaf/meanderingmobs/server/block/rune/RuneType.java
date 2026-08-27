package com.fernleaf.meanderingmobs.server.block.rune;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum RuneType implements StringRepresentable {
    DEERFOX(0, "deerfox", "rune.meanderingmobs.deerfox"),
    WHISP(1, "whisp", "rune.meanderingmobs.whisp"),
    AUKVULTURE(2, "aukvulture", "rune.meanderingmobs.aukvulture"),
    TEGU(3, "tegu", "rune.meanderingmobs.tegu"),
    PORCUPINE(4, "porcupine", "rune.meanderingmobs.porcupine"),
    RUFFIAN(5, "ruffian", "rune.meanderingmobs.ruffian"),
    OKAPI(6, "okapi", "rune.meanderingmobs.okapi"),
    WOLVERINE(7, "wolverine", "rune.meanderingmobs.wolverine"),
    PARROTFISH(8, "parrotfish", "rune.meanderingmobs.parrotfish"),
    ANCHOVY(9, "anchovy", "rune.meanderingmobs.anchovy"),
    GUTTERTANK(10, "guttertank", "rune.meanderingmobs.guttertank");

    private static final Map<Integer, RuneType> BY_ID = Arrays.stream(values())
            .collect(Collectors.toMap(RuneType::getId, Function.identity()));

    private final int id;
    private final String name;
    private final String translationKey;
    private final ResourceLocation textureLocation;

    RuneType(int id, String name, String translationKey) {
        this.id = id;
        this.name = name;
        this.translationKey = translationKey;
        // Points directly to assets/meanderingmobs/textures/block/runes/rune_<name>.png
        this.textureLocation = ResourceLocation.fromNamespaceAndPath(
                MeanderingMobs.MODID, "block/runes/rune_" + name
        );
    }

    public int getId() { return this.id; }
    public String getTranslationKey() { return this.translationKey; }
    public ResourceLocation getTextureLocation() { return this.textureLocation; }

    @Override
    public @NotNull String getSerializedName() { return this.name; }

    public static RuneType byId(int id) {
        return BY_ID.getOrDefault(id, DEERFOX);
    }

    public RuneType next() {
        return byId((this.id + 1) % values().length);
    }
}