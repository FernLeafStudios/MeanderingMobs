package com.fernleaf.meanderingmobs.server.command;

import com.fernleaf.meanderingmobs.server.entity.RuffianEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RuffianInspectCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ruffian")
                        .then(Commands.literal("inspect")
                                .executes(RuffianInspectCommand::inspectTargetRuffian)
                        )
        );
    }

    private static int inspectTargetRuffian(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Entity executor = source.getEntity();

        if (executor == null) {
            source.sendFailure(Component.literal("Only entities can inspect Ruffians."));
            return 0;
        }

        // Locate nearest Ruffian within 8 blocks of the player's line of sight/position
        Vec3 pos = executor.position();
        AABB searchBox = AABB.ofSize(pos, 16.0D, 16.0D, 16.0D);
        List<RuffianEntity> ruffians = executor.level().getEntitiesOfClass(RuffianEntity.class, searchBox);

        RuffianEntity target = ruffians.stream()
                .min(Comparator.comparingDouble(r -> r.distanceToSqr(executor)))
                .orElse(null);

        if (target == null) {
            source.sendFailure(Component.literal("No Ruffian nearby to inspect."));
            return 0;
        }

        // Directly query active traits from the PersonalityEngine
        Map<String, Float> traits = target.getPersonalityEngine().getActiveTraits();

        source.sendSuccess(() -> Component.literal("=== Ruffian Personality Report ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        traits.forEach((trait, value) -> {
            ChatFormatting color = value >= 0.7F ? ChatFormatting.GREEN
                    : value <= 0.3F ? ChatFormatting.RED
                    : ChatFormatting.YELLOW;

            String formattedValue = String.format("%.0f%%", value * 100.0F);

            source.sendSuccess(() -> Component.literal("• " + capitalize(trait) + ": ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(formattedValue).withStyle(color)), false);
        });

        return 1;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}