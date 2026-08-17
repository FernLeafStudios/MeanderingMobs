package com.fernleaf.meanderingmobs.server.item;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.server.entity.projectile.QuillArrowEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class PorcupineQuillItem extends ArrowItem {

    public PorcupineQuillItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull AbstractArrow createArrow(@NotNull Level level, @NotNull ItemStack ammo, @NotNull LivingEntity shooter, ItemStack weapon) {
        return new QuillArrowEntity(level, shooter, ammo.copyWithCount(1), weapon);
    }

    @Override
    public @NotNull Projectile asProjectile(@NotNull Level level, Position pos, @NotNull ItemStack stack, @NotNull Direction direction) {
        QuillArrowEntity arrow = new QuillArrowEntity(MeanderingMobsEntityRegistry.QUILL_ARROW.get(), level);
        arrow.setPos(pos.x(), pos.y(), pos.z());
        return arrow;
    }
}