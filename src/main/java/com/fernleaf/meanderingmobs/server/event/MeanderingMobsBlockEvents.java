package com.fernleaf.meanderingmobs.server.event;

import com.evandev.redomesticate.api.ICommandableMob;
import com.evandev.redomesticate.api.PetCommand;
import com.evandev.redomesticate.content.block.DrumBlock;
import com.evandev.redomesticate.util.TameableUtils;
import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.compat.redomesticate.RedomesticateCompat;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsBlockRegistry;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.server.block.CarvedStrippedSpruceLogBlock;
import com.fernleaf.meanderingmobs.server.block.pattern.GuttertankPattern;
import com.fernleaf.meanderingmobs.server.block.pattern.RuffianPattern;
import com.fernleaf.meanderingmobs.server.block.entity.CarvedStrippedSpruceLogBlockEntity;
import com.fernleaf.meanderingmobs.server.block.rune.RuneType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

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

    // --- Interaction Handler (Sword Logs + Redomesticate Drum Override) ---
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // Prevent off-hand duplicate triggers
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack stack = event.getItemStack();
        BlockState targetState = level.getBlockState(pos);
        Player player = event.getEntity();

        // 1. Turn vanilla stripped spruce logs into Carved Spruce Log using any Sword
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

                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(event.getHand()));
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            return;
        }

        // --- Redomesticate Drum Override ---
        if (RedomesticateCompat.isLoaded() && !level.isClientSide()) {

            if (targetState.getBlock() instanceof DrumBlock && !player.isShiftKeyDown()) {
                int currentCommand = targetState.getValue(DrumBlock.COMMAND);
                int newCommand = (currentCommand + 1) % 3;

                // 1. ADVANCE THE BLOCK STATE IN THE WORLD so it doesn't stay stuck on state 1!
                level.setBlock(pos, targetState.setValue(DrumBlock.COMMAND, newCommand), 3);

                AABB searchBox = new AABB(pos).inflate(32.0D);

                List<Dolphin> nearbyDolphins = level.getEntitiesOfClass(
                        Dolphin.class,
                        searchBox,
                        d -> d.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get()) &&
                                player.getUUID().equals(d.getData(MeanderingMobsAttachmentRegistry.OWNER_UUID.get()).orElse(null))
                );

                List<Mob> nearbyVanillaPets = level.getEntitiesOfClass(
                        Mob.class,
                        searchBox,
                        m -> !(m instanceof Dolphin) && TameableUtils.isTamed(m) && player.getUUID().equals(TameableUtils.getOwnerUUIDOf(m))
                );

                int totalAffected = nearbyDolphins.size() + nearbyVanillaPets.size();

                // 2. Command dolphins
                for (Dolphin dolphin : nearbyDolphins) {
                    RedomesticateCompat.setDolphinCommand(dolphin, newCommand);
                    dolphin.getNavigation().stop();
                    dolphin.setTarget(null);
                    dolphin.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false));
                }

                // 3. Command standard Redomesticate pets
                PetCommand targetPetCommand = switch (newCommand) {
                    case 0 -> PetCommand.WANDER;
                    case 1 -> PetCommand.SIT;
                    default -> PetCommand.FOLLOW;
                };

                for (Mob mob : nearbyVanillaPets) {
                    if (mob instanceof ICommandableMob commandable) {
                        commandable.redomesticate$setPetCommand(targetPetCommand);
                    }
                    if (mob instanceof TamableAnimal tamable) {
                        // Explicitly set false for non-sit commands so they stand back up
                        tamable.setOrderedToSit(targetPetCommand == PetCommand.SIT);
                    }
                    mob.getNavigation().stop();
                    mob.setTarget(null);
                    mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false));
                }

                if (totalAffected > 0) {
                    player.displayClientMessage(
                            Component.translatable("message.redomesticate.drum_command_" + newCommand, totalAffected),
                            true
                    );
                }

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            }
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