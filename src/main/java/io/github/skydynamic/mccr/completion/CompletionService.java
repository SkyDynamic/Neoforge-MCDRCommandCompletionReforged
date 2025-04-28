package io.github.skydynamic.mccr.completion;

import com.google.gson.Gson;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class CompletionService {
    private static final Logger logger = LoggerFactory.getLogger("CompletionService");
    private static String endpoint = "";
    private static final HttpClient client = HttpClient.newBuilder().build();
    public static final Gson gson = new Gson();

    public static CompletableFuture<CompletionResult> requestCompletion(Player player, String command) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI("http://%s/completion?player_name=%s&command=%s".formatted(
                        endpoint,
                        player.getGameProfile().getName(),
                        URLEncoder.encode(command, Charset.defaultCharset())
                    ))
                )
                .version(HttpClient.Version.HTTP_1_1)
                .build();
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(it -> gson.fromJson(it.body(), CompletionResult.class));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setEndpoint(String endpoint) {
        if (endpoint.startsWith("localhost:")) {
            endpoint = "127.0.0.1" + endpoint.substring(9);
        }
        CompletionService.endpoint = endpoint;
        logger.info("Completion Endpoint configured at {}.", endpoint);
    }
}