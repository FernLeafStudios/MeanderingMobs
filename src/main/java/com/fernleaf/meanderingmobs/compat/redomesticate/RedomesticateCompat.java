package com.fernleaf.meanderingmobs.compat.redomesticate;

import com.evandev.redomesticate.api.PetCommand;
import com.fernleaf.meanderingmobs.registry.MeanderingMobsAttachmentRegistry;
import com.fernleaf.meanderingmobs.server.entity.util.MeanderingMobsTameableEntity;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

@SuppressWarnings("unused")
public class RedomesticateCompat {
    public static final String MOD_ID = "redomesticate";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

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

    public static void setDolphinCommand(Dolphin dolphin, int commandId) {
        if (!isLoaded()) return;
        int state = switch (commandId) {
            case 1 -> 1;
            case 2 -> 2;
            default -> 0;
        };
        dolphin.setData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get(), state);
    }

    public static int getDolphinCommand(Dolphin dolphin) {
        if (!isLoaded()) return 0;
        return dolphin.getData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get());
    }

    public static boolean isDolphinStayingStill(Dolphin dolphin) {
        if (!isLoaded()) return false;
        boolean isTamed = dolphin.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
        int state = dolphin.getData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get());
        return isTamed && state == 1;
    }

    public static boolean isDolphinFollowing(Dolphin dolphin) {
        if (!isLoaded()) return false;
        boolean isTamed = dolphin.getData(MeanderingMobsAttachmentRegistry.IS_TAMED.get());
        int state = dolphin.getData(MeanderingMobsAttachmentRegistry.COMMAND_STATE.get());
        return isTamed && state == 2;
    }
}