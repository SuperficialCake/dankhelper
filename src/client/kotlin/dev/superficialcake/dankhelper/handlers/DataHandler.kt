package dev.superficialcake.dankhelper.handlers

import dev.superficialcake.dankhelper.util.UtilFunctions
import net.minecraft.client.MinecraftClient
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object DataHandler {

    private val gameDir: File = MinecraftClient.getInstance().runDirectory

    private val rootFolder: File = File(gameDir, "dankhelper")
    private val sessionsFolder: File = File(rootFolder, "sessions")
    private val frenzyRoot: File = File(rootFolder, "frenzies")

    private lateinit var currentSessionFile: File

    fun init(){
        if(!rootFolder.exists())rootFolder.mkdirs()
        if(!sessionsFolder.exists())sessionsFolder.mkdirs()

        prepareSessionFile()
    }

    private fun prepareSessionFile(){
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        var sessionNum = 1

        while(File(sessionsFolder, "$date-$sessionNum.csv").exists()){
            sessionNum++
        }

        currentSessionFile = File(sessionsFolder, "$date-$sessionNum.csv")

        val header = "Timestamp,Money,Tokens,Crates,Keys,Blocks,Swings,SessionBM,Fortune\n"
        currentSessionFile.writeText(header)
    }

    fun saveFrenzy(type: String, header: String, data: String) {
        val folder = File(frenzyRoot, type)
        if (!folder.exists()) folder.mkdirs()

        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        var num = 1
        val timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        while (File(folder, "${type.uppercase()}-$date-$num.csv").exists()) { num++ }

        val file = File(folder, "${type.uppercase()}-$date-$num.csv")
        val content = "Timestamp,${header}\n${timestamp},$data\n"

        try {
            file.writeText(content)
            UtilFunctions.showToast("Frenzy Saved", "Saved ${type.replaceFirstChar { it.uppercase() }} to .minecraft/dankhelper/$type/")
        } catch (e: Exception){
            e.printStackTrace()
        }
    }


    fun logStats(money: String, tokens: Long, crates: Long, keys: Long, blocks: Long, swings: Long, sessionBM: Long, fortune: Long){
        val timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val row = "$timestamp,$money,$tokens,$crates,$keys,$blocks,$swings,$sessionBM,$fortune"

        try {
            currentSessionFile.appendText("$row\n")
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
}