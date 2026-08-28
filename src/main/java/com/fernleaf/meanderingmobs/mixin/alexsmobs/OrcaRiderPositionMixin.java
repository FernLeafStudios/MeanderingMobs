package com.fernleaf.meanderingmobs.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class OrcaRiderPositionMixin {

    @Inject(
            method = "getPassengerAttachmentPoint",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void meandering$offsetOrcaAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if ((Object) this instanceof EntityOrca orca) {
            Vec3 baseOffset = cir.getReturnValue();

            if (baseOffset != null) {
                double forwardOffset = 1.4D;
                double yawRad = orca.getYRot() * Mth.DEG_TO_RAD;

                double offsetX = -Mth.sin((float) yawRad) * forwardOffset;
                double offsetZ = Mth.cos((float) yawRad) * forwardOffset;

                cir.setReturnValue(new Vec3(
                        baseOffset.x() + offsetX,
                        baseOffset.y(),
                        baseOffset.z() + offsetZ
                ));
            }
        }
    }
}