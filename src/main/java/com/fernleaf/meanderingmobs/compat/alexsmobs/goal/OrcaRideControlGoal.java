package com.fernleaf.meanderingmobs.compat.alexsmobs.goal;

import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class OrcaRideControlGoal extends Goal {
    private final EntityOrca orca;

    public OrcaRideControlGoal(EntityOrca orca) {
        this.orca = orca;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Active only when a player is riding the orca and it's in water
        return this.orca.isVehicle() && this.orca.getFirstPassenger() instanceof Player && this.orca.isInWater();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.orca.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.orca.getFirstPassenger() instanceof Player player) {
            // Match rotation to the player's view
            this.orca.setYRot(player.getYRot());
            this.orca.yBodyRot = player.getYRot();
            this.orca.yHeadRot = player.getYRot();
            this.orca.setXRot(player.getXRot() * 0.5F); // Mild pitch control

            // Read player movement inputs directly from the player instance
            // player.zza = forward/back (-1.0 to 1.0), player.xxa = left/right (-1.0 to 1.0)
            float forward = player.zza;
            float sideways = player.xxa;

            if (forward != 0.0F || sideways != 0.0F) {
                Vec3 lookVector = player.getLookAngle();

                // Scale movement speed (adjust 0.15D to make it faster/slower)
                double speed = 0.15D * forward;

                Vec3 currentMotion = this.orca.getDeltaMovement();
                // Apply forward movement along look vector, adding a bit of upward/downward steering based on pitch
                this.orca.setDeltaMovement(
                        currentMotion.add(
                                lookVector.x * speed,
                                lookVector.y * Math.abs(forward) * 0.1D,
                                lookVector.z * speed
                        ).scale(0.85D) // Damping factor so it doesn't build infinite speed
                );
            } else {
                // Slow down smoothly when no keys are pressed
                this.orca.setDeltaMovement(this.orca.getDeltaMovement().scale(0.8D));
            }
        }
    }
}