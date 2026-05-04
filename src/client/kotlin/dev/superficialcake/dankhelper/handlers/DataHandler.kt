package dev.superficialcake.dankhelper.handlers

import dev.superficialcake.dankhelper.util.UtilFunctions
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import java.io.File
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DataHandler {
    private val gameDir: File = Minecraft.getInstance().gameDirectory

    private val rootFolder: File = File(gameDir, "dankhelper")
    private val sessionsFolder: File = File(rootFolder, "sessions")
    private val frenzyRoot: File = File(rootFolder, "frenzies")

    private lateinit var currentSessionFile: File
    private lateinit var currentCFFile: File

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss")

    private fun utcDateStr(): String = ZonedDateTime.now(ZoneOffset.UTC).format(dateFmt)

    fun init() {
        if (!rootFolder.exists()) rootFolder.mkdirs()
        if (!sessionsFolder.exists()) sessionsFolder.mkdirs()

        prepareSessionFile()
        registerMidnightRollover()
    }

    private fun registerMidnightRollover() {
        var lastUtcDate = utcDateStr()
        var tickCount = 0

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            tickCount++
            if (tickCount < 20) return@register
            tickCount = 0

            val currentUtcDate = utcDateStr()
            if (currentUtcDate != lastUtcDate) {
                lastUtcDate = currentUtcDate
                prepareSessionFile()
            }
        }
    }

    private fun prepareSessionFile() {
        val date = utcDateStr()
        var sessionNum = 1
        while (File(sessionsFolder, "$date-$sessionNum.csv").exists()) sessionNum++

        currentSessionFile = File(sessionsFolder, "$date-$sessionNum.csv")
        currentSessionFile.writeText("Timestamp,Money,Tokens,Crates,Keys,Blocks,Swings,SessionBM,Fortune,Momentum\n")
    }

    fun prepareCFFile() {
        val cfFolder = File(frenzyRoot, "champion")
        if (!cfFolder.exists()) cfFolder.mkdirs()

        val date = utcDateStr()
        var cfNum = 1
        while (File(cfFolder, "$date-$cfNum.csv").exists()) cfNum++

        currentCFFile = File(cfFolder, "$date-$cfNum.csv")
        currentCFFile.writeText("Timestamp,Money,Tokens,Crates,Keys,Blocks,Swings,SessionBM,Fortune,Momentum\n")
    }

    fun saveFrenzy(
        type: String,
        header: String,
        data: String,
    ) {
        val folder = File(frenzyRoot, type)
        if (!folder.exists()) folder.mkdirs()

        val date = utcDateStr()
        val timestamp = LocalTime.now().format(timeFmt)
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
            e.printStackTrace()
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
        momentum: Long = 0L,
        isCF: Boolean = false,
    ) {
        val timestamp = LocalTime.now().format(timeFmt)
        val row = "$timestamp,$money,$tokens,$crates,$keys,$blocks,$swings,$sessionBM,$fortune,$momentum"
        val targetFile = if (isCF) currentCFFile else currentSessionFile
        try {
            targetFile.appendText("$row\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
