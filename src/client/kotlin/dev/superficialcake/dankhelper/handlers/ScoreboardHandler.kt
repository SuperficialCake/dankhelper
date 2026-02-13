package dev.superficialcake.dankhelper.handlers

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Formatting
import org.slf4j.LoggerFactory

object ScoreboardHandler{

    private val logger = LoggerFactory.getLogger("DankHelper-Scoreboard")
    private var tickCounter = 0
    var formattedSessionBM: String = "0"

    private const val SCAN_INTERVAL = 10

    private var initialBM: Long = -1L
    private var lastSeenBM: Long = -1L
    var sessionBM: Long = 0L

    fun init(){
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            tickCounter++

            if(tickCounter >= SCAN_INTERVAL){
                tickCounter = 0
                scanScoreboard(client)
            }
        })
    }

    private fun scanScoreboard(client: MinecraftClient) {
        val scoreboard = client.world?.scoreboard ?: return

        for (team in scoreboard.teams) {
            val fullLine = Formatting.strip(team.prefix.string + team.suffix.string) ?: ""

            if (fullLine.contains("DBM", ignoreCase = true)) {
                val match = Regex("""[\d,]+""").find(fullLine)
                val currentTotalBM = match?.value?.replace(",", "")?.toLongOrNull() ?: continue

                // 1. If this is the first time we see the value, set the offset
                if (initialBM == -1L) {
                    initialBM = currentTotalBM
                    lastSeenBM = currentTotalBM
                    logger.info("Session Start BM captured: $initialBM")
                }

                // 2. Calculate session progress
                sessionBM = currentTotalBM - initialBM

                formattedSessionBM = "%,d".format(sessionBM)
            }
        }
    }

    fun reset(){
        initialBM = -1L
        lastSeenBM = -1L
        sessionBM = 0L
    }
}