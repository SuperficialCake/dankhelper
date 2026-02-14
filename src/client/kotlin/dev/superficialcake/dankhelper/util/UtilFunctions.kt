package dev.superficialcake.dankhelper.util

import dev.superficialcake.dankhelper.DankHelperClient
import dev.superficialcake.dankhelper.handlers.DataHandler
import dev.superficialcake.dankhelper.handlers.ScoreboardHandler
import dev.superficialcake.dankhelper.handlers.StatsManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.toast.SystemToast
import net.minecraft.text.Text
import java.math.BigDecimal
import java.util.Locale

object UtilFunctions {

    fun resetAll(){
        DankHelperClient.startTime = System.currentTimeMillis()

        StatsManager.resetStats()
        ScoreboardHandler.reset()
        DataHandler.init()

        if(!DankHelperClient.initialSession) showToast("Session Reset", "The session has been reset! New CSV has been generated, and UI reset")

    }

    fun showToast(title: String, description: String){
        MinecraftClient.getInstance().toastManager.add(
            SystemToast(
                SystemToast.Type.PERIODIC_NOTIFICATION,
                Text.literal(title),
                Text.literal(description)
            )
        )
    }

    fun parseSuffixedNum(input: String): BigDecimal {
        val cleanInput = input.replace("$", "").replace(",", "").trim()
        val numberPart = cleanInput.takeWhile { it.isDigit() || it == '.' }
        val suffixPart = cleanInput.drop(numberPart.length).lowercase()

        if (numberPart.isEmpty()) return BigDecimal.ZERO
        val baseValue = BigDecimal(numberPart)

        val multiplier = when (suffixPart) {
            "k" -> BigDecimal("1000")
            "m" -> BigDecimal("1000000")
            "b" -> BigDecimal("1000000000")
            "t" -> BigDecimal("1000000000000")
            "qd" -> BigDecimal("1000000000000000")
            "qt" -> BigDecimal("1000000000000000000")
            "sx" -> BigDecimal("1000000000000000000000")
            "sp" -> BigDecimal("1000000000000000000000000")
            else -> BigDecimal.ONE
        }
        return baseValue.multiply(multiplier)
    }

    fun formatNumber(value: Double): String{
        if (value < 1000) return String.Companion.format(Locale.US, "%.0f", value)

        val suffixes = listOf("", "K", "M", "B", "T", "Qd", "Qt", "Sx", "Sp")
        var current = value
        var index = 0

        while(current >= 1000){
            current /= 1000
            index++
        }

        return String.Companion.format(Locale.US, "%.1f%s", current, suffixes[index])
    }

    fun getFormattedTime(startTime: Long): String{
        if (startTime == 0L) return "00:00:00"

        val s = (System.currentTimeMillis() - startTime) / 1000
        val hours = s / 3600
        val minutes = ( s % 3600 ) / 60
        val seconds = s % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}