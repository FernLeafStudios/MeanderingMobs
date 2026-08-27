package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.server.block.CarvedStrippedSpruceLogBlock;
import com.fernleaf.meanderingmobs.server.block.entity.CarvedStrippedSpruceLogBlockEntity;
import com.fernleaf.meanderingmobs.server.block.GuttertankPattern;
import com.fernleaf.meanderingmobs.server.block.RuffianPattern;
import com.fernleaf.meanderingmobs.server.block.rune.RuneType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class MeanderingMobsBlockEvents {

    // --- Aurora Walker Boots Handler ---
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Level level = player.level();
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (boots.isEmpty()) return;

            ResourceKey<Enchantment> auroraWalkerKey = ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath("meanderingmobs", "aurora_walker")
            );

            Holder<Enchantment> enchantmentHolder = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .get(auroraWalkerKey).orElse(null);

            if (enchantmentHolder == null) return;

            int levelEnchant = boots.getEnchantmentLevel(enchantmentHolder);

            if (levelEnchant > 0) {
                boolean isAirborne = !player.onGround() && !player.isInWater() && !player.isFallFlying();

                if (isAirborne && player.getDeltaMovement().y > -0.4D) {
                    BlockPos targetPos = player.blockPosition().below();

                    if (level.getBlockState(targetPos).isAir() && boots.getDamageValue() < boots.getMaxDamage() - 1) {
                        level.setBlock(targetPos, MeanderingMobsBlockRegistry.AURORA_BLOCK.get().defaultBlockState(), 3);
                        boots.hurtAndBreak(1, player, EquipmentSlot.FEET);
                        level.scheduleTick(targetPos, MeanderingMobsBlockRegistry.AURORA_BLOCK.get(), 60);
                    }
                }
            }
        }
    }

    // --- Sword Log Carving Handler ---
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack stack = event.getItemStack();
        BlockState targetState = level.getBlockState(pos);

        // Turn vanilla stripped spruce logs into Carved Spruce Log using any Sword
        if (stack.getItem() instanceof SwordItem && targetState.is(Blocks.STRIPPED_SPRUCE_LOG)) {
            if (!level.isClientSide) {
                BlockState newBlockState = MeanderingMobsBlockRegistry.CARVED_STRIPPED_SPRUCE_LOG.get()
                        .defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, targetState.getValue(RotatedPillarBlock.AXIS))
                        .setValue(CarvedStrippedSpruceLogBlock.RUNE_ID, 0);

                level.setBlock(pos, newBlockState, 3);

                if (level.getBlockEntity(pos) instanceof CarvedStrippedSpruceLogBlockEntity blockEntity) {
                    blockEntity.setRuneType(RuneType.DEERFOX);
                }

                stack.hurtAndBreak(1, event.getEntity(), LivingEntity.getSlotForHand(event.getHand()));
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        }
    }

    // --- Block Placement Pattern Checks ---
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getPlacedBlock().is(BlockTags.WOOL) ||
                event.getPlacedBlock().is(Blocks.CARVED_PUMPKIN) ||
                event.getPlacedBlock().is(Blocks.JACK_O_LANTERN)) {

            Player player = event.getEntity() instanceof Player p ? p : null;

            RuffianPattern.trySpawnRuffian(
                    (Level) event.getLevel(),
                    event.getPos(),
                    MeanderingMobsEntityRegistry.RUFFIAN.get(),
                    player
            );
        }

        if (event.getPlacedBlock().is(Blocks.CARVED_PUMPKIN) || event.getPlacedBlock().is(Blocks.JACK_O_LANTERN)) {
            Player player = event.getEntity() instanceof Player p ? p : null;

            GuttertankPattern.trySpawnGuttertank(
                    (Level) event.getLevel(),
                    event.getPos(),
                    MeanderingMobsEntityRegistry.GUTTERTANK.get(),
                    player
            );
        }
    }
}