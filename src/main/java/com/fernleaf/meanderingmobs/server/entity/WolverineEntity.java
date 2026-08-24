package com.fernleaf.meanderingmobs.server.entity;

import com.fernleaf.meanderingmobs.server.entity.ai.TameableStateGoal;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class WolverineEntity extends MeanderingMobsTameableEntity {

    public static final TagKey<Item> WOLVERINE_TAMEABLE = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "wolverine_tame")
    );

    public static final TagKey<EntityType<?>> WOLVERINE_HATES = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "wolverine_hates")
    );

    public WolverineEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        // Core Movement & Action Goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TameableStateGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // Tamed Owner Protection Goals
        this.targetSelector.addGoal(1, new WolverineOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new WolverineOwnerHurtTargetGoal(this));

        // Self Defense
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());

        // Wild Aggression Tag Selector (Targets anything in #meanderingmobs:wolverine_hates when untamed)
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                this,
                LivingEntity.class,
                10,
                true,
                false,
                target -> !this.isTamed() && target.getType().is(WOLVERINE_HATES)
        ));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public boolean isSitting() {
        return !this.isTamed() || this.getAiState() != CommandState.SIT.id;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        if (!isTamed() && heldStack.is(WOLVERINE_TAMEABLE)) {
            if (!player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }

            if (!this.level().isClientSide()) {
                if (this.random.nextInt(3) == 0) {
                    tame(player);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (isTamed() && isOwner(player) && hand == InteractionHand.MAIN_HAND) {
            this.cycleAiState(player, "wolverine");
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    // --- Custom Owner Target Protection Goals ---

    private static class WolverineOwnerHurtByTargetGoal extends TargetGoal {
        private final WolverineEntity wolverine;
        private LivingEntity attacker;

        public WolverineOwnerHurtByTargetGoal(WolverineEntity wolverine) {
            super(wolverine, false);
            this.wolverine = wolverine;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (this.wolverine.isTamed() && this.wolverine.isSitting()) {
                LivingEntity owner = this.wolverine.getOwner();
                if (owner != null) {
                    this.attacker = owner.getLastHurtByMob();
                    return this.attacker != null && this.canAttack(this.attacker, TargetingConditions.DEFAULT);
                }
            }
            return false;
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacker);
            super.start();
        }
    }

    public static boolean checkWolverineSpawnRules(
            EntityType<WolverineEntity> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {

        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        if (level.getRawBrightness(pos, 0) < 8) {
            return false;
        }

        BlockState stateBelow = level.getBlockState(pos.below());
        return stateBelow.is(BlockTags.DIRT)
                || stateBelow.is(BlockTags.SAND)
                || stateBelow.is(BlockTags.SNOW)
                || stateBelow.is(Blocks.GRAVEL)
                || stateBelow.is(Blocks.STONE);
    }

    private static class WolverineOwnerHurtTargetGoal extends TargetGoal {
        private final WolverineEntity wolverine;
        private LivingEntity target;

        public WolverineOwnerHurtTargetGoal(WolverineEntity wolverine) {
            super(wolverine, false);
            this.wolverine = wolverine;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (this.wolverine.isTamed() && this.wolverine.isSitting()) {
                LivingEntity owner = this.wolverine.getOwner();
                if (owner != null) {
                    this.target = owner.getLastHurtMob();
                    return this.target != null && this.canAttack(this.target, TargetingConditions.DEFAULT);
                }
            }
            return false;
        }

        @Override
        public void start() {
            this.mob.setTarget(this.target);
            super.start();
        }
    }
}