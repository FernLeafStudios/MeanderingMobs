package com.fernleaf.meanderingmobs.server.entity.ai.porcupine;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import com.fernleaf.meanderingmobs.server.entity.projectile.QuillArrowEntity;
import com.fernleaf.meanderingmobs.server.entity.tameable.PorcupineEntity;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class PorcupineShootGoal extends Goal {

    private final PorcupineEntity porcupine;
    private LivingEntity target;
    private int attackCooldown = 0;

    public PorcupineShootGoal(PorcupineEntity porcupine) {
        this.porcupine = porcupine;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        if (!this.porcupine.isTamed()
                || this.porcupine.getCommandState() != MeanderingMobsTameableEntity.CommandState.SIT
                || this.porcupine.isSheared()) {
            return false;
        }

        // Search for any alive Enemy entity nearby regardless of whether they have a target set yet
        List<LivingEntity> potentialTargets = this.porcupine.level().getEntitiesOfClass(
                LivingEntity.class,
                this.porcupine.getBoundingBox().inflate(12.0D),
                e -> e.isAlive()
                        && !isFriendly(e)
                        && (e instanceof Enemy || e.getLastHurtByMob() == this.porcupine.getOwner())
                        && this.porcupine.getSensing().hasLineOfSight(e)
        );

        if (!potentialTargets.isEmpty()) {
            this.target = potentialTargets.getFirst();
            return true;
        }
        return false;
    }

    private boolean isFriendly(LivingEntity entity) {
        if (entity == this.porcupine) return true;
        if (this.porcupine.isOwner(entity)) return true;
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) return true;

        if (entity instanceof OwnableEntity ownable && this.porcupine.getOwnerUUID() != null) {
            return this.porcupine.getOwnerUUID().equals(ownable.getOwnerUUID());
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null
                && this.target.isAlive()
                && !isFriendly(this.target)
                && this.porcupine.getCommandState() == MeanderingMobsTameableEntity.CommandState.SIT
                && !this.porcupine.isSheared()
                && this.porcupine.distanceToSqr(this.target) <= 144.0D
                && this.porcupine.getSensing().hasLineOfSight(this.target);
    }

    @Override
    public void tick() {
        if (this.target == null) return;

        // Turn body towards target directly
        double d0 = this.target.getX() - this.porcupine.getX();
        double d2 = this.target.getZ() - this.porcupine.getZ();
        float yaw = (float)(Math.atan2(d2, d0) * (180.0D / Math.PI)) - 90.0F;

        this.porcupine.setYRot(yaw);
        this.porcupine.setYHeadRot(yaw);
        this.porcupine.yBodyRot = yaw;
        this.porcupine.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

        if (--this.attackCooldown <= 0) {
            this.attackCooldown = 25; // 1.25s attack speed

            Vec3 look = Vec3.directionFromRotation(0.0F, yaw);
            double spawnX = this.porcupine.getX() + look.x * 0.6D;
            double spawnY = this.porcupine.getEyeY() - 0.1D;
            double spawnZ = this.porcupine.getZ() + look.z * 0.6D;

            QuillArrowEntity quill = new QuillArrowEntity(
                    this.porcupine.level(),
                    this.porcupine,
                    new ItemStack(MeanderingMobsItemRegistry.PORCUPINE_QUILL.get()),
                    null
            );

            quill.setPos(spawnX, spawnY, spawnZ);

            double targetX = this.target.getX() - spawnX;
            double targetY = this.target.getY(0.33D) - spawnY;
            double targetZ = this.target.getZ() - spawnZ;
            double horizDist = Math.sqrt(targetX * targetX + targetZ * targetZ);

            quill.shoot(targetX, targetY + horizDist * 0.2D, targetZ, 1.6F, 1.0F);

            this.porcupine.level().playSound(null, spawnX, spawnY, spawnZ,
                    SoundEvents.SKELETON_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.2F);

            this.porcupine.level().addFreshEntity(quill);
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.attackCooldown = 0;
    }
}