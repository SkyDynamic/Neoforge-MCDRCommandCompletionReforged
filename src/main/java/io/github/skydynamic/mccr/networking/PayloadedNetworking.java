package io.github.skydynamic.mccr.networking;

import io.github.skydynamic.mccr.Mccr;
import io.github.skydynamic.mccr.completion.CompletionResult;
import io.github.skydynamic.mccr.completion.CompletionService;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class PayloadedNetworking {
    private static final Long2ReferenceMap<CompletableFuture<CompletionResult>> futures = new Long2ReferenceLinkedOpenHashMap<>();
    private static final AtomicLong requestId = new AtomicLong();

    public static CompletableFuture<CompletionResult> requestCompletion(String content) {
        CompletableFuture<CompletionResult> future = new CompletableFuture<>();
        long id = requestId.addAndGet(1);
        futures.put(id, future);
        PacketDistributor.sendToServer(new ServerboundRequestCompletionPacket(content, id));
        return future;
    }

    public record ServerboundRequestCompletionPacket(String content, long session) implements CustomPacketPayload {
        public static final Type<ServerboundRequestCompletionPacket> TYPE = new Type<>(Mccr.location("request_completion"));
        public static final StreamCodec<ByteBuf, ServerboundRequestCompletionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ServerboundRequestCompletionPacket::content,
            ByteBufCodecs.VAR_LONG,
            ServerboundRequestCompletionPacket::session,
            ServerboundRequestCompletionPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(final ServerboundRequestCompletionPacket packet, final IPayloadContext context) {
            CompletionService.requestCompletion(context.player(), packet.content)
                .thenAccept(it -> context.reply(
                    new ClientboundCompletionResultPacket(it.getCompletion(), it.getHint(), packet.session))
                );
        }
    }

    public record ClientboundCompletionResultPacket(List<String> content, String hint, long session) implements CustomPacketPayload {
        public static final Type<ClientboundCompletionResultPacket> TYPE = new Type<>(Mccr.location("completion_result"));
        public static final StreamCodec<ByteBuf, ClientboundCompletionResultPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            ClientboundCompletionResultPacket::content,
            ByteBufCodecs.STRING_UTF8,
            ClientboundCompletionResultPacket::hint,
            ByteBufCodecs.VAR_LONG,
            ClientboundCompletionResultPacket::session,
            ClientboundCompletionResultPacket::new
        );


        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(final ClientboundCompletionResultPacket packet, final IPayloadContext context) {
            LoggerFactory.getLogger(PayloadedNetworking.class).info("Received completion result for content {}", packet.content.toString());
            CompletableFuture<CompletionResult> future = futures.get(packet.session);
            if (future != null) {
                future.complete(new CompletionResult(packet.content, packet.hint));
            }
        }
    }
}
