package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class MeanderingMobsArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, MeanderingMobs.MODID);

    public static final Holder<ArmorMaterial> AUKVULTURE_MASK_MATERIAL = ARMOR_MATERIALS.register("aukvulture_mask",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.HELMET, 2); // Defense value
                    }),
                    15, // Enchantability
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.EMPTY,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "aukvulture_mask"))),
                    0.0F, // Toughness
                    0.0F  // Knockback Resistance
            ));

    public static void register(IEventBus eventBus) {
        ARMOR_MATERIALS.register(eventBus);
    }
}