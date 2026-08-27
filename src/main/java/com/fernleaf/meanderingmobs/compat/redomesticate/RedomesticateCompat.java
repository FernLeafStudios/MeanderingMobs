package com.fernleaf.meanderingmobs.compat.redomesticate;

import com.evandev.redomesticate.api.PetCommand;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public class RedomesticateCompat {
    public static final String MOD_ID = "redomesticate";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    // Only executed when isLoaded() is true!
    public static void processCommand(MeanderingMobsTameableEntity entity, Player owner, int commandId) {
        PetCommand command = PetCommand.fromId(commandId);
        entity.setAiState(command.getId());

        if (!owner.level().isClientSide()) {
            owner.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.redomesticate.command_" + command.getId(),
                            entity.getName()
                    ),
                    true
            );
        }
    }
}