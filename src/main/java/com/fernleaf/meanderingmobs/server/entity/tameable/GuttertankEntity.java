package com.fernleaf.meanderingmobs.server.entity.tameable;

import com.fernleaf.meanderingmobs.client.model.deerfox.DeerfoxVariant;
import com.fernleaf.meanderingmobs.client.model.guttertank.GuttertankVariant;
import com.fernleaf.meanderingmobs.server.entity.ai.guttertank.GuttertankPunchGoal;
import com.fernleaf.meanderingmobs.server.entity.ai.guttertank.GuttertankShootGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class GuttertankEntity extends MeanderingMobsTameableEntity {

    private static final EntityDataAccessor<Boolean> SHOOTING = SynchedEntityData.defineId(GuttertankEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PUNCHING = SynchedEntityData.defineId(GuttertankEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState shootAnimationState = new AnimationState();
    public final AnimationState punchAnimationState = new AnimationState();

    private int playerShootTimer = 0;

    public GuttertankEntity(EntityType<? extends MeanderingMobsTameableEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        // Action Goals (Disabled if entity is currently mounted)
        this.goalSelector.addGoal(1, new GuttertankPunchGoal(this));
        this.goalSelector.addGoal(2, new GuttertankShootGoal(this));

        // Target Selectors (Only target players if untamed)
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                10,
                true,
                false,
                target -> !this.isTame() && !this.isVehicle()
        ));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHOOTING, false);
        builder.define(PUNCHING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D);
    }

    public boolean isShooting() { return this.entityData.get(SHOOTING); }
    public void setShooting(boolean shooting) { this.entityData.set(SHOOTING, shooting); }

    public boolean isPunching() { return this.entityData.get(PUNCHING); }
    public void setPunching(boolean punching) { this.entityData.set(PUNCHING, punching); }

    public GuttertankVariant getVariant() { return GuttertankVariant.byId(this.getVariantId()); }
    public void setVariant(GuttertankVariant variant) { this.setVariantId(variant.id); }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(@NotNull Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(-0.7D, dimensions.height() * 1.2D, 0.0D);
    }

    // --- Interaction & Mounting Support ---
    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (isTamed() && isOwner(player)) {
            if (player.isSecondaryUseActive()) {
                // Shift-Right-Click cycles AI commands (WANDER, SIT, FOLLOW)
                return super.mobInteract(player, hand);
            } else if (!this.isVehicle() && hand == InteractionHand.MAIN_HAND) {
                // Right-Click mounts entity
                if (!this.level().isClientSide()) {
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void tickRidden(@NotNull Player player, @NotNull Vec3 travelVector) {
        super.tickRidden(player, travelVector);

        // 1. INSTANT SYNC: Prevents all camera stutter and server desync
        this.setRot(player.getYRot(), player.getXRot() * 0.5F);
        this.yHeadRot = this.getYRot();

        // 2. VISUAL LAG: Gives the heavy mech feel without breaking hitboxes
        this.yBodyRot = Mth.rotLerp(0.1F, this.yBodyRot, this.getYRot());

        // 3. Attack logic
        if (player.isUsingItem() || player.swinging) {
            this.triggerRiddenShootSequence(player);
        } else if (this.playerShootTimer > 0) {
            this.playerShootTimer--;
            if (this.playerShootTimer == 0) {
                this.setShooting(false);
            }
        }
    }

    public void triggerRiddenShootSequence(Player rider) {
        this.setShooting(true);
        this.playerShootTimer = 10; // Keep shoot state active briefly

        if (this.tickCount % 4 == 0 && !this.level().isClientSide()) {
            Vec3 look = rider.getLookAngle();
            double spawnX = this.getX() + look.x * 2.5D;
            double spawnY = this.getY(0.85D) + look.y;
            double spawnZ = this.getZ() + look.z * 2.5D;

            SmallFireball fireball = new SmallFireball(this.level(), spawnX, spawnY, spawnZ, look);
            fireball.setOwner(this);
            this.level().addFreshEntity(fireball);
            fireball.setOwner(rider);
            this.level().addFreshEntity(fireball);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if (this.isPunching()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.shootAnimationState.stop();
            this.punchAnimationState.startIfStopped(this.tickCount);
            return;
        } else {
            this.punchAnimationState.stop();
        }

        if (this.isShooting()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.shootAnimationState.startIfStopped(this.tickCount);
            return;
        } else {
            this.shootAnimationState.stop();
        }

        if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D) {
            this.idleAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }
}