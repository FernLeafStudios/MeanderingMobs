package com.fernleaf.meanderingmobs.server.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class AuroraBlock extends Block {

    public AuroraBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentState, @NotNull net.minecraft.core.Direction side) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity instanceof LivingEntity living && hasAuroraWalker(living)) {
                return Shapes.block();
            }
        }
        // Fall through completely if they don't have the boots
        return Shapes.empty();
    }

    private boolean hasAuroraWalker(LivingEntity entity) {
        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty()) return false;

        ResourceKey<Enchantment> auroraWalkerKey = ResourceKey.create(
                Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath("meanderingmobs", "aurora_walker")
        );

        Holder<Enchantment> enchantmentHolder = entity.level().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(auroraWalkerKey).orElse(null);

        if (enchantmentHolder == null) return false;

        return boots.getEnchantmentLevel(enchantmentHolder) > 0;
    }

    @Override
    public void tick(@NotNull BlockState state, net.minecraft.server.level.ServerLevel level, @NotNull BlockPos pos, @NotNull net.minecraft.util.RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return false;
    }
}