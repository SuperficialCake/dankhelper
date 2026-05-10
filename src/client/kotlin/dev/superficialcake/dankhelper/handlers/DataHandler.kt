package dev.superficialcake.dankhelper.handlers

import dev.superficialcake.dankhelper.util.UtilFunctions
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import org.slf4j.LoggerFactory
import java.io.File
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DataHandler {
    private val gameDir: File = MinecraftClient.getInstance().runDirectory
    private val logger = LoggerFactory.getLogger("DankHelper-DataHandler")

    private val rootFolder: File = File(gameDir, "dankhelper")
    private val trendsFolder: File = File(rootFolder, "logs")
    private val frenzyRoot: File = File(rootFolder, "frenzies")

    private lateinit var currentSessionFile: File
    private lateinit var currentCFFile: File
    private lateinit var currentDayFile: File

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")

    private fun getUtcDateTime(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

    private fun getUtcDateString(): String = getUtcDateTime().format(DATE_FORMATTER)

    fun init() {
        if (!rootFolder.exists()) rootFolder.mkdirs()
        if (!trendsFolder.exists()) trendsFolder.mkdirs()

        prepareSessionFile()
        prepareTrendFile()
        registerMidnightRollover()
    }

    private fun registerMidnightRollover() {
        var lastRecordedDate = getUtcDateString()
        var tickCounter = 0

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            tickCounter++
            if (tickCounter < 20) return@register
            tickCounter = 0

            val currentDate = getUtcDateString()
            if (currentDate != lastRecordedDate) {
                lastRecordedDate = currentDate
                prepareTrendFile()
                logger.info("Date rolled over to $currentDate. Started new session file.")
                UtilFunctions.resetAll()
            }
        }
    }

    private fun prepareSessionFile() {
        currentSessionFile = File(rootFolder, "session.csv")
        currentSessionFile.writeText("Timestamp,Money,Tokens,Crates,Keys,Blocks,Swings,BlocksMined,Fortune,Momentum,Artifacts\n")
    }

    private fun prepareTrendFile() {
        val dateString = getUtcDateString()

        currentDayFile = File(trendsFolder, "$dateString.csv")
        if (!currentDayFile.exists()) {
            currentDayFile.writeText("Timestamp,Money,Tokens,Crates,Keys,Blocks,Swings,BlocksMined,Fortune,Momentum,Artifacts\n")
        }
    }

    fun prepareCFFile() {
        val cfFolder = File(frenzyRoot, "champion")
        if (!cfFolder.exists()) cfFolder.mkdirs()

        val date = getUtcDateString()
        var cfNum = 1
        while (File(cfFolder, "$date-$cfNum.csv").exists()) cfNum++

        currentCFFile = File(cfFolder, "$date-$cfNum.csv")
        currentCFFile.writeText("Timestamp,Money,Tokens,Crates,Keys,Blocks,Swings,SessionBM,Fortune,Momentum,Artifacts\n")
    }

    fun saveFrenzy(
        type: String,
        header: String,
        data: String,
    ) {
        val folder = File(frenzyRoot, type)
        if (!folder.exists()) folder.mkdirs()

        val date = getUtcDateString()
        val timestamp = getUtcDateTime().format(TIME_FORMATTER)
        var num = 1
        while (File(folder, "${type.uppercase()}-$date-summary-$num.csv").exists()) num++

        val file = File(folder, "${type.uppercase()}-$date-summary-$num.csv")
        try {
            file.writeText("Timestamp,$header\n$timestamp,$data\n")
            UtilFunctions.showToast(
                "Frenzy Saved",
                "Saved ${type.replaceFirstChar { it.uppercase() }} to .minecraft/dankhelper/$type/",
            )
        } catch (e: Exception) {
            logger.error("Failed to log", e)
        }
    }

    fun logStats(
        money: String,
        tokens: Long,
        crates: Long,
        keys: Long,
        blocks: Long,
        swings: Long,
        sessionBM: Long,
        fortune: Long,
        momentum: Long,
        artifacts: Long,
        isCF: Boolean,
    ) {
        val utcTimestamp = getUtcDateTime().format(TIME_FORMATTER)
        val csvRow = "$utcTimestamp,$money,$tokens,$crates,$keys,$blocks,$swings,$sessionBM,$fortune,$momentum,$artifacts"

        try {
            currentSessionFile.appendText("$csvRow\n")
            currentDayFile.appendText("$csvRow\n")
            StatsManager.loggedArtifact = 0L
            StatsManager.loggedMomentum = 0L
            if (isCF) {
                currentCFFile.appendText("$csvRow\n")
            }
        } catch (e: Exception) {
            logger.error("Failed to append stats to CSV", e)
        }
    }
}
