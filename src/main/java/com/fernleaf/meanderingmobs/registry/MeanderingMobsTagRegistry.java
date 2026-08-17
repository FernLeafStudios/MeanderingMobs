package com.fernleaf.meanderingmobs.registry;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class MeanderingMobsTagRegistry {

    public static class Items {
        public static final TagKey<Item> AUKVULTURE_TAME_FOOD =
                ItemTags.create(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "aukvulture_tame"));

        public static final TagKey<Item> PORCUPINE_TAME = TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("meanderingmobs", "porcupine_tame")
        );
    }

    public static class Blocks {
        public static final TagKey<Block> WHISP_PHASE_THROUGH =
                BlockTags.create(ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "whisp_phase_through"));
    }

    public static class EntityTypes {
        public static final TagKey<EntityType<?>> WHISP_INFLICT_PACIFISM =
                TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "whisp_inflict_pacifism"));

        public static final TagKey<EntityType<?>> SOUL_ORB_BLACKLISTED = TagKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "soul_orb_blacklisted")
        );

        public static final TagKey<EntityType<?>> PORCUPINE_HATES = TagKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath("meanderingmobs", "porcupine_hates")
        );
    }

    public static class Biomes {
        public static final TagKey<Biome> SPAWNS_COLD_PORCUPINES =
                TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "spawns_cold_porcupines"));
        public static final TagKey<Biome> SPAWNS_WARM_PORCUPINES =
                TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MeanderingMobs.MODID, "spawns_warm_porcupines"));
    }
}