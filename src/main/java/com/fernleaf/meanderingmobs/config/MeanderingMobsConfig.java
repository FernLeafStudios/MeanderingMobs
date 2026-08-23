package com.fernleaf.meanderingmobs.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MeanderingMobsConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC; // <--- Client Spec

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

    //Okapi Configs
    public static final ModConfigSpec.DoubleValue OKAPI_ALERT_RADIUS;

    // Client Configs
    public static final ModConfigSpec.BooleanValue ENABLE_AUKVULTURE_CAMERA_ROLL;

    static {
        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();

        commonBuilder.push("Whisp Settings");
        WHISP_TAG_MAX_DISTANCE = commonBuilder
                .comment("Maximum distance in blocks a player can be from a Whisp before the tag game fails.")
                .defineInRange("tagMaxDistance", 16.0, 4.0, 64.0);
        WHISP_PACIFY_RADIUS = commonBuilder
                .comment("Radius in blocks for the Tamed Whisp pacification aura.")
                .defineInRange("pacifyRadius", 8.0, 1.0, 32.0);
        WHISP_PACIFY_DURATION_TICKS = commonBuilder
                .comment("Duration in ticks of the Whimsical effect applied to mobs (20 ticks = 1 second).")
                .defineInRange("pacifyDurationTicks", 600, 20, 7200);
        commonBuilder.pop();

        commonBuilder.push("Soul Orb Settings");
        SOUL_ORB_ALLOW_WILD_CAPTURE = commonBuilder
                .comment("If true, wild mobs can be captured using struggle chance logic. If false, only tamed entities can be captured.")
                .define("allowWildCapture", true);
        SOUL_ORB_BASE_SUCCESS_RATE = commonBuilder
                .comment("Base capture chance for wild mobs when near 0% HP (0.7 = 70%).")
                .defineInRange("baseSuccessRate", 0.70, 0.0, 1.0);
        SOUL_ORB_HEALTH_PENALTY_WEIGHT = commonBuilder
                .comment("How much remaining HP reduces capture chance. Formula: baseSuccess - (healthRatio * penaltyWeight).")
                .defineInRange("healthPenaltyWeight", 0.50, 0.0, 1.0);
        commonBuilder.pop();

        commonBuilder.push("Quilled Effect Settings");
        QUILLED_DAMAGE_SCALING = commonBuilder
                .comment("Calculates damage per tick based on amplifier level: LINEAR (base * amp), EXPONENTIAL (base ^ amp), LOGARITHMIC (base * log2(amp)).")
                .defineEnum("damageScaling", DamageScaling.EXPONENTIAL);
        QUILLED_BASE_DAMAGE = commonBuilder
                .comment("Base damage dealt by the Quilled effect.")
                .defineInRange("baseDamage", 1.0, 0.1, 20.0);
        QUILLED_BASE_INTERVAL_TICKS = commonBuilder
                .comment("Base interval in ticks between damage ticks at level 1.")
                .defineInRange("baseIntervalTicks", 40, 5, 200);
        commonBuilder.pop();

        commonBuilder.push("Tegu Settings");
        TEGU_MIN_SHED_TICKS = commonBuilder
                .comment("Minimum interval in ticks before a Tegu sheds a scale (6000 ticks = 5 minutes).")
                .defineInRange("minShedTicks", 6000, 1200, 72000);
        TEGU_MAX_SHED_TICKS = commonBuilder
                .comment("Maximum interval in ticks before a Tegu sheds a scale (12000 ticks = 10 minutes).")
                .defineInRange("maxShedTicks", 12000, 2400, 144000);
        commonBuilder.pop();

        commonBuilder.push("Okapi Settings");
        OKAPI_ALERT_RADIUS = commonBuilder
                .comment("Radius in blocks that okapis scan for hostile mobs tagged in 'alert_okapi'.")
                .defineInRange("alertRadius", 12.0, 4.0, 32.0);
        commonBuilder.pop();

        COMMON_SPEC = commonBuilder.build();

        // Build Client Spec
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();

        clientBuilder.push("Aukvulture Flight Controls");
        ENABLE_AUKVULTURE_CAMERA_ROLL = clientBuilder
                .comment("Enables camera roll while riding an Aukvulture.")
                .define("enableCameraRoll", true);
        clientBuilder.pop();

        CLIENT_SPEC = clientBuilder.build();
    }

    public static <T> T getSafe(ModConfigSpec.ConfigValue<T> configValue) {
        try {
            return configValue.get();
        } catch (IllegalStateException e) {
            return configValue.getDefault();
        }
    }
}