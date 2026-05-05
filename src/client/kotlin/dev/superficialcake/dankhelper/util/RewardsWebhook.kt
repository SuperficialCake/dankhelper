package dev.superficialcake.dankhelper.util

import dev.superficialcake.dankhelper.config.DankConfig
import me.shedaniel.autoconfig.AutoConfig
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object RewardsWebhook {
    private val logger = LoggerFactory.getLogger("DankHelper-Webhook")

    fun sendReward(
        username: String,
        uuid: String,
        action: String,
        count: String,
        reward: String,
    ) {
        val config = AutoConfig.getConfigHolder(DankConfig::class.java).config
        val webhook = config.webhookURL

        if (config.webhookURL.isBlank()) return
        val httpClient = HttpClient.newHttpClient()

        val uri =
            try {
                URI.create(webhook).also {
                    if (it.scheme == null || !it.scheme.startsWith("http")) {
                        throw IllegalArgumentException("Missing Exception")
                    }
                }
            } catch (e: Exception) {
                logger.error("Invalid Webhook URL in config: $webhook. Error ${e.message}")
                return
            }

        val discordEmbed =
            """{
                "content": null,
                "embeds": [
                {
                    "color": 65535,
                    "author": {
                    "name": "$username has $action ${count}x $reward",
                    "icon_url": "http://minotar.net/avatar/$uuid/64"
                }
                }
                ],
                "attachments": []
            }"""

        val request =
            HttpRequest
                .newBuilder()
                .uri(uri)
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(discordEmbed))
                .build()

        httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept { response ->
                if (response.statusCode() in 200..299) {
                    logger.info("Successfully sent webhook!")
                } else {
                    logger.warn("Error posting webhook ${response.statusCode()}")
                }
            }.exceptionally { exception ->
                logger.info("There was an error", exception)
                null
            }
    }
}
