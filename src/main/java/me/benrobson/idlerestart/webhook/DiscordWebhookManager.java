package me.benrobson.idlerestart.webhook;

import me.benrobson.idlerestart.platform.PlatformAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DiscordWebhookManager {
    private final PlatformAdapter platform;
    private final HttpClient httpClient;

    public DiscordWebhookManager(PlatformAdapter platform) {
        this.platform = platform;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void sendRestartNotification(String reason) {
        if (!platform.isDiscordWebhookEnabled()) {
            return;
        }

        String webhookUrl = platform.getDiscordWebhookUrl();
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            platform.warning("Discord webhook is enabled, but the webhook URL is not set.");
            return;
        }

        String serverName = platform.getDiscordServerName();
        String jsonPayload = buildJsonPayload(serverName, reason);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        platform.info("Successfully sent Discord webhook notification.");
                    } else {
                        platform.warning("Failed to send Discord webhook notification. Status code: " + response.statusCode() + ", Body: " + response.body());
                    }
                })
                .exceptionally(e -> {
                    platform.severe("An exception occurred while sending Discord webhook notification: " + e.getMessage());
                    return null;
                });
    }

    private String buildJsonPayload(String serverName, String reason) {
        // Simple JSON string building. For more complex payloads, a JSON library would be better.
        return "{\"content\":\"\",\"embeds\":[{\"title\":\"Server Restarting\",\"description\":\"The server `" + serverName + "` is restarting.\",\"fields\":[{\"name\":\"Reason\",\"value\":\"" + reason + "\",\"inline\":false}],\"color\":15158332}]}";
    }
}
