package com.fernleaf.meanderingmobs.server.item;

import com.fernleaf.meanderingmobs.registry.MeanderingMobsArmorMaterials;
import net.minecraft.world.item.ArmorItem;

public class AukvultureMaskItem extends ArmorItem {

    public AukvultureMaskItem(Properties properties) {
        super(MeanderingMobsArmorMaterials.AUKVULTURE_MASK_MATERIAL, Type.HELMET, properties);
    }
}