package dev.superficialcake.dankhelper.handlers

import net.minecraft.client.MinecraftClient
import java.io.File

object DataHandler {

    private val gameDir: File = MinecraftClient.getInstance().runDirectory

    private val dataFolder: File = File(gameDir, "dankhelper")

    fun init(){
        if(!dataFolder.exists()){
            dataFolder.mkdirs()
        }
    }

    fun getFile(fileName: String): File{
        return File(dataFolder, fileName)
    }
}