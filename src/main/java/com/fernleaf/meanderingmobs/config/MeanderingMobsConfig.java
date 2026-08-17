package com.fernleaf.meanderingmobs.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MeanderingMobsConfig {

    public static final ModConfigSpec SERVER_SPEC;

    // Whisp Configs
    public static final ModConfigSpec.DoubleValue WHISP_TAG_MAX_DISTANCE;
    public static final ModConfigSpec.DoubleValue WHISP_PACIFY_RADIUS;
    public static final ModConfigSpec.IntValue WHISP_PACIFY_DURATION_TICKS;

    // Soul Orb Configs
    public static final ModConfigSpec.BooleanValue SOUL_ORB_ALLOW_WILD_CAPTURE;
    public static final ModConfigSpec.DoubleValue SOUL_ORB_BASE_SUCCESS_RATE;
    public static final ModConfigSpec.DoubleValue SOUL_ORB_HEALTH_PENALTY_WEIGHT;

    // Quilled Effect Configs
    public enum DamageScaling { LINEAR, EXPONENTIAL, LOGARITHMIC }
    public static final ModConfigSpec.EnumValue<DamageScaling> QUILLED_DAMAGE_SCALING;
    public static final ModConfigSpec.DoubleValue QUILLED_BASE_DAMAGE;
    public static final ModConfigSpec.IntValue QUILLED_BASE_INTERVAL_TICKS;

    // Tegu Configs
    public static final ModConfigSpec.IntValue TEGU_MIN_SHED_TICKS;
    public static final ModConfigSpec.IntValue TEGU_MAX_SHED_TICKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Whisp Settings");
        WHISP_TAG_MAX_DISTANCE = builder
                .comment("Maximum distance in blocks a player can be from a Whisp before the tag game fails.")
                .defineInRange("tagMaxDistance", 16.0, 4.0, 64.0);
        WHISP_PACIFY_RADIUS = builder
                .comment("Radius in blocks for the Tamed Whisp pacification aura.")
                .defineInRange("pacifyRadius", 8.0, 1.0, 32.0);
        WHISP_PACIFY_DURATION_TICKS = builder
                .comment("Duration in ticks of the Whimsical effect applied to mobs (20 ticks = 1 second).")
                .defineInRange("pacifyDurationTicks", 600, 20, 7200);
        builder.pop();

        builder.push("Soul Orb Settings");
        SOUL_ORB_ALLOW_WILD_CAPTURE = builder
                .comment("If true, wild mobs can be captured using struggle chance logic. If false, only tamed entities can be captured.")
                .define("allowWildCapture", true);
        SOUL_ORB_BASE_SUCCESS_RATE = builder
                .comment("Base capture chance for wild mobs when near 0% HP (0.7 = 70%).")
                .defineInRange("baseSuccessRate", 0.70, 0.0, 1.0);
        SOUL_ORB_HEALTH_PENALTY_WEIGHT = builder
                .comment("How much remaining HP reduces capture chance. Formula: baseSuccess - (healthRatio * penaltyWeight).")
                .defineInRange("healthPenaltyWeight", 0.50, 0.0, 1.0);
        builder.pop();

        builder.push("Quilled Effect Settings");
        QUILLED_DAMAGE_SCALING = builder
                .comment("Calculates damage per tick based on amplifier level: LINEAR (base * amp), EXPONENTIAL (base ^ amp), LOGARITHMIC (base * log2(amp)).")
                .defineEnum("damageScaling", DamageScaling.EXPONENTIAL);
        QUILLED_BASE_DAMAGE = builder
                .comment("Base damage dealt by the Quilled effect.")
                .defineInRange("baseDamage", 1.0, 0.1, 20.0);
        QUILLED_BASE_INTERVAL_TICKS = builder
                .comment("Base interval in ticks between damage ticks at level 1.")
                .defineInRange("baseIntervalTicks", 40, 5, 200);
        builder.pop();

        builder.push("Tegu Settings");
        TEGU_MIN_SHED_TICKS = builder
                .comment("Minimum interval in ticks before a Tegu sheds a scale (6000 ticks = 5 minutes).")
                .defineInRange("minShedTicks", 6000, 1200, 72000);
        TEGU_MAX_SHED_TICKS = builder
                .comment("Maximum interval in ticks before a Tegu sheds a scale (12000 ticks = 10 minutes).")
                .defineInRange("maxShedTicks", 12000, 2400, 144000);
        builder.pop();

        SERVER_SPEC = builder.build();
    }
}