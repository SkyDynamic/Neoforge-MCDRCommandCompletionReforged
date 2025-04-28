package io.github.skydynamic.mccr.mixin;

import com.mojang.brigadier.CommandDispatcher;
import io.github.skydynamic.mccr.command.MccrCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {
    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    void onRegisterCommands(
        Commands.CommandSelection selection,
        CommandBuildContext context,
        CallbackInfo ci
    ){
        MccrCommand.register(dispatcher);
    }
}
