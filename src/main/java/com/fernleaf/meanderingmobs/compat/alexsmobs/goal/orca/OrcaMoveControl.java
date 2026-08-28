package com.fernleaf.meanderingmobs.compat.alexsmobs.goal.orca;

import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.player.Player;

public class OrcaMoveControl extends MoveControl {
    private final EntityOrca orca;
    private boolean isJumping = false;
    private boolean isSneaking = false;

    public OrcaMoveControl(EntityOrca orca) {
        super(orca);
        this.orca = orca;
    }

    public void setClientInputs(boolean jumping, boolean sneaking) {
        this.isJumping = jumping;
        this.isSneaking = sneaking;
    }

    @Override
    public void tick() {
        // Ambient buoyancy
        if (this.orca.isInWater()) {
            this.orca.setDeltaMovement(this.orca.getDeltaMovement().add(0.0D, 0.005D, 0.0D));
        }

        // --- RIDDEN LOGIC ---
        if (this.orca.getFirstPassenger() instanceof Player player) {
            this.orca.setYRot(player.getYRot());
            this.orca.yBodyRot = this.orca.getYRot();
            this.orca.yHeadRot = this.orca.getYRot();

            float pitch = player.getXRot();
            if (this.isJumping) pitch = -45.0F;
            if (this.isSneaking) pitch = 45.0F;

            this.orca.setXRot(Mth.clamp(pitch, -85.0F, 85.0F));

            float movementSpeed = (float) this.orca.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float controlledSpeed = movementSpeed * 1.5F; // Proper riding speed boost

            float forwardInput = player.zza > 0 ? 1.0F : (player.zza < 0 ? -0.5F : 0.0F);

            if (forwardInput != 0.0F || this.isJumping || this.isSneaking) {
                this.orca.setSpeed(controlledSpeed * 0.05F);

                float xRotRad = this.orca.getXRot() * Mth.DEG_TO_RAD;
                float cosPitch = Mth.cos(xRotRad);
                float sinPitch = Mth.sin(xRotRad);

                this.orca.zza = cosPitch * controlledSpeed * forwardInput;
                this.orca.yya = -sinPitch * controlledSpeed * (forwardInput != 0 ? Math.signum(forwardInput) : 1.0F);
            } else {
                this.orca.setSpeed(0.0F);
                this.orca.setXxa(0.0F);
                this.orca.setYya(0.0F);
                this.orca.setZza(0.0F);
            }
            return;
        }

        // --- WILD / UNRIDDEN LOGIC ---
        // Ported directly from Alex's native MoveHelperController so they don't freeze
        if (this.operation == MoveControl.Operation.MOVE_TO && !this.orca.getNavigation().isDone()) {
            double d0 = this.wantedX - this.orca.getX();
            double d1 = this.wantedY - this.orca.getY();
            double d2 = this.wantedZ - this.orca.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d3 < 2.5000003E-7F) {
                this.orca.setZza(0.0F);
            } else {
                float f = (float) (Mth.atan2(d2, d0) * (double) Mth.RAD_TO_DEG) - 90.0F;
                this.orca.setYRot(this.rotlerp(this.orca.getYRot(), f, 10.0F));
                this.orca.yBodyRot = this.orca.getYRot();
                this.orca.yHeadRot = this.orca.getYRot();
                float f1 = (float) (this.speedModifier * this.orca.getAttributeValue(Attributes.MOVEMENT_SPEED));

                if (this.orca.isInWater()) {
                    this.orca.setSpeed(f1 * 0.02F);
                    float f2 = -((float) (Mth.atan2(d1, Mth.sqrt((float) (d0 * d0 + d2 * d2))) * (double) Mth.RAD_TO_DEG));
                    f2 = Mth.clamp(Mth.wrapDegrees(f2), -85.0F, 85.0F);
                    this.orca.setXRot(this.rotlerp(this.orca.getXRot(), f2, 5.0F));
                    float xRotRad = this.orca.getXRot() * Mth.DEG_TO_RAD;
                    float f3 = Mth.cos(xRotRad);
                    float f4 = Mth.sin(xRotRad);
                    this.orca.zza = f3 * f1;
                    this.orca.yya = -f4 * f1;
                } else {
                    this.orca.setSpeed(f1 * 0.1F);
                }
            }
        } else {
            this.orca.setSpeed(0.0F);
            this.orca.setXxa(0.0F);
            this.orca.setYya(0.0F);
            this.orca.setZza(0.0F);
        }
    }
}