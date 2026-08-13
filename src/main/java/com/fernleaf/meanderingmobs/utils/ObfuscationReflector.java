package com.fernleaf.meanderingmobs.utils;

import net.minecraft.world.entity.Entity;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

public class ObfuscationReflector {

    private static final Method SET_ROT_METHOD = ObfuscationReflectionHelper.findMethod(
            Entity.class,
            "m_19915_", // SRG mapping for setRot(float, float) in 1.21.1
            float.class,
            float.class
    );

    public static void setEntityRotation(Entity entity, float yaw, float pitch) {
        try {
            SET_ROT_METHOD.invoke(entity, yaw, pitch);
        } catch (Exception e) {
            // Fallback using direct setters if reflection fails
            entity.setYRot(yaw);
            entity.setXRot(pitch);
        }
    }
}