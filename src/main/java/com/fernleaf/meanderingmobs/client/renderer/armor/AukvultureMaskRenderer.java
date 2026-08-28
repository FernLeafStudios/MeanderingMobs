package com.fernleaf.meanderingmobs.client.renderer.armor;

import com.fernleaf.meanderingmobs.client.model.armor.AukvultureMaskModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class AukvultureMaskRenderer implements IClientItemExtensions {

    private AukvultureMaskModel<LivingEntity> model;

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> original) {
        if (this.model == null) {
            this.model = new AukvultureMaskModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(AukvultureMaskModel.LAYER_LOCATION));
        }

        original.copyPropertiesTo((HumanoidModel) this.model);

        this.model.hat.visible = false;
        this.model.leftArm.visible = false;
        this.model.rightArm.visible = false;
        this.model.leftLeg.visible = false;
        this.model.rightLeg.visible = false;

        this.model.head.visible = (slot == EquipmentSlot.HEAD);
        this.model.body.visible = (slot == EquipmentSlot.CHEST);

        return this.model;
    }
}