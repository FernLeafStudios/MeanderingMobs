package com.fernleaf.meanderingmobs.server.entity.projectile;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsEffectsRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsItemRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuillArrowEntity extends AbstractArrow {

    public QuillArrowEntity(EntityType<? extends QuillArrowEntity> entityType, Level level) {
        super(entityType, level);
    }

    public QuillArrowEntity(Level level, LivingEntity shooter, ItemStack pickupItemStack, @Nullable ItemStack weapon) {
        super(MeanderingMobsEntityRegistry.QUILL_ARROW.get(), shooter, level, pickupItemStack, weapon);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(MeanderingMobsItemRegistry.PORCUPINE_QUILL.get());
    }

    @Override
    protected void doPostHurtEffects(@NotNull LivingEntity target) {
        super.doPostHurtEffects(target);
        // Apply Quilled effect for 30 seconds (600 ticks)
        target.addEffect(new MobEffectInstance(MeanderingMobsEffectsRegistry.QUILLED, 600, 0));
    }
}