package dev.superficialcake.dankhelper.handlers

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

object ScoreboardHandler {
    private val logger = LoggerFactory.getLogger("DankHelper-Scoreboard")
    private var tickCounter = 0
    private var prevTitleColor: Int? = null
    var formattedSessionBM: String = "0"

    private const val SCAN_INTERVAL = 10

    private var initialBM: Long = -1L
    private var lastSeenBM: Long = -1L
    var sessionBM: Long = 0L
    var sunriseTime: String = "--:--"

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register(
            ClientTickEvents.EndTick { client ->
                tickCounter++

                if (tickCounter >= SCAN_INTERVAL) {
                    tickCounter = 0
                    scanScoreboard(client)
                }
            },
        )
    }

    private fun scanScoreboard(client: Minecraft) {
        val scoreboard = client.level?.scoreboard ?: return
        val sidebarObj = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR)
        val titleText = sidebarObj?.displayName ?: return

        var currentTitleColor = titleText.style.color?.value
        if (currentTitleColor == null) {
            currentTitleColor =
                titleText.siblings
                    .firstOrNull {
                        it.string.isNotBlank() && it.style.color != null
                    }?.style
                    ?.color
                    ?.value
        }

        if (prevTitleColor == null) {
            prevTitleColor = currentTitleColor
        } else if (prevTitleColor != currentTitleColor) {
            val now = LocalDateTime.now()
            val formattedTime = "0:%02d:%02d".format(now.minute % 10, now.second)

            logger.info(formattedTime)
            prevTitleColor = currentTitleColor

            sunriseTime = formattedTime
        }

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

                if (currentTotalBM < lastSeenBM) {
                    initialBM = lastSeenBM
                }

                lastSeenBM = currentTotalBM
                sessionBM = currentTotalBM - initialBM

                formattedSessionBM = "%,d".format(sessionBM)
            }
        }
    }

    fun reset() {
        initialBM = -1L
        lastSeenBM = -1L
        sessionBM = 0L
        prevTitleColor = null
    }
}
