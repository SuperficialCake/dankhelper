package dev.superficialcake.dankhelper.ui

import dev.superficialcake.dankhelper.util.UtilFunctions
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.awt.Desktop
import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class TrendsScreen : Screen(Text.literal("Trends")) {
    private enum class Tab(
        val label: String,
    ) {
        SESSIONS("Mining Sessions"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly"),
        CHAMPION("Champion Frenzies"),
        FISHING("Fishing Frenzies"),
    }

    private var currentTab = Tab.SESSIONS
    private var days: List<DayStats> = emptyList()
    private var weeks: List<WeekStats> = emptyList()
    private var months: List<MonthStats> = emptyList()
    private var cfEntries: List<CfEntry> = emptyList()
    private var ffEntries: List<FfEntry> = emptyList()
    private var selectedIndex = 0
    private var scrollOffset = 0

    private val pad = 8
    private val titleHeight = 20
    private val tabHeight = 18
    private val buttonWidth = 72
    private val buttonHeight = 28
    private val buttonGap = 4
    private val pickerHeight = buttonHeight + pad * 2

    private val backgroundColor = 0xE0101318.toInt()
    private val panelColor = 0xCC1A1F28.toInt()
    private val borderColor = 0xFF2E3A50.toInt()
    private val selectedColor = 0xFF1E6B3E.toInt()
    private val hoverColor = 0xFF253040.toInt()
    private val textColor = 0xFFDDEEFF.toInt()
    private val dimmedTextColor = 0xFF6A7A90.toInt()
    private val accentColor = 0xFF55FF77.toInt()
    private val moneyColor = 0xFF55FF55.toInt()
    private val tokenColor = 0xFF55FFFF.toInt()
    private val crateColor = 0xFFFFFF55.toInt()
    private val swingColor = 0xFFFF55FF.toInt()
    private val emptyColor = 0xFF555577.toInt()
    private val keyColor = 0xFFFF9944.toInt()
    private val bpmColor = 0xFFFF5555.toInt()
    private val fishColor = 0xFF44AAFF.toInt()
    private val selectedTabColor = 0xFF1A2A3A.toInt()
    private val tabColor = 0xFF111820.toInt()
    private val momentumColor = 0xFF5555FF.toInt()
    private val artifactColor = 0xFFFFFF00.toInt()

    private var mouseX = 0
    private var mouseY = 0

    //  Lifecycle

    override fun init() {
        super.init()
        days = TrendsLoader.loadTrends()
        weeks = TrendsLoader.loadWeeklyTrends(days)
        months = TrendsLoader.loadMonthlyTrends(days)
        cfEntries = TrendsLoader.loadChampionFrenzies()
        ffEntries = TrendsLoader.loadFishingFrenzies()
        resetPicker()
    }

    private fun resetPicker() {
        selectedIndex = 0
        scrollOffset = 0
    }

    override fun shouldPause(): Boolean = false

    override fun renderBackground(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
    ) {
        // Suppress Minecraft's blur so the game world stays visible
    }

    override fun render(
        context: DrawContext,
        mx: Int,
        my: Int,
        delta: Float,
    ) {
        mouseX = mx
        mouseY = my
        context.fill(0, 0, width, height, backgroundColor)
        renderTitleBar(context, width)
        renderTabs(context, width)
        renderPicker(context, width)

        val contentY = titleHeight + tabHeight + pickerHeight + 2
        val contentHeight = height - contentY - pad

        when (currentTab) {
            Tab.SESSIONS -> {
                if (days.isEmpty()) {
                    renderEmpty(context, width, contentY, contentHeight)
                } else {
                    renderSessionDetail(context, days[selectedIndex], width, contentY, contentHeight)
                }
            }

            Tab.WEEKLY -> {
                if (weeks.isEmpty()) {
                    renderEmpty(context, width, contentY, contentHeight)
                } else {
                    renderWeekDetail(context, weeks[selectedIndex], width, contentY, contentHeight)
                }
            }

            Tab.MONTHLY -> {
                if (months.isEmpty()) {
                    renderEmpty(context, width, contentY, contentHeight)
                } else {
                    renderMonthDetail(context, months[selectedIndex], width, contentY, contentHeight)
                }
            }

            Tab.CHAMPION -> {
                if (cfEntries.isEmpty()) {
                    renderEmpty(context, width, contentY, contentHeight)
                } else {
                    renderCfDetail(context, cfEntries[selectedIndex], width, contentY, contentHeight)
                }
            }

            Tab.FISHING -> {
                if (ffEntries.isEmpty()) {
                    renderEmpty(context, width, contentY, contentHeight)
                } else {
                    renderFfDetail(context, ffEntries[selectedIndex], width, contentY, contentHeight)
                }
            }
        }

        super.render(context, mx, my, delta)
    }

    //  Title bar

    private fun closeButtonBounds(screenWidth: Int): Pair<Int, Int> {
        val label = "§c✕  Close"
        val bw = textRenderer.getWidth(label) + pad * 2
        return (screenWidth - bw - pad) to bw
    }

    private fun folderButtonBounds(screenWidth: Int): Pair<Int, Int> {
        val label = "§7⧃  Open Folder"
        val bw = textRenderer.getWidth(label) + pad * 2
        val bx = screenWidth - closeButtonBounds(screenWidth).second - pad - bw - 4
        return bx to bw
    }

    private fun currentFolder(): File {
        val gameDir = MinecraftClient.getInstance().runDirectory
        val subfolder =
            when (currentTab) {
                Tab.SESSIONS -> "logs"
                Tab.WEEKLY -> "logs"
                Tab.MONTHLY -> "logs"
                Tab.CHAMPION -> "frenzies/champion"
                Tab.FISHING -> "frenzies/fishing"
            }
        return File(gameDir, "dankhelper/$subfolder").also { it.mkdirs() }
    }

    private fun openFolder(folder: File) {
        runCatching {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(folder)
        }
    }

    private fun renderTitleBar(
        context: DrawContext,
        screenWidth: Int,
    ) {
        context.fill(0, 0, screenWidth, titleHeight, panelColor)
        context.fill(0, titleHeight, screenWidth, titleHeight + 1, borderColor)
        context.drawTextWithShadow(
            textRenderer,
            "§b§lDankHelper §7> §f§lTrends",
            pad,
            (titleHeight - 9) / 2,
            textColor,
        )

        val (closeBx, closeBw) = closeButtonBounds(screenWidth)
        val closeHovered = mouseX in closeBx..(closeBx + closeBw) && mouseY in 0..titleHeight
        context.fill(closeBx, 1, closeBx + closeBw, titleHeight - 1, if (closeHovered) 0xFF3A1A1A.toInt() else 0xFF2A1010.toInt())
        drawBorder(context, closeBx, 1, closeBw, titleHeight - 2, if (closeHovered) 0xFFFF5555.toInt() else 0xFF7A2222.toInt())
        context.drawTextWithShadow(textRenderer, "§c✕  Close", closeBx + pad, (titleHeight - 9) / 2, textColor)

        val (folderBx, folderBw) = folderButtonBounds(screenWidth)
        val folderHovered = mouseX in folderBx..(folderBx + folderBw) && mouseY in 0..titleHeight
        context.fill(folderBx, 1, folderBx + folderBw, titleHeight - 1, if (folderHovered) 0xFF1A2A1A.toInt() else 0xFF101A10.toInt())
        drawBorder(context, folderBx, 1, folderBw, titleHeight - 2, if (folderHovered) 0xFF55FF55.toInt() else 0xFF225522.toInt())
        context.drawTextWithShadow(textRenderer, "§7⧃  Open Folder", folderBx + pad, (titleHeight - 9) / 2, textColor)
    }

    //  Tab bar

    private fun renderTabs(
        context: DrawContext,
        screenWidth: Int,
    ) {
        val y = titleHeight
        context.fill(0, y, screenWidth, y + tabHeight, tabColor)
        context.fill(0, y + tabHeight, screenWidth, y + tabHeight + 1, borderColor)

        val tabWidth = screenWidth / Tab.entries.size
        for ((i, tab) in Tab.entries.withIndex()) {
            val tabX = i * tabWidth
            val isSelected = tab == currentTab
            val isHovered = mouseX in tabX..(tabX + tabWidth) && mouseY in y..(y + tabHeight)
            context.fill(
                tabX,
                y,
                tabX + tabWidth,
                y + tabHeight,
                if (isSelected) {
                    selectedTabColor
                } else if (isHovered) {
                    hoverColor
                } else {
                    tabColor
                },
            )
            if (isSelected) context.fill(tabX, y + tabHeight - 2, tabX + tabWidth, y + tabHeight, accentColor)
            context.fill(tabX + tabWidth - 1, y, tabX + tabWidth, y + tabHeight, borderColor)
            val label = if (isSelected) "§f§l${tab.label}" else "§7${tab.label}"
            context.drawCenteredTextWithShadow(textRenderer, label, tabX + tabWidth / 2, y + (tabHeight - 9) / 2, textColor)
        }
    }

    //  Picker

    private fun renderPicker(
        context: DrawContext,
        screenWidth: Int,
    ) {
        val y = titleHeight + tabHeight + 1
        context.fill(0, y, screenWidth, y + pickerHeight, panelColor)
        context.fill(0, y + pickerHeight, screenWidth, y + pickerHeight + 1, borderColor)

        val entries = currentEntries()
        if (entries.isEmpty()) {
            context.drawTextWithShadow(textRenderer, "§7No data", pad, y + (pickerHeight - 9) / 2, dimmedTextColor)
            return
        }

        val arrowSize = buttonHeight
        val arrowX = pad
        val arrowY = y + pad
        val rightArrowX = screenWidth - pad - arrowSize
        val buttonAreaX = arrowX + arrowSize + buttonGap
        val buttonAreaWidth = rightArrowX - buttonGap - buttonAreaX
        val visibleCount = (buttonAreaWidth + buttonGap) / (buttonWidth + buttonGap)

        val canScrollLeft = scrollOffset > 0
        val canScrollRight = scrollOffset + visibleCount < entries.size
        val leftArrowHovered = mouseX in arrowX..(arrowX + arrowSize) && mouseY in arrowY..(arrowY + arrowSize)
        val rightArrowHovered = mouseX in rightArrowX..(rightArrowX + arrowSize) && mouseY in arrowY..(arrowY + arrowSize)

        context.fill(
            arrowX,
            arrowY,
            arrowX + arrowSize,
            arrowY + arrowSize,
            if (canScrollLeft &&
                leftArrowHovered
            ) {
                hoverColor
            } else {
                panelColor
            },
        )
        context.drawCenteredTextWithShadow(
            textRenderer,
            if (canScrollLeft) "§f◄" else "§8◄",
            arrowX + arrowSize / 2,
            arrowY + (arrowSize - 9) / 2,
            textColor,
        )

        context.fill(
            rightArrowX,
            arrowY,
            rightArrowX + arrowSize,
            arrowY + arrowSize,
            if (canScrollRight &&
                rightArrowHovered
            ) {
                hoverColor
            } else {
                panelColor
            },
        )
        context.drawCenteredTextWithShadow(
            textRenderer,
            if (canScrollRight) "§f►" else "§8►",
            rightArrowX + arrowSize / 2,
            arrowY + (arrowSize - 9) / 2,
            textColor,
        )

        for (i in 0 until visibleCount) {
            val entryIndex = scrollOffset + i
            if (entryIndex >= entries.size) break
            val (topLabel, bottomLabel) = pickerLabels(entries[entryIndex])
            val bx = buttonAreaX + i * (buttonWidth + buttonGap)
            val by = arrowY
            val isSelected = entryIndex == selectedIndex
            val isHovered = mouseX in bx..(bx + buttonWidth) && mouseY in by..(by + buttonHeight)
            context.fill(
                bx,
                by,
                bx + buttonWidth,
                by + buttonHeight,
                if (isSelected) {
                    selectedColor
                } else if (isHovered) {
                    hoverColor
                } else {
                    0xFF151C25.toInt()
                },
            )
            val borderHighlight = if (isSelected) accentColor else borderColor
            context.fill(bx, by, bx + buttonWidth, by + 1, borderHighlight)
            context.fill(bx, by + buttonHeight - 1, bx + buttonWidth, by + buttonHeight, borderHighlight)
            context.fill(bx, by, bx + 1, by + buttonHeight, borderHighlight)
            context.fill(bx + buttonWidth - 1, by, bx + buttonWidth, by + buttonHeight, borderHighlight)
            val labelColor = if (isSelected) accentColor else textColor
            context.drawCenteredTextWithShadow(textRenderer, "§f$topLabel", bx + buttonWidth / 2, by + 4, labelColor)
            context.drawCenteredTextWithShadow(textRenderer, bottomLabel, bx + buttonWidth / 2, by + 14, dimmedTextColor)
        }
    }

    private fun pickerLabels(entry: Any): Pair<String, String> =
        when (entry) {
            is DayStats -> {
                val dateLabel = entry.date.format(DateTimeFormatter.ofPattern("MM/dd"))
                val utcToday = LocalDate.now(ZoneOffset.UTC)
                val badge =
                    when {
                        entry.date == utcToday -> "§aToday"
                        entry.date == utcToday.minusDays(1) -> "§7Yest."
                        else -> "§8${entry.minuteCount} min"
                    }
                dateLabel to badge
            }

            is WeekStats -> {
                val fmt = DateTimeFormatter.ofPattern("MM/dd")
                "${entry.weekStart.format(fmt)}-${entry.weekEnd.format(fmt)}" to "§7${entry.days.size} day(s)"
            }

            is MonthStats -> {
                val localDate = LocalDate.of(entry.year, entry.month, 1)
                localDate.format(DateTimeFormatter.ofPattern("MMM")) to "§7${entry.year}"
            }

            is CfEntry -> {
                entry.date.format(DateTimeFormatter.ofPattern("MM/dd")) to "§7${entry.timestamp}"
            }

            is FfEntry -> {
                entry.date.format(DateTimeFormatter.ofPattern("MM/dd")) to "§7${entry.timestamp}"
            }

            else -> {
                "?" to "?"
            }
        }

    private fun currentEntries(): List<Any> =
        when (currentTab) {
            Tab.SESSIONS -> days
            Tab.WEEKLY -> weeks
            Tab.MONTHLY -> months
            Tab.CHAMPION -> cfEntries
            Tab.FISHING -> ffEntries
        }

    private fun renderEmpty(
        context: DrawContext,
        screenWidth: Int,
        y: Int,
        h: Int,
    ) {
        context.drawCenteredTextWithShadow(
            textRenderer,
            "§7No data found. Play on DankPrison to generate files.",
            screenWidth / 2,
            y + h / 2,
            dimmedTextColor,
        )
    }

    //  Session detail

    private fun renderSessionDetail(
        context: DrawContext,
        day: DayStats,
        screenWidth: Int,
        y: Int,
        h: Int,
    ) {
        val x = pad
        val contentWidth = screenWidth - pad * 2
        val summaryHeight = 36

        context.fill(x, y + pad, x + contentWidth, y + pad + summaryHeight, panelColor)
        drawBorder(context, x, y + pad, contentWidth, summaryHeight, borderColor)
        context.drawTextWithShadow(
            textRenderer,
            "§f§l${day.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))}",
            x + 6,
            y + pad + 9,
            textColor,
        )
        context.drawTextWithShadow(
            textRenderer,
            "§7${day.minuteCount} samples",
            x + 6,
            y + pad + 20,
            dimmedTextColor,
        )
        val totals =
            "§aMoney: §f${UtilFunctions.formatNumber(day.totalMoney.toDouble())}" +
                "   §bTokens: §f${fmtLong(day.totalTokens)}" +
                "   §eBlocks: §f${fmtLong(day.totalBlocks)}"
        context.drawTextWithShadow(
            textRenderer,
            totals,
            x + contentWidth - textRenderer.getWidth(totals) - pad,
            y + pad + (summaryHeight - 9) / 2,
            textColor,
        )

        val cardsY = y + pad + summaryHeight + pad
        val cardHeight = 34
        val metrics =
            listOf(
                Triple("§a§lMPM", UtilFunctions.formatNumber(day.avgMpm.toDouble()), moneyColor),
                Triple("§b§lTPM", fmtLong(day.avgTpm), tokenColor),
                Triple("§e§lCPM", fmtLong(day.avgCpm), crateColor),
                Triple("§6§lKPM", fmtLong(day.avgKpm), keyColor),
                Triple("§d§lSPM", fmtLong(day.avgSpm), swingColor),
                Triple("§c§lBPM", fmtLong(day.avgBpm), bpmColor),
            )
        val cardWidth = (contentWidth - (metrics.size - 1) * buttonGap) / metrics.size
        for ((i, metric) in metrics.withIndex()) {
            renderStatCard(
                context,
                x + i * (cardWidth + buttonGap),
                cardsY,
                cardWidth,
                cardHeight,
                metric.first,
                metric.second,
                metric.third,
            )
        }

        val extraCardsY = cardsY + cardHeight + buttonGap
        val extraCardsHeight = 31
        val halfCardWidth = (contentWidth - buttonGap) / 2

        val momentumCardY = cardsY + cardHeight + buttonGap
        val momentumCardHeight = 31
        renderStatCard(
            context,
            x,
            extraCardsY,
            halfCardWidth,
            extraCardsHeight,
            "§9§lMomentum",
            fmtLong(day.totalMomentum),
            momentumColor,
        )

        renderStatCard(
            context,
            x + halfCardWidth + buttonGap,
            extraCardsY,
            contentWidth - halfCardWidth - buttonGap,
            extraCardsHeight,
            "§e§lArtifacts",
            fmtLong(day.totalArtifact),
            artifactColor,
        )

        val graphsY = extraCardsY + extraCardsHeight + pad
        val graphHeight = h - (graphsY - y) - pad
        val halfWidth = (contentWidth - pad) / 2

        if (day.moneyTimeline.size >=
            2
        ) {
            renderGraph(context, x, graphsY, halfWidth, graphHeight, day.moneyTimeline, moneyColor, "Money / Minute")
        } else {
            renderEmptyGraph(context, x, graphsY, halfWidth, graphHeight, "Money / Minute")
        }

        if (day.tokenTimeline.size >=
            2
        ) {
            renderGraph(context, x + halfWidth + pad, graphsY, halfWidth, graphHeight, day.tokenTimeline, tokenColor, "Tokens / Minute")
        } else {
            renderEmptyGraph(context, x + halfWidth + pad, graphsY, halfWidth, graphHeight, "Tokens / Minute")
        }
    }

    //  Weekly detail

    private fun renderWeekDetail(
        context: DrawContext,
        week: WeekStats,
        screenWidth: Int,
        y: Int,
        h: Int,
    ) {
        val x = pad
        val contentWidth = screenWidth - pad * 2
        val summaryHeight = 36

        context.fill(x, y + pad, x + contentWidth, y + pad + summaryHeight, panelColor)
        drawBorder(context, x, y + pad, contentWidth, summaryHeight, borderColor)
        val weekFmt = DateTimeFormatter.ofPattern("MMM d")
        val weekEndFmt = DateTimeFormatter.ofPattern("MMM d, yyyy")
        context.drawTextWithShadow(
            textRenderer,
            "§f§lWeek of ${week.weekStart.format(weekFmt)} – ${week.weekEnd.format(weekEndFmt)}",
            x + 6,
            y + pad + 9,
            textColor,
        )
        context.drawTextWithShadow(
            textRenderer,
            "§7${week.days.size} day(s)  ·  ${week.minuteCount} samples",
            x + 6,
            y + pad + 20,
            dimmedTextColor,
        )
        val totals =
            "§aMoney: §f${UtilFunctions.formatNumber(week.totalMoney.toDouble())}" +
                "   §bTokens: §f${fmtLong(week.totalTokens)}" +
                "   §eBlocks: §f${fmtLong(week.totalBlocks)}"
        context.drawTextWithShadow(
            textRenderer,
            totals,
            x + contentWidth - textRenderer.getWidth(totals) - pad,
            y + pad + (summaryHeight - 9) / 2,
            textColor,
        )

        val cardsY = y + pad + summaryHeight + pad
        val cardHeight = 34
        val metrics =
            listOf(
                Triple("§a§lMPM", UtilFunctions.formatNumber(week.avgMpm.toDouble()), moneyColor),
                Triple("§b§lTPM", fmtLong(week.avgTpm), tokenColor),
                Triple("§e§lCPM", fmtLong(week.avgCpm), crateColor),
                Triple("§6§lKPM", fmtLong(week.avgKpm), keyColor),
                Triple("§d§lSPM", fmtLong(week.avgSpm), swingColor),
                Triple("§c§lBPM", fmtLong(week.avgBpm), bpmColor),
            )
        val cardWidth = (contentWidth - (metrics.size - 1) * buttonGap) / metrics.size
        for ((i, metric) in metrics.withIndex()) {
            renderStatCard(context, x + i * (cardWidth + buttonGap), cardsY, cardWidth, cardHeight, metric.first, metric.second, metric.third)
        }

        val extraCardsY = cardsY + cardHeight + buttonGap
        val extraCardsHeight = 31
        val halfCardWidth = (contentWidth - buttonGap) / 2
        renderStatCard(context, x, extraCardsY, halfCardWidth, extraCardsHeight, "§9§lMomentum", fmtLong(week.totalMomentum), momentumColor)
        renderStatCard(context, x + halfCardWidth + buttonGap, extraCardsY, contentWidth - halfCardWidth - buttonGap, extraCardsHeight, "§e§lArtifacts", fmtLong(week.totalArtifact), artifactColor)

        val graphsY = extraCardsY + extraCardsHeight + pad
        val graphHeight = h - (graphsY - y) - pad
        val halfWidth = (contentWidth - pad) / 2

        if (week.moneyTimeline.size >= 2) {
            renderGraph(context, x, graphsY, halfWidth, graphHeight, week.moneyTimeline, moneyColor, "Money / Minute")
        } else {
            renderEmptyGraph(context, x, graphsY, halfWidth, graphHeight, "Money / Minute")
        }
        if (week.tokenTimeline.size >= 2) {
            renderGraph(context, x + halfWidth + pad, graphsY, halfWidth, graphHeight, week.tokenTimeline, tokenColor, "Tokens / Minute")
        } else {
            renderEmptyGraph(context, x + halfWidth + pad, graphsY, halfWidth, graphHeight, "Tokens / Minute")
        }
    }

    //  Monthly detail

    private fun renderMonthDetail(
        context: DrawContext,
        month: MonthStats,
        screenWidth: Int,
        y: Int,
        h: Int,
    ) {
        val x = pad
        val contentWidth = screenWidth - pad * 2
        val summaryHeight = 36
        val monthDate = LocalDate.of(month.year, month.month, 1)

        context.fill(x, y + pad, x + contentWidth, y + pad + summaryHeight, panelColor)
        drawBorder(context, x, y + pad, contentWidth, summaryHeight, borderColor)
        context.drawTextWithShadow(
            textRenderer,
            "§f§l${monthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}",
            x + 6,
            y + pad + 9,
            textColor,
        )
        context.drawTextWithShadow(
            textRenderer,
            "§7${month.days.size} day(s)  ·  ${month.minuteCount} samples",
            x + 6,
            y + pad + 20,
            dimmedTextColor,
        )
        val totals =
            "§aMoney: §f${UtilFunctions.formatNumber(month.totalMoney.toDouble())}" +
                "   §bTokens: §f${fmtLong(month.totalTokens)}" +
                "   §eBlocks: §f${fmtLong(month.totalBlocks)}"
        context.drawTextWithShadow(
            textRenderer,
            totals,
            x + contentWidth - textRenderer.getWidth(totals) - pad,
            y + pad + (summaryHeight - 9) / 2,
            textColor,
        )

        val cardsY = y + pad + summaryHeight + pad
        val cardHeight = 34
        val metrics =
            listOf(
                Triple("§a§lMPM", UtilFunctions.formatNumber(month.avgMpm.toDouble()), moneyColor),
                Triple("§b§lTPM", fmtLong(month.avgTpm), tokenColor),
                Triple("§e§lCPM", fmtLong(month.avgCpm), crateColor),
                Triple("§6§lKPM", fmtLong(month.avgKpm), keyColor),
                Triple("§d§lSPM", fmtLong(month.avgSpm), swingColor),
                Triple("§c§lBPM", fmtLong(month.avgBpm), bpmColor),
            )
        val cardWidth = (contentWidth - (metrics.size - 1) * buttonGap) / metrics.size
        for ((i, metric) in metrics.withIndex()) {
            renderStatCard(context, x + i * (cardWidth + buttonGap), cardsY, cardWidth, cardHeight, metric.first, metric.second, metric.third)
        }

        val extraCardsY = cardsY + cardHeight + buttonGap
        val extraCardsHeight = 31
        val halfCardWidth = (contentWidth - buttonGap) / 2
        renderStatCard(context, x, extraCardsY, halfCardWidth, extraCardsHeight, "§9§lMomentum", fmtLong(month.totalMomentum), momentumColor)
        renderStatCard(context, x + halfCardWidth + buttonGap, extraCardsY, contentWidth - halfCardWidth - buttonGap, extraCardsHeight, "§e§lArtifacts", fmtLong(month.totalArtifact), artifactColor)

        val graphsY = extraCardsY + extraCardsHeight + pad
        val graphHeight = h - (graphsY - y) - pad
        val halfWidth = (contentWidth - pad) / 2

        if (month.moneyTimeline.size >= 2) {
            renderGraph(context, x, graphsY, halfWidth, graphHeight, month.moneyTimeline, moneyColor, "Money / Minute")
        } else {
            renderEmptyGraph(context, x, graphsY, halfWidth, graphHeight, "Money / Minute")
        }
        if (month.tokenTimeline.size >= 2) {
            renderGraph(context, x + halfWidth + pad, graphsY, halfWidth, graphHeight, month.tokenTimeline, tokenColor, "Tokens / Minute")
        } else {
            renderEmptyGraph(context, x + halfWidth + pad, graphsY, halfWidth, graphHeight, "Tokens / Minute")
        }
    }

    //  Champion Frenzy detail

    private fun renderCfDetail(
        context: DrawContext,
        cf: CfEntry,
        screenWidth: Int,
        y: Int,
        h: Int,
    ) {
        val x = pad
        val contentWidth = screenWidth - pad * 2
        val summaryHeight = 36

        context.fill(x, y + pad, x + contentWidth, y + pad + summaryHeight, panelColor)
        drawBorder(context, x, y + pad, contentWidth, summaryHeight, borderColor)
        context.drawTextWithShadow(
            textRenderer,
            "§f§l${cf.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))} §7at §f${cf.timestamp}",
            x + 6,
            y + pad + 9,
            textColor,
        )
        context.drawTextWithShadow(textRenderer, "§7${cf.minuteCount} minute(s) logged", x + 6, y + pad + 20, dimmedTextColor)
        val totals =
            "§aMoney: §f${UtilFunctions.formatNumber(cf.totalMoney.toDouble())}" +
                "   §bTokens: §f${fmtLong(cf.totalTokens)}" +
                "   §eCrates: §f${fmtLong(cf.totalCrates)}"
        context.drawTextWithShadow(
            textRenderer,
            totals,
            x + contentWidth - textRenderer.getWidth(totals) - pad,
            y + pad + (summaryHeight - 9) / 2,
            textColor,
        )

        val cardsY = y + pad + summaryHeight + pad
        val cardHeight = 34
        val metrics =
            listOf(
                Triple("§a§lMPM", UtilFunctions.formatNumber(cf.avgMpm.toDouble()), moneyColor),
                Triple("§b§lTPM", fmtLong(cf.avgTpm), tokenColor),
                Triple("§e§lCPM", fmtLong(cf.avgCpm), crateColor),
                Triple("§6§lKPM", fmtLong(cf.avgKpm), keyColor),
                Triple("§d§lSPM", fmtLong(cf.avgSpm), swingColor),
                Triple("§c§lBPM", fmtLong(cf.avgBpm), bpmColor),
            )
        val cardWidth = (contentWidth - (metrics.size - 1) * buttonGap) / metrics.size
        for ((i, metric) in metrics.withIndex()) {
            renderStatCard(
                context,
                x + i * (cardWidth + buttonGap),
                cardsY,
                cardWidth,
                cardHeight,
                metric.first,
                metric.second,
                metric.third,
            )
        }

        val graphsY = cardsY + cardHeight + pad
        val graphHeight = h - (graphsY - y) - pad
        val halfWidth = (contentWidth - pad) / 2

        if (cf.moneyTimeline.size >=
            2
        ) {
            renderGraph(context, x, graphsY, halfWidth, graphHeight, cf.moneyTimeline, moneyColor, "Money / Minute")
        } else {
            renderEmptyGraph(context, x, graphsY, halfWidth, graphHeight, "Money / Minute")
        }

        if (cf.tokenTimeline.size >=
            2
        ) {
            renderGraph(context, x + halfWidth + pad, graphsY, halfWidth, graphHeight, cf.tokenTimeline, tokenColor, "Tokens / Minute")
        } else {
            renderEmptyGraph(context, x + halfWidth + pad, graphsY, halfWidth, graphHeight, "Tokens / Minute")
        }
    }

    //  Fishing Frenzy detail

    private fun renderFfDetail(
        context: DrawContext,
        ff: FfEntry,
        screenWidth: Int,
        y: Int,
        h: Int,
    ) {
        val x = pad
        val contentWidth = screenWidth - pad * 2
        val summaryHeight = 36

        context.fill(x, y + pad, x + contentWidth, y + pad + summaryHeight, panelColor)
        drawBorder(context, x, y + pad, contentWidth, summaryHeight, borderColor)
        context.drawTextWithShadow(
            textRenderer,
            "§f§l${ff.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))} §7at §f${ff.timestamp}",
            x + 6,
            y + pad + 9,
            textColor,
        )
        context.drawTextWithShadow(textRenderer, "§7Fishing Frenzy Summary", x + 6, y + pad + 20, dimmedTextColor)

        val cardsY = y + pad + summaryHeight + pad
        val cardHeight = 34
        val ffMetrics =
            listOf(
                Triple("§6§lKeys", fmtLong(ff.keys), keyColor),
                Triple("§3§lFish", fmtLong(ff.fish), fishColor),
                Triple("§7§lCasts", fmtLong(ff.casts), dimmedTextColor),
            )
        val cardWidth = (contentWidth - (ffMetrics.size - 1) * buttonGap) / ffMetrics.size
        for ((i, metric) in ffMetrics.withIndex()) {
            renderStatCard(
                context,
                x + i * (cardWidth + buttonGap),
                cardsY,
                cardWidth,
                cardHeight,
                metric.first,
                metric.second,
                metric.third,
            )
        }

        val derivedY = cardsY + cardHeight + pad
        val derivedHeight = 48
        context.fill(x, derivedY, x + contentWidth, derivedY + derivedHeight, panelColor)
        drawBorder(context, x, derivedY, contentWidth, derivedHeight, borderColor)
        context.drawTextWithShadow(textRenderer, "§7Derived", x + pad, derivedY + 4, dimmedTextColor)

        val fishPerCast = if (ff.casts > 0) ff.fish.toDouble() / ff.casts else 0.0
        val keysPerCast = if (ff.casts > 0) ff.keys.toDouble() / ff.casts else 0.0
        val derived =
            listOf(
                "§3Fish/Cast: §f${"%.2f".format(fishPerCast)}",
                "§6Keys/Cast: §f${"%.3f".format(keysPerCast)}",
            )
        val derivedColWidth = contentWidth / derived.size
        for ((i, stat) in derived.withIndex()) {
            context.drawTextWithShadow(textRenderer, stat, x + pad + i * derivedColWidth, derivedY + 18, textColor)
        }

        val historyY = derivedY + derivedHeight + pad
        val historyHeight = h - (historyY - y) - pad
        if (historyHeight > 20) renderFfHistory(context, x, historyY, contentWidth, historyHeight)
    }

    private fun renderFfHistory(
        context: DrawContext,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
    ) {
        val fishValues = ffEntries.reversed().map { it.fish.toDouble() }
        val keyValues = ffEntries.reversed().map { it.keys.toDouble() }
        val halfWidth = (w - pad) / 2

        if (fishValues.size >= 2) {
            renderGraph(context, x, y, halfWidth, h, fishValues, fishColor, "Fish per run  (Oldest → Newest)")
        } else {
            renderEmptyGraph(context, x, y, halfWidth, h, "Fish per run  (Oldest → Newest)")
        }

        if (keyValues.size >=
            2
        ) {
            renderGraph(context, x + halfWidth + pad, y, halfWidth, h, keyValues, keyColor, "Keys per run  (Oldest → Newest)")
        } else {
            renderEmptyGraph(context, x + halfWidth + pad, y, halfWidth, h, "Keys per run  (Oldest → Newest)")
        }
    }

    //  Shared rendering primitives

    private fun renderStatCard(
        context: DrawContext,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        label: String,
        value: String,
        color: Int,
    ) {
        context.fill(x, y, x + w, y + h, panelColor)
        drawBorder(context, x, y, w, h, borderColor)
        context.fill(x, y, x + 2, y + h, color)
        context.drawTextWithShadow(textRenderer, label, x + 6, y + 5, color)
        context.drawTextWithShadow(textRenderer, "§f$value", x + 6, y + 20, textColor)
    }

    private fun renderGraph(
        context: DrawContext,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        data: List<Double>,
        color: Int,
        title: String,
    ) {
        context.fill(x, y, x + w, y + h, panelColor)
        drawBorder(context, x, y, w, h, borderColor)
        context.drawTextWithShadow(textRenderer, "§7$title", x + pad, y + 3, dimmedTextColor)

        val titleHeight = 12
        val graphX = x + pad
        val graphY = y + titleHeight + 2
        val graphWidth = w - pad * 2 - 36
        val graphHeight = h - titleHeight - pad - 2

        for (gridLine in 1..2) {
            context.fill(
                graphX,
                graphY + (graphHeight * gridLine / 3),
                graphX + graphWidth,
                graphY + (graphHeight * gridLine / 3) + 1,
                0x22FFFFFF,
            )
        }

        val maxValue = data.maxOrNull() ?: 1.0
        val minValue = data.minOrNull() ?: 0.0
        val valueRange = (maxValue - minValue).coerceAtLeast(1.0)
        val xStep = graphWidth.toDouble() / (data.size - 1).coerceAtLeast(1)

        for (i in 0 until data.size - 1) {
            val x1 = graphX + (i * xStep).toInt()
            val x2 = graphX + ((i + 1) * xStep).toInt()
            val y1 = (graphY + graphHeight) - (((data[i] - minValue) / valueRange) * graphHeight).toInt()
            val y2 = (graphY + graphHeight) - (((data[i + 1] - minValue) / valueRange) * graphHeight).toInt()
            drawLineSegment(context, x1, y1, x2, y2, color)
        }

        context.drawTextWithShadow(textRenderer, "§f${UtilFunctions.formatNumber(maxValue)}", graphX + graphWidth + 2, graphY, color)
        context.drawTextWithShadow(
            textRenderer,
            "§8${UtilFunctions.formatNumber(minValue)}",
            graphX + graphWidth + 2,
            graphY + graphHeight - 9,
            dimmedTextColor,
        )
    }

    private fun renderEmptyGraph(
        context: DrawContext,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        title: String,
    ) {
        context.fill(x, y, x + w, y + h, panelColor)
        drawBorder(context, x, y, w, h, borderColor)
        context.drawTextWithShadow(textRenderer, "§7$title", x + pad, y + 3, dimmedTextColor)
        context.drawCenteredTextWithShadow(textRenderer, "§8Not enough data", x + w / 2, y + h / 2 - 4, emptyColor)
    }

    //  Input

    override fun mouseClicked(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean {
        val (closeBx, closeBw) = closeButtonBounds(width)
        if (mouseX >= closeBx && mouseX <= closeBx + closeBw && mouseY in 0.0..titleHeight.toDouble()) {
            close()
            return true
        }

        val (folderBx, folderBw) = folderButtonBounds(width)
        if (mouseX >= folderBx && mouseX <= folderBx + folderBw && mouseY in 0.0..titleHeight.toDouble()) {
            openFolder(currentFolder())
            return true
        }

        if (mouseY >= titleHeight && mouseY <= titleHeight + tabHeight) {
            val tabWidth = width / Tab.entries.size
            val clickedTab = Tab.entries[(mouseX / tabWidth).toInt().coerceIn(0, Tab.entries.size - 1)]
            if (clickedTab != currentTab) {
                currentTab = clickedTab
                resetPicker()
            }
            return true
        }

        val entries = currentEntries()
        if (entries.isEmpty()) return super.mouseClicked(mouseX, mouseY, button)

        val arrowY = titleHeight + tabHeight + 1 + pad
        val rightArrowX = width - pad - buttonHeight
        val buttonAreaX = pad + buttonHeight + buttonGap
        val buttonAreaWidth = rightArrowX - buttonGap - buttonAreaX
        val visibleCount = (buttonAreaWidth + buttonGap) / (buttonWidth + buttonGap)

        if (mouseX >= pad && mouseX <= pad + buttonHeight && mouseY >= arrowY && mouseY <= arrowY + buttonHeight && scrollOffset > 0) {
            scrollOffset = (scrollOffset - visibleCount).coerceAtLeast(0)
            return true
        }
        if (mouseX >= rightArrowX && mouseX <= rightArrowX + buttonHeight && mouseY >= arrowY && mouseY <= arrowY + buttonHeight &&
            scrollOffset + visibleCount < entries.size
        ) {
            scrollOffset = (scrollOffset + visibleCount).coerceAtMost((entries.size - visibleCount).coerceAtLeast(0))
            return true
        }

        for (i in 0 until visibleCount) {
            val entryIndex = scrollOffset + i
            if (entryIndex >= entries.size) break
            val bx = buttonAreaX + i * (buttonWidth + buttonGap)
            if (mouseX >= bx && mouseX <= bx + buttonWidth && mouseY >= arrowY && mouseY <= arrowY + buttonHeight) {
                selectedIndex = entryIndex
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double,
    ): Boolean {
        val entries = currentEntries()
        val delta = if (verticalAmount > 0) -1 else 1
        val buttonAreaX = pad + buttonHeight + buttonGap
        val buttonAreaWidth = width - pad - buttonHeight - buttonGap - buttonAreaX
        val visibleCount = (buttonAreaWidth + buttonGap) / (buttonWidth + buttonGap)
        scrollOffset = (scrollOffset + delta).coerceIn(0, (entries.size - visibleCount).coerceAtLeast(0))
        return true
    }

    //  Drawing helpers

    private fun drawBorder(
        context: DrawContext,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        color: Int,
    ) {
        context.fill(x, y, x + w, y + 1, color)
        context.fill(x, y + h - 1, x + w, y + h, color)
        context.fill(x, y, x + 1, y + h, color)
        context.fill(x + w - 1, y, x + w, y + h, color)
    }

    private fun drawLineSegment(
        context: DrawContext,
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        color: Int,
    ) {
        if (y1 == y2) {
            context.fill(x1, y1, x2, y1 + 1, color)
            return
        }
        val midX = (x1 + x2) / 2
        context.fill(x1, y1, midX, y1 + 1, color)
        context.fill(midX, minOf(y1, y2), midX + 1, maxOf(y1, y2) + 1, color)
        context.fill(midX, y2, x2, y2 + 1, color)
    }

    //  Formatters

    private fun fmtLong(value: Long) = "%,d".format(value)
}
