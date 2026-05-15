package dev.superficialcake.dankhelper

import dev.superficialcake.dankhelper.config.DankConfig
import dev.superficialcake.dankhelper.handlers.KeybindHandler
import dev.superficialcake.dankhelper.handlers.MessageHandler
import dev.superficialcake.dankhelper.handlers.ScoreboardHandler
import dev.superficialcake.dankhelper.handlers.StatsManager
import dev.superficialcake.dankhelper.ui.DankHud
import dev.superficialcake.dankhelper.util.UtilFunctions
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import org.slf4j.LoggerFactory

object DankHelperClient : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("dankhelper")
    private var tickCounter = 0

    var isConnected: Boolean = false
    var startTime: Long = 0L
    var initialSession: Boolean = true

    override fun onInitializeClient() {
        AutoConfig.register(DankConfig::class.java) { definition, configClass ->
            GsonConfigSerializer(definition, configClass)
        }

        val config = AutoConfig.getConfigHolder(DankConfig::class.java).config

        HudRenderCallback.EVENT.register(DankHud)
        KeybindHandler.init()
        ScoreboardHandler.init()
        ClientReceiveMessageEvents.GAME.register(MessageHandler::onGameMessage)

        ClientPlayConnectionEvents.JOIN.register {
            handler: ClientPlayNetworkHandler,
            sender: PacketSender,
            client: MinecraftClient,
            ->
            val serverData = client.currentServerEntry
            val ipAddress = serverData?.address?.lowercase() ?: ""

            UtilFunctions.resetAll()
            if (ipAddress == "dankprison.com" || ipAddress.contains("dankprison")) {
                if (initialSession) {
                    UtilFunctions.showToast("Started Session", "Started logging Mining Summaries. New CSV Generated")
                    initialSession = false
                }

                logger.info("Connected to DankPrison")
                startTime = System.currentTimeMillis()
                isConnected = true
            } else {
                isConnected = false
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            if (isConnected && config.stretchReminder) {
                tickCounter++
                if (tickCounter >= 72000) {
                    UtilFunctions.showToast(
                        "An hour has passed!",
                        "An hour has passed, stretch your legs and get some water!",
                    )
                    tickCounter = 0
                }
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            StatsManager.forceSave()
            isConnected = false
            tickCounter = 0
        }
    }
}
