package com.fernleaf.meanderingmobs.server.entity.hostile;

import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsHostileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class HollowRuffianEntity extends MeanderingMobsHostileEntity implements VibrationSystem {

    private final VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.User vibrationUser = new HollowRuffianVibrationUser();
    private final DynamicGameEventListener<VibrationSystem.Listener> vibrationListener =
            new DynamicGameEventListener<>(new VibrationSystem.Listener(this));

    private int vibrationCooldown = 0;
    private int shriekCooldown = 0;

    public HollowRuffianEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this,
                LivingEntity.class,
                10,
                true,
                false,
                entity -> !(entity instanceof Monster) && entity.canBeSeenAsEnemy()
        ));
    }

    @Override
    public VibrationSystem.@NotNull Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.@NotNull User getVibrationUser() {
        return this.vibrationUser;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.vibrationCooldown > 0) this.vibrationCooldown--;
        if (this.shriekCooldown > 0) this.shriekCooldown--;

        if (this.level() instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
        }
    }

    public void triggerSculkShriek(ServerLevel level, LivingEntity targetPlayer) {
        if (this.shriekCooldown > 0) return;
        if (targetPlayer instanceof Player player && !this.isValidPlayerTarget(player)) return;

        this.shriekCooldown = 100;

        level.playSound(null, this.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK,
                SoundSource.HOSTILE, 1.5F, 1.0F);
        level.sendParticles(ParticleTypes.SCULK_SOUL,
                this.getX(), this.getEyeY(), this.getZ(), 15, 0.3, 0.5, 0.3, 0.05);

        level.getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(36.0D), mob -> mob != this && mob.isAlive())
                .forEach(hostile -> {
                    if (!(hostile instanceof Creeper) && targetPlayer.canBeSeenAsEnemy()) {
                        hostile.setTarget(targetPlayer);
                        hostile.getNavigation().moveTo(targetPlayer, 1.25D);
                    }
                });
    }

    @Override
    public void updateDynamicGameEventListener(@NotNull java.util.function.BiConsumer<DynamicGameEventListener<?>, ServerLevel> consumer) {
        if (this.level() instanceof ServerLevel serverLevel) {
            consumer.accept(this.vibrationListener, serverLevel);
        }
    }

    public static boolean checkHollowRuffianSpawnRules(
            EntityType<HollowRuffianEntity> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        return level.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                && Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    private class HollowRuffianVibrationUser implements VibrationSystem.User {
        private final PositionSource positionSource = new EntityPositionSource(HollowRuffianEntity.this, HollowRuffianEntity.this.getEyeHeight());

        @Override
        public int getListenerRadius() { return 16; }

        @Override
        public @NotNull PositionSource getPositionSource() { return this.positionSource; }

        @Override
        public boolean canReceiveVibration(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull Holder<GameEvent> gameEvent, @Nullable GameEvent.Context context) {
            if (HollowRuffianEntity.this.vibrationCooldown > 0) return false;
            if (context != null && context.sourceEntity() == HollowRuffianEntity.this) return false;

            return !gameEvent.is(GameEvent.PROJECTILE_SHOOT) && !gameEvent.is(GameEvent.ITEM_INTERACT_START);
        }

        @Override
        public void onReceiveVibration(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull Holder<GameEvent> gameEvent, @Nullable Entity entity, @Nullable Entity projectileOwner, float distance) {
            HollowRuffianEntity.this.vibrationCooldown = 30;
            HollowRuffianEntity.this.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.25D);

            if (entity instanceof LivingEntity livingTarget && !(livingTarget instanceof Monster) && livingTarget.canBeSeenAsEnemy()) {

                if (livingTarget instanceof Player player && !HollowRuffianEntity.this.isValidPlayerTarget(player)) {
                    return;
                }

                HollowRuffianEntity.this.setTarget(livingTarget);
                if (distance <= 4.0F) {
                    HollowRuffianEntity.this.triggerSculkShriek(level, livingTarget);
                }
            }
        }
    }
}