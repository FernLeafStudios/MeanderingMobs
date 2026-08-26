package com.fernleaf.meanderingmobs.server.block;

import com.fernleaf.meanderingmobs.server.entity.tameable.DeerfoxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class AuroraBlock extends Block {

    private static final ResourceKey<Enchantment> AURORA_WALKER_KEY = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("meanderingmobs", "aurora_walker")
    );

    public AuroraBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentState, @NotNull Direction side) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof LivingEntity living) {
            if (hasAuroraWalker(living)) {
                return Shapes.block();
            }
        }
        return Shapes.empty();
    }

    private boolean hasAuroraWalker(LivingEntity entity) {
        if (entity instanceof DeerfoxEntity || entity.getVehicle() instanceof DeerfoxEntity || entity.getFirstPassenger() instanceof DeerfoxEntity) {
            return true;
        }

        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty()) {
            return false;
        }

        return entity.level().registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(AURORA_WALKER_KEY))
                .map(enchantment -> EnchantmentHelper.getItemEnchantmentLevel(enchantment, boots) > 0)
                .orElse(false);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return false;
    }
}