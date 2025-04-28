package io.github.skydynamic.mccr.networking;

import io.github.skydynamic.mccr.completion.CompletionResult;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.concurrent.CompletableFuture;

public class Networking {
    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(
            PayloadedNetworking.ServerboundRequestCompletionPacket.TYPE,
            PayloadedNetworking.ServerboundRequestCompletionPacket.STREAM_CODEC,
            PayloadedNetworking.ServerboundRequestCompletionPacket::handle
        );
        registrar.playToClient(
            PayloadedNetworking.ClientboundCompletionResultPacket.TYPE,
            PayloadedNetworking.ClientboundCompletionResultPacket.STREAM_CODEC,
            PayloadedNetworking.ClientboundCompletionResultPacket::handle
        );
    }

    public static CompletableFuture<CompletionResult> requestCompletion(String content) {
        return PayloadedNetworking.requestCompletion(content);
    }
}
