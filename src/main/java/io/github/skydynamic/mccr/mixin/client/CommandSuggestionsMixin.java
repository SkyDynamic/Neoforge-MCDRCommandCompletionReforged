package io.github.skydynamic.mccr.mixin.client;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.skydynamic.mccr.completion.CompletionResult;
import io.github.skydynamic.mccr.networking.Networking;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow
    @Final
    EditBox input;

    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private static int getLastWordIndex(String text) {
        return 0;
    }

    @Shadow
    @Nullable
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    boolean keepSuggestions;

    @Shadow
    public abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Shadow
    @Final
    private List<FormattedCharSequence> commandUsage;

    @Shadow
    private int commandUsageWidth;

    @Shadow
    @Final
    Font font;

    @Shadow
    private int commandUsagePosition;

    @Shadow
    @Final
    Minecraft minecraft;

    @Inject(
        method = "updateCommandInfo()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/EditBox;getValue()Ljava/lang/String;"
        ),
        cancellable = true
    )
    private void onSuggestCommand(CallbackInfo ci) {
        // do nothing when server hasn't networking channel
        if (this.minecraft.getSingleplayerServer() != null) {
            return;
        }
        String text = this.input.getValue();
        if (text.startsWith("!") || text.startsWith("！")) {
            text = text.replace('！', '!');
            String command = text.substring(0, this.input.getCursorPosition());
            if (!this.keepSuggestions) {
                Networking.requestCompletion(command)
                    .thenAccept(it -> this.minecraft.execute(() -> mccr$applySuggestion(command, it)));
                ci.cancel();
            }
        }
    }

    @Unique
    public void mccr$applySuggestion(String command, CompletionResult suggestion) {
        this.input.setSuggestion(null);
        this.pendingSuggestions = null;
        this.suggestions = null;
        if (suggestion.getCompletion().isEmpty()) {
            if (suggestion.getHint().isEmpty()) {
                return;
            }
            FormattedCharSequence text = FormattedCharSequence.forward(suggestion.getHint(), Style.EMPTY.withColor(ChatFormatting.GRAY));
            this.commandUsage.clear();
            this.commandUsage.add(text);
            int width = this.font.width(text);
            this.commandUsageWidth = width;
            this.commandUsagePosition = Mth.clamp(
                this.input.getScreenX(getLastWordIndex(command)),
                0,
                this.input.getScreenX(0) + this.input.getInnerWidth() - width
            );
        } else {
            this.pendingSuggestions = SharedSuggestionProvider.suggest(suggestion.getCompletion(), new SuggestionsBuilder(command, getLastWordIndex(command)));
            this.pendingSuggestions.thenRun(() -> {
                if (this.pendingSuggestions.isDone()) {
                    this.commandUsage.clear();
                    this.showSuggestions(false);
                }
            });
        }
    }
}
