package io.github.skydynamic.mccr.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.skydynamic.mccr.completion.CompletionService;
import net.minecraft.commands.CommandSourceStack;

public class MccrCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("configureCompletion")
            .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("endpoint", StringArgumentType.greedyString())
                .executes(context -> {
                    CompletionService.setEndpoint(context.getArgument("endpoint", String.class));
                    return 1;
                })
            )
        );
    }
}
