package com.fernleaf.meanderingmobs.server.entity;

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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
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
public class HollowRuffianEntity extends Monster implements VibrationSystem {

    private final VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.User vibrationUser = new HollowRuffianVibrationUser();
    private final DynamicGameEventListener<VibrationSystem.Listener> vibrationListener =
            new DynamicGameEventListener<>(new VibrationSystem.Listener(this));

    // Cooldown ticks to prevent constant processing spam (e.g., 20 ticks = 1 second)
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
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
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

        if (this.vibrationCooldown > 0) {
            this.vibrationCooldown--;
        }
        if (this.shriekCooldown > 0) {
            this.shriekCooldown--;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
        }
    }

    public void triggerSculkShriek(ServerLevel level, LivingEntity targetPlayer) {
        if (this.shriekCooldown > 0) return;

        // Safety check: ensure target isn't a Creative/Spectator player or invulnerable target
        if (targetPlayer instanceof net.minecraft.world.entity.player.Player player) {
            if (player.isCreative() || player.isSpectator() || !player.canBeSeenAsEnemy()) {
                return;
            }
        }

        this.shriekCooldown = 100;

        level.playSound(null, this.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK,
                SoundSource.HOSTILE, 1.5F, 1.0F);
        level.sendParticles(ParticleTypes.SCULK_SOUL,
                this.getX(), this.getEyeY(), this.getZ(), 15, 0.3, 0.5, 0.3, 0.05);

        net.minecraft.world.phys.AABB searchBox = this.getBoundingBox().inflate(36.0D);
        level.getEntitiesOfClass(Monster.class, searchBox, mob -> mob != this && mob.isAlive()).forEach(hostile -> {
            if (hostile instanceof Creeper) {
                return;
            }

            if (targetPlayer.canBeSeenAsEnemy()) {
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

        // Use checkMonsterSpawnRules which handles standard surface/cave monster spawning criteria properly
        return level.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                && Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    // --- Vibration User Logic ---
    private class HollowRuffianVibrationUser implements VibrationSystem.User {
        private final PositionSource positionSource = new EntityPositionSource(HollowRuffianEntity.this, HollowRuffianEntity.this.getEyeHeight());

        @Override
        public int getListenerRadius() {
            return 16;
        }

        @Override
        public @NotNull PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public boolean canReceiveVibration(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull Holder<GameEvent> gameEvent, @Nullable GameEvent.Context context) {
            // 1. Check cooldown
            if (HollowRuffianEntity.this.vibrationCooldown > 0) {
                return false;
            }

            // 2. Ignore self-generated noises
            if (context != null && context.sourceEntity() == HollowRuffianEntity.this) {
                return false;
            }

            // 3. IGNORE THROWING/SHOOTING EVENTS (forces it to listen to the impact instead!)
            return !gameEvent.is(GameEvent.PROJECTILE_SHOOT) && !gameEvent.is(GameEvent.ITEM_INTERACT_START);
        }

        @Override
        public void onReceiveVibration(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull Holder<GameEvent> gameEvent, @Nullable Entity entity, @Nullable Entity projectileOwner, float distance) {
            HollowRuffianEntity.this.vibrationCooldown = 30;

            // Move to sound source
            HollowRuffianEntity.this.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.25D);

            // Only respond to valid living targets (players, etc.), ignoring monsters and non-enemies
            if (entity instanceof LivingEntity livingTarget
                    && !(livingTarget instanceof Monster)
                    && livingTarget.canBeSeenAsEnemy()
                    && !(entity instanceof HollowRuffianEntity)) {

                if (livingTarget instanceof net.minecraft.world.entity.player.Player player) {
                    if (player.isCreative() || player.isSpectator()) {
                        return;
                    }
                }

                HollowRuffianEntity.this.setTarget(livingTarget);
                if (distance <= 4.0F) {
                    HollowRuffianEntity.this.triggerSculkShriek(level, livingTarget);
                }
            }
        }
    }
}