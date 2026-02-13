package dev.superficialcake.dankhelper.ui

import dev.superficialcake.dankhelper.DankHelperClient
import dev.superficialcake.dankhelper.Util
import dev.superficialcake.dankhelper.handlers.KeybindHandler
import dev.superficialcake.dankhelper.handlers.ScoreboardHandler
import dev.superficialcake.dankhelper.handlers.StatsManager
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext

object DankHud : HudRenderCallback {

    override fun onHudRender(drawContext: DrawContext, tickDelta: Float) {
        val client = MinecraftClient.getInstance()
        if (client.options.hudHidden || !DankHelperClient.isConnected || !KeybindHandler.showUI || client.options.debugEnabled) return

        val textRenderer = client.textRenderer
        var x = 10
        var y = 60
        val padding = 4

        val lines = listOf(
            "§2§lSession Time:",
            "§r${Util.getFormattedTime(DankHelperClient.startTime)}",
            "§7=================",
            "§a§lMPM: §r${StatsManager.avgMpm}",
            "§b§lTPM: §r${StatsManager.avgTpm}",
            "§e§lCPM: §r${StatsManager.avgCpm}",
            "§6§lKPM: §r${StatsManager.avgKpm}",
            "§7=================",
            "§d§lSPM: §r${StatsManager.avgSpm}",
            "§c§lBPM: §r${StatsManager.avgBpm}",
            "§5§lBM: §r${ScoreboardHandler.formattedSessionBM}",
            "§b§lFortune Gained: §r${StatsManager.sumFortune}"
        )

        val maxTextWidth = lines.maxOf{textRenderer.getWidth(it)}
        val graphHeight = 25
        val graphSpacing = 5
        val graphWidth = maxTextWidth

        val labelWidth = 35
        val totalWidth = maxTextWidth + labelWidth + (padding * 2)
        val totalTextHeight = (lines.size * 10)
        val totalHeight = totalTextHeight + (graphHeight * 2) + (graphSpacing * 2) + padding


        drawContext.fill(x - padding, y - padding, x + totalWidth, y + totalHeight -2, 0x90000000.toInt() )

        var currentY = y
        for (line in lines){
            drawContext.drawTextWithShadow(textRenderer, line, x, currentY, 0xFFFFFF)
            currentY += 10
        }

        currentY += 5

        val graphX = x
        val graphYBase = currentY

        drawGraph(drawContext, textRenderer, graphX, graphYBase, graphWidth, graphHeight, StatsManager.moneyHistory, 0xFF55FF55.toInt(), "MPM")
        drawGraph(drawContext, textRenderer, graphX, graphYBase + graphHeight + graphSpacing, graphWidth, graphHeight, StatsManager.tokenHistory, 0xFF55FFFF.toInt(), "TPM")

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

        val maxStr = Util.formatNumber(max)
        val minStr = Util.formatNumber(min)

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