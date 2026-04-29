package dev.superficialcake.dankhelper.handlers

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
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

    private fun scanScoreboard(client: Minecraft) {
        val scoreboard = client.level?.scoreboard ?: return

        for (team in scoreboard.playerTeams) {
            val fullLine = ChatFormatting.stripFormatting(team.playerPrefix.string + team.playerSuffix.string) ?: ""

            if (fullLine.contains("DBM", ignoreCase = true)) {
                val match = Regex("""[\d,]+""").find(fullLine)
                val currentTotalBM = match?.value?.replace(",", "")?.toLongOrNull() ?: continue

                if (initialBM == -1L) {
                    initialBM = currentTotalBM
                    lastSeenBM = currentTotalBM
                    logger.info("Session Start BM captured: $initialBM")
                }

                if (currentTotalBM < lastSeenBM){
                    initialBM -= lastSeenBM
                }

                lastSeenBM = currentTotalBM
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