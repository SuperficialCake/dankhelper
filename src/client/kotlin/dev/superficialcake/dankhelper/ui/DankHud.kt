package dev.superficialcake.dankhelper.ui

import dev.superficialcake.dankhelper.DankHelperClient
import dev.superficialcake.dankhelper.util.UtilFunctions
import dev.superficialcake.dankhelper.config.DankConfig
import dev.superficialcake.dankhelper.handlers.KeybindHandler
import dev.superficialcake.dankhelper.handlers.ScoreboardHandler
import dev.superficialcake.dankhelper.handlers.StatsManager
import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

object DankHud {

    var currentHeight = 0
    var currentWidth = 0
    val translatedSessionTime = Text.translatable("text.hud.dankhelper.session_time").string

    fun onHudRender(drawContext: DrawContext) {

        val client = MinecraftClient.getInstance()
        if (client.options.hudHidden || !DankHelperClient.isConnected || !KeybindHandler.showUI) return

        val config = AutoConfig.getConfigHolder(DankConfig::class.java).config

        val textRenderer = client.textRenderer
        val x = config.hudX
        val y = config.hudY
        val padding = 6


        val lines = mutableListOf<String>()

        if (config.showSessionTime) {
            lines.add("§2§l${translatedSessionTime}")
            lines.add("§r${UtilFunctions.getFormattedTime(DankHelperClient.startTime)}")
            lines.add("§7=================")
        }

        if (config.showMPM) lines.add("§a§lMPM: §r${StatsManager.avgMpm}")
        if (config.showTPM) lines.add("§b§lTPM: §r${StatsManager.avgTpm}")
        if (config.showCPM) lines.add("§e§lCPM: §r${StatsManager.avgCpm}")
        if (config.showKPM) lines.add("§6§lKPM: §r${StatsManager.avgKpm}")

        if ((config.showMPM || config.showTPM || config.showCPM || config.showKPM) &&
            (config.showSPM || config.showBPM || config.showBM || config.showFortune)
        ) {
            lines.add("§7=================")
        }

        if (config.showSPM) lines.add("§d§lSPM: §r${StatsManager.avgSpm}")
        if (config.showBPM) lines.add("§c§lBPM: §r${StatsManager.avgBpm}")
        if (config.showBM) lines.add("§5§lBM: §r${ScoreboardHandler.formattedSessionBM}")
        if (config.showFortune) {
            val formattedFortune = "%,d".format(StatsManager.sumFortune)
            lines.add("""§b§lFortune: §r${formattedFortune}""")
        }

        val maxTextWidth = if (lines.isNotEmpty()) lines.maxOf { textRenderer.getWidth(it) } else 0
        val graphWidth = 85
        val labelWidth = 25


        currentWidth = maxOf(maxTextWidth, graphWidth + labelWidth) + (padding * 2)

        val textHeight = lines.size * 10
        var graphsSectionHeight = 0
        if (config.showMoneyGraph) graphsSectionHeight += 50
        if (config.showTokenGraph) graphsSectionHeight += 50

        currentHeight = textHeight + graphsSectionHeight

        drawContext.fill(x - padding, y - padding, x + currentWidth, y + currentHeight, 0x90000000.toInt())

        lines.forEachIndexed { i, line ->
            drawContext.drawText(textRenderer, Text.literal(line), x, y + (i * 10), 0xFFFFFFFF.toInt(), true)
        }

        var graphY = y + textHeight + 5
        if (config.showMoneyGraph) {
            drawGraph(drawContext, textRenderer, x, graphY, graphWidth, 40, StatsManager.moneyHistory, 0xFF55FF55.toInt(), "MPM")
            graphY += 45
        }
        if (config.showTokenGraph) {
            drawGraph(drawContext, textRenderer, x, graphY, graphWidth, 40, StatsManager.tokenHistory, 0xFF55FFFF.toInt(), "TPM")
        }
    }

    private fun drawGraph(context: DrawContext, textRenderer: TextRenderer, x: Int, y: Int, width: Int, height: Int, data: List<Double>, color: Int, label: String) {
        context.fill(x, y, x + width, y + height, 0x50000000)

        val historySnapshot = data.toList()

        if (historySnapshot.size < 2) {
            context.drawTextWithShadow(textRenderer, label, x + width + 4, y + (height / 2) - 4, color)
            return
        }

        val max = historySnapshot.maxOrNull() ?: 1.0
        val min = historySnapshot.minOrNull() ?: 0.0
        val range = (max - min).coerceAtLeast(1.0)
        val stepX = width.toDouble() / (StatsManager.MAX_HISTORY - 1)

        for (i in 0 until historySnapshot.size - 1) {
            val x1 = x + (i * stepX).toInt()
            val x2 = x + ((i + 1) * stepX).toInt()

            val y1 = (y + height) - (((historySnapshot[i] - min) / range) * height).toInt()
            val y2 = (y + height) - (((historySnapshot[i + 1] - min) / range) * height).toInt()

            drawConnection(context, x1, y1, x2, y2, color)
        }

        val maxStr = UtilFunctions.formatNumber(max)
        val minStr = UtilFunctions.formatNumber(min)

        context.drawTextWithShadow(textRenderer, maxStr, x + width + 4, y, color)
        context.drawTextWithShadow(textRenderer, minStr, x + width + 4, y + height - 8, 0xFFAAAAAA.toInt())
    }

    private fun drawConnection(context: DrawContext, x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        if (y1 == y2){
            context.fill(x1, y1, x2, y1 + 1, color)
            return
        }

        val midX = (x1 + x2) / 2

        context.fill(x1, y1, midX, y1 + 1, color)

        val minY = minOf(y1, y2)
        val maxY = maxOf(y1, y2)
        context.fill(midX, minY, midX + 1, maxY + 1, color)

        context.fill(midX, y2, x2, y2 + 1, color)
    }

}