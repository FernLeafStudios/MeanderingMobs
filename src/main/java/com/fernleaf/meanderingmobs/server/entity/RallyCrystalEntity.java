package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.server.data.RallyWaveManager;
import com.fernleaf.meanderingmobs.server.data.RallyWavePattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RallyCrystalEntity extends Monster {

    private static final EntityDataAccessor<Boolean> IS_SINKING = SynchedEntityData.defineId(RallyCrystalEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SINK_TICKS = SynchedEntityData.defineId(RallyCrystalEntity.class, EntityDataSerializers.INT);

    public int time = 0;
    private int currentWave = 0;
    private int waveCooldown = 0;
    private boolean messageSent = false;
    private final List<UUID> activeWaveMobs = new ArrayList<>();

    private RallyWavePattern activePattern = null;

    private static final double DETECTION_RADIUS = 12.0;

    public RallyCrystalEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("unused")
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        // Leave completely empty so it stays rooted in place like a stationary structure!
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(@NotNull Entity entity) {
        // Prevent entities from nudging or pushing the crystal around
    }

    @Override
    public boolean canCollideWith(@NotNull Entity entity) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_SINKING, false);
        builder.define(SINK_TICKS, 0);
    }

    public static boolean checkRallyCrystalSpawnRules(
            EntityType<RallyCrystalEntity> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        BlockState stateBelow = level.getBlockState(pos.below());
        boolean isSoulBlock = stateBelow.is(Blocks.SOUL_SAND) || stateBelow.is(Blocks.SOUL_SOIL);
        return isSoulBlock && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return !source.is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);
        this.time++;

        if (this.level().isClientSide) {
            if (this.isSinking()) {
                this.level().addParticle(ParticleTypes.SOUL, this.getX() + (this.random.nextDouble() - 0.5), this.getY() + 0.2, this.getZ() + (this.random.nextDouble() - 0.5), 0, 0.05, 0);
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        if (this.isSinking()) {
            int ticks = this.getSinkTicks() + 1;
            this.setSinkTicks(ticks);

            if (ticks >= 60) {
                this.dropLootAndFinish(serverLevel);
                this.discard();
            }
            return;
        }

        // 1. Clean active mobs
        this.activeWaveMobs.removeIf(uuid -> {
            Entity entity = serverLevel.getEntity(uuid);
            return entity != null && !entity.isAlive();
        });

        // 2. Cooldown timer
        if (this.waveCooldown > 0) {
            this.waveCooldown--;
            return;
        }

        // 3. Player Proximity Detection
        Player nearbyPlayer = serverLevel.players().stream()
                .filter(p -> !p.isSpectator() && !p.isCreative() && p.distanceToSqr(this) <= (DETECTION_RADIUS * DETECTION_RADIUS))
                .findFirst()
                .orElse(null);

        if (nearbyPlayer == null) return;

        if (!this.messageSent) {
            Component message = Component.translatable("message.meanderingmobs.rally_crystal.emerge")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC);
            nearbyPlayer.displayClientMessage(message, true);
            this.messageSent = true;
        }

        // 4. Wave Management
        if (this.activeWaveMobs.isEmpty()) {
            if (this.activePattern == null) {
                RallyWaveManager.getRandomPattern(this.random).ifPresent(p -> this.activePattern = p);
            }

            if (this.activePattern == null || this.activePattern.waves().isEmpty()) {
                MeanderingMobs.LOGGER.warn("No Rally Wave patterns available to trigger.");
                this.setSinking(true);
                return;
            }

            int totalWaves = this.activePattern.waves().size();

            if (this.currentWave < totalWaves) {
                int nextWave = this.currentWave + 1;
                if (this.spawnWave(serverLevel, nextWave, nearbyPlayer)) {
                    this.currentWave = nextWave;
                    this.waveCooldown = 100; // 5 seconds
                }
            } else {
                this.setSinking(true);
            }
        }
    }

    private boolean spawnWave(ServerLevel level, int waveNumber, Player targetPlayer) {
        if (this.activePattern == null) return false;

        int waveIndex = waveNumber - 1;
        if (waveIndex >= this.activePattern.waves().size()) return false;

        RallyWavePattern.WaveEntry waveEntry = this.activePattern.waves().get(waveIndex);

        List<EntityType<?>> selectableTypes = new ArrayList<>();

        if (waveEntry.entityId().isPresent()) {
            EntityType.byString(waveEntry.entityId().get().toString()).ifPresent(selectableTypes::add);
        } else if (waveEntry.tag().isPresent()) {
            Optional<HolderSet.Named<EntityType<?>>> tagHolder = level.registryAccess()
                    .registryOrThrow(Registries.ENTITY_TYPE)
                    .getTag(waveEntry.tag().get());

            tagHolder.ifPresent(holders -> holders.forEach(h -> selectableTypes.add(h.value())));
        }

        if (selectableTypes.isEmpty()) return false;

        int min = waveEntry.minCount();
        int max = waveEntry.maxCount();
        int spawnCount = min >= max ? min : min + level.random.nextInt(max - min + 1);

        for (int i = 0; i < spawnCount; i++) {
            EntityType<?> selectedType = selectableTypes.get(level.random.nextInt(selectableTypes.size()));
            Vec3 spawnPos = this.getSpawnPosition(level);

            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, spawnPos.x, spawnPos.y + 0.2, spawnPos.z, 25, 0.4, 0.5, 0.4, 0.05);
            level.sendParticles(ParticleTypes.SOUL, spawnPos.x, spawnPos.y + 0.2, spawnPos.z, 15, 0.3, 0.3, 0.3, 0.02);

            Entity spawnedEntity = selectedType.create(level);
            if (spawnedEntity instanceof Mob mob) {
                mob.moveTo(spawnPos.x, spawnPos.y - 0.6D, spawnPos.z, level.random.nextFloat() * 360.0F, 0.0F);
                mob.setDeltaMovement(0.0D, 0.35D, 0.0D);
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(spawnPos)), MobSpawnType.SPAWNER, null);

                if (targetPlayer != null) {
                    mob.setTarget(targetPlayer);
                }

                level.addFreshEntity(mob);
                this.activeWaveMobs.add(mob.getUUID());
            }
        }

        return !this.activeWaveMobs.isEmpty();
    }

    private void dropLootAndFinish(ServerLevel level) {
        ResourceKey<LootTable> lootKey = ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "entities/rally_crystal")
        );
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(lootKey);

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ORIGIN, this.position())
                .create(LootContextParamSets.EMPTY);

        lootTable.getRandomItems(params).forEach(this::spawnAtLocation);

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.5, this.getZ(), 50, 0.5, 0.8, 0.5, 0.1);
    }

    private Vec3 getSpawnPosition(ServerLevel level) {
        double offsetAngle = level.random.nextDouble() * Math.PI * 2;
        double distance = 2.0 + level.random.nextDouble() * 3.0;
        return new Vec3(this.getX() + Math.cos(offsetAngle) * distance, this.getY(), this.getZ() + Math.sin(offsetAngle) * distance);
    }

    public boolean isSinking() { return this.entityData.get(IS_SINKING); }
    public void setSinking(boolean sinking) { this.entityData.set(IS_SINKING, sinking); }
    public int getSinkTicks() { return this.entityData.get(SINK_TICKS); }
    public void setSinkTicks(int ticks) { this.entityData.set(SINK_TICKS, ticks); }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.time = compound.getInt("Time");
        this.currentWave = compound.getInt("CurrentWave");
        this.waveCooldown = compound.getInt("WaveCooldown");
        this.setSinking(compound.getBoolean("IsSinking"));
        this.setSinkTicks(compound.getInt("SinkTicks"));
        this.messageSent = compound.getBoolean("MessageSent");
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Time", this.time);
        compound.putInt("CurrentWave", this.currentWave);
        compound.putInt("WaveCooldown", this.waveCooldown);
        compound.putBoolean("IsSinking", this.isSinking());
        compound.putInt("SinkTicks", this.getSinkTicks());
        compound.putBoolean("MessageSent", this.messageSent);
    }

    @Override
    protected @NotNull EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
        return EntityDimensions.scalable(0.8F, 2.5F);
    }

    @Override
    public void refreshDimensions() {
        super.refreshDimensions();
        this.setBoundingBox(this.makeBoundingBox());
    }
}