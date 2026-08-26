package com.fernleaf.meanderingmobs.server.event;

import com.fernleaf.meanderingmobs.MeanderingMobs;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsEntityRegistry;
import com.fernleaf.meanderingmobs.server.block.GuttertankPattern;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = MeanderingMobs.MODID)
public class GuttertankSpawnHandler {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getPlacedBlock().is(Blocks.CARVED_PUMPKIN) || event.getPlacedBlock().is(Blocks.JACK_O_LANTERN)) {
            Player player = event.getEntity() instanceof Player p ? p : null;

            GuttertankPattern.trySpawnGuttertank(
                    (net.minecraft.world.level.Level) event.getLevel(),
                    event.getPos(),
                    MeanderingMobsEntityRegistry.GUTTERTANK.get(),
                    player
            );
        }
    }
}