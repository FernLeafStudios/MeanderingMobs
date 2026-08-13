package com.fernleaf.meanderingmobs.server.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SoulFireballEntity extends ThrowableItemProjectile {
    private int flightTicks = 0;
    private static final int MAX_FLIGHT_TICKS = 100;

    // 1. Default constructor for your entity registry
    public SoulFireballEntity(EntityType<? extends SoulFireballEntity> entityType, Level level) {
        super(entityType, level);
    }

    // 2. AI Goal Constructor
    public SoulFireballEntity(EntityType<? extends SoulFireballEntity> entityType, Level level, LivingEntity shooter, Vec3 movement) {
        super(entityType, level);
        this.setOwner(shooter);

        // Push spawn position slightly forward so it doesn't instantly collide with the Soul Flare's hitbox
        this.setPos(shooter.getX() + movement.x * 0.7D, shooter.getY(0.5D) + 0.2D, shooter.getZ() + movement.z * 0.7D);
        this.setDeltaMovement(movement.scale(0.85D));

        // Disables standard throwing arc so it flies straight like a Ghast fireball
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        // Satisfies ThrownItemRenderer automatically
        return Items.FIRE_CHARGE;
    }

    @Override
    public void tick() {
        // super.tick() automatically handles ALL movement, bounding box updates, and block/entity raycasting!
        super.tick();

        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        } else {
            this.flightTicks++;
            if (this.flightTicks > MAX_FLIGHT_TICKS) {
                this.discard();
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }

        this.markHurt();
        Entity attacker = source.getEntity();

        if (attacker != null) {
            Vec3 lookVec = attacker.getLookAngle();
            this.setDeltaMovement(lookVec.scale(1.4D));
            this.setOwner(attacker);
            this.hasImpulse = true;
            this.flightTicks = 0;
            return true;
        }
        return false;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            if (result.getEntity() instanceof LivingEntity target) {
                DamageSource source = this.getOwner() instanceof LivingEntity owner
                        ? this.damageSources().mobAttack(owner)
                        : this.damageSources().generic();

                float damage = (this.getOwner() instanceof Player) ? 6.0F : 2.5F;

                if (target.hurt(source, damage)) {
                    target.setRemainingFireTicks(80);

                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                                target.getX(), target.getY(0.5D), target.getZ(),
                                15, 0.3D, 0.5D, 0.3D, 0.02D);
                    }
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            BlockPos hitPos = result.getBlockPos();
            BlockPos firePos = hitPos.relative(result.getDirection());

            // Swapped isEmptyBlock() for canBeReplaced() to allow it to burn away tall grass and snow layers
            if (this.level().getBlockState(firePos).canBeReplaced() && !this.level().getFluidState(firePos).isSource()) {
                BlockPos groundPos = firePos.below();
                this.level().setBlockAndUpdate(groundPos, Blocks.SOUL_SOIL.defaultBlockState());
                this.level().setBlockAndUpdate(firePos, Blocks.SOUL_FIRE.defaultBlockState());
            }
            this.discard();
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}