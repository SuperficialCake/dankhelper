package dev.superficialcake.dankhelper.ui

import net.minecraft.client.MinecraftClient
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object TrendsLoader {
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Sessions

    fun loadSessions(): List<DayStats> {
        val sessionsFolder = folder("sessions") ?: return emptyList()

        sessionsFolder.listFiles { f -> f.extension == "csv" }?.forEach { csvFile ->
            val hasData =
                runCatching { csvFile.readLines() }
                    .getOrElse { emptyList() }
                    .drop(1)
                    .any { it.isNotBlank() }
            if (!hasData) runCatching { csvFile.delete() }
        }

        val remaining = sessionsFolder.listFiles { f -> f.extension == "csv" } ?: return emptyList()
        val byDate = mutableMapOf<LocalDate, MutableList<File>>()

        for (csvFile in remaining) {
            runCatching {
                LocalDate.parse(csvFile.nameWithoutExtension.substringBeforeLast("-"), dateFmt)
            }.onSuccess { date ->
                byDate.getOrPut(date) { mutableListOf() }.add(csvFile)
            }
        }

        return byDate.entries
            .sortedByDescending { it.key }
            .map { (date, files) -> aggregateSessionDay(date, files) }
    }

    private data class SessionRow(
        val money: BigDecimal,
        val tokens: Long,
        val crates: Long,
        val keys: Long,
        val blocks: Long,
        val swings: Long,
        val momentum: Long,
        val artifact: Long,
    )

    private fun aggregateSessionDay(
        date: LocalDate,
        files: List<File>,
    ): DayStats {
        val rows = mutableListOf<SessionRow>()

        for (csvFile in files) {
            for (line in readDataLines(csvFile)) {
                val columns = line.split(",")
                if (columns.size < 7) continue
                val money = columns[1].toBigDecimalOrNull() ?: continue
                val momentum = if (columns.size > 9) columns[9].toLongOrNull() ?: 0L else 0L
                val artifact = if (columns.size > 10) columns[10].toLongOrNull() ?: 0L else 0L
                rows.add(
                    SessionRow(
                        money = money,
                        tokens = columns[2].toLongOrNull() ?: 0L,
                        crates = columns[3].toLongOrNull() ?: 0L,
                        keys = columns[4].toLongOrNull() ?: 0L,
                        blocks = columns[5].toLongOrNull() ?: 0L,
                        swings = columns[6].toLongOrNull() ?: 0L,
                        momentum = momentum,
                        artifact = artifact,
                    ),
                )
            }
        }

        var totalMoney = BigDecimal.ZERO
        var totalTokens = 0L
        var totalCrates = 0L
        var totalKeys = 0L
        var totalBlocks = 0L
        var totalSwings = 0L

        // New variables to hold the sum of the "final" values from each session
        var totalMomentum = 0L
        var totalArtifact = 0L

        var minuteCount = 0
        val moneyTimeline = mutableListOf<Double>()
        val tokenTimeline = mutableListOf<Double>()

        for (csvFile in files) {
            val fileLines = readDataLines(csvFile)
            if (fileLines.isEmpty()) continue

            for (line in fileLines) {
                val columns = line.split(",")
                if (columns.size < 7) continue

                val money = columns[1].toBigDecimalOrNull() ?: continue
                totalMoney += money
                totalTokens += columns[2].toLongOrNull() ?: 0L
                totalCrates += columns[3].toLongOrNull() ?: 0L
                totalKeys += columns[4].toLongOrNull() ?: 0L
                totalBlocks += columns[5].toLongOrNull() ?: 0L
                totalSwings += columns[6].toLongOrNull() ?: 0L

                minuteCount++
                moneyTimeline.add(money.toDouble())
                tokenTimeline.add(columns[2].toDoubleOrNull() ?: 0.0)
            }

            val lastLine = fileLines.lastOrNull()?.split(",")
            if (lastLine != null && lastLine.size >= 11) {
                val sessionFinalMomentum = lastLine[9].toLongOrNull() ?: 0L
                val sessionFinalArtifact = lastLine[10].toLongOrNull() ?: 0L

                totalMomentum += sessionFinalMomentum
                totalArtifact += sessionFinalArtifact
            }
        }

        return DayStats(
            date = date,
            sessions = files.size,
            totalMoney = totalMoney,
            totalTokens = totalTokens,
            totalCrates = totalCrates,
            totalKeys = totalKeys,
            totalBlocks = totalBlocks,
            totalSwings = totalSwings,
            minuteCount = minuteCount,
            totalMomentum = totalMomentum, // Sum of all final session values
            totalArtifact = totalArtifact, // Sum of all final session values
            moneyTimeline = moneyTimeline,
            tokenTimeline = tokenTimeline,
        )
    }

    // Champion Frenzies

    fun loadChampionFrenzies(): List<CfEntry> {
        val cfFolder = folder("frenzies/champion") ?: return emptyList()
        val files = cfFolder.listFiles { f -> f.extension == "csv" } ?: return emptyList()
        return files
            .filter { !it.nameWithoutExtension.startsWith("CHAMPION-") }
            .mapNotNull { parseCfFile(it) }
            .sortedByDescending { it.date.toString() + it.timestamp }
    }

    private fun parseCfFile(csvFile: File): CfEntry? {
        val nameWithoutExt = csvFile.nameWithoutExtension
        val date =
            runCatching {
                LocalDate.parse(nameWithoutExt.substringBeforeLast("-"), dateFmt)
            }.getOrElse { return null }
        val sessionNum = nameWithoutExt.substringAfterLast("-")
        val lines = readDataLines(csvFile)
        if (lines.isEmpty()) return null

        var firstTimestamp = ""
        var totalMoney = BigDecimal.ZERO
        var totalTokens = 0L
        var totalCrates = 0L
        var totalKeys = 0L
        var totalBlocks = 0L
        var totalSwings = 0L
        var minuteCount = 0
        val moneyTimeline = mutableListOf<Double>()
        val tokenTimeline = mutableListOf<Double>()

        for (line in lines) {
            val columns = line.split(",")
            if (columns.size < 7) continue
            if (firstTimestamp.isEmpty()) firstTimestamp = columns[0].take(5)
            val money = columns[1].toBigDecimalOrNull() ?: continue
            totalMoney += money
            totalTokens += columns[2].toLongOrNull() ?: 0L
            totalCrates += columns[3].toLongOrNull() ?: 0L
            totalKeys += columns[4].toLongOrNull() ?: 0L
            totalBlocks += columns[5].toLongOrNull() ?: 0L
            totalSwings += columns[6].toLongOrNull() ?: 0L
            minuteCount++
            moneyTimeline.add(money.toDouble())
            tokenTimeline.add((columns[2].toLongOrNull() ?: 0L).toDouble())
        }

        if (minuteCount == 0) return null

        // If fewer than 15 minutes were recorded, extrapolate the remainder
        // from the corresponding summary file: CHAMPION-{date}-summary-{N}.csv
        if (minuteCount < 15) {
            val summaryFile =
                File(
                    csvFile.parentFile,
                    "CHAMPION-${date.format(dateFmt)}-summary-$sessionNum.csv",
                )
            if (summaryFile.exists()) {
                val summaryLine = readDataLines(summaryFile).firstOrNull()
                if (summaryLine != null) {
                    val summaryColumns = summaryLine.split(",")
                    if (summaryColumns.size >= 7) {
                        val summaryMoney = summaryColumns[1].toBigDecimalOrNull()
                        val summaryTokens = summaryColumns[2].toDoubleOrNull()?.toLong()
                        val summaryCrates = summaryColumns[3].toLongOrNull()
                        val summaryKeys = summaryColumns[4].toLongOrNull()
                        val summaryBlocks = summaryColumns[5].toLongOrNull()
                        val summarySwings = summaryColumns[6].toLongOrNull()

                        if (summaryMoney != null && summaryTokens != null && summaryCrates != null &&
                            summaryKeys != null && summaryBlocks != null && summarySwings != null
                        ) {
                            val missingMinutes = 15 - minuteCount
                            val remainingMoney = (summaryMoney - totalMoney).coerceAtLeast(BigDecimal.ZERO)
                            val remainingTokens = (summaryTokens - totalTokens).coerceAtLeast(0L)
                            val remainingCrates = (summaryCrates - totalCrates).coerceAtLeast(0L)
                            val remainingKeys = (summaryKeys - totalKeys).coerceAtLeast(0L)
                            val remainingBlocks = (summaryBlocks - totalBlocks).coerceAtLeast(0L)
                            val remainingSwings = (summarySwings - totalSwings).coerceAtLeast(0L)
                            val moneyPerMissingMinute = remainingMoney.divide(BigDecimal(missingMinutes), 2, RoundingMode.HALF_UP)

                            repeat(missingMinutes) {
                                totalMoney += moneyPerMissingMinute
                                totalTokens += remainingTokens / missingMinutes
                                totalCrates += remainingCrates / missingMinutes
                                totalKeys += remainingKeys / missingMinutes
                                totalBlocks += remainingBlocks / missingMinutes
                                totalSwings += remainingSwings / missingMinutes
                                minuteCount++
                                moneyTimeline.add(moneyPerMissingMinute.toDouble())
                                tokenTimeline.add((remainingTokens / missingMinutes).toDouble())
                            }
                        }
                    }
                }
            }
        }

        return CfEntry(
            date = date,
            timestamp = firstTimestamp,
            totalMoney = totalMoney,
            totalTokens = totalTokens,
            totalCrates = totalCrates,
            totalKeys = totalKeys,
            totalBlocks = totalBlocks,
            totalSwings = totalSwings,
            minuteCount = minuteCount,
            moneyTimeline = moneyTimeline,
            tokenTimeline = tokenTimeline,
        )
    }

    // Fishing Frenzies

    fun loadFishingFrenzies(): List<FfEntry> {
        val ffFolder = folder("frenzies/fishing") ?: return emptyList()
        val files = ffFolder.listFiles { f -> f.extension == "csv" } ?: return emptyList()
        return files
            .mapNotNull { parseFfFile(it) }
            .sortedByDescending { it.date.toString() + it.timestamp }
    }

    private fun parseFfFile(csvFile: File): FfEntry? {
        val stripped =
            csvFile.nameWithoutExtension
                .removePrefix("FISHING-")
                .substringBefore("-summary")
        val date = runCatching { LocalDate.parse(stripped, dateFmt) }.getOrElse { return null }
        val lines = readDataLines(csvFile)
        if (lines.isEmpty()) return null
        val columns = lines.first().split(",")
        if (columns.size < 5) return null
        return FfEntry(
            date = date,
            timestamp = columns[0].take(5),
            tokens = columns[1].toDoubleOrNull() ?: return null,
            keys = columns[2].toLongOrNull() ?: 0L,
            fish = columns[3].toLongOrNull() ?: 0L,
            casts = columns[4].toLongOrNull() ?: 0L,
        )
    }

    // Helpers

    private fun folder(subpath: String): File? {
        val dir = File(MinecraftClient.getInstance().runDirectory, "dankhelper/$subpath")
        return if (dir.exists()) dir else null
    }

    private fun readDataLines(csvFile: File): List<String> =
        runCatching { csvFile.readLines() }
            .getOrElse { emptyList() }
            .drop(1)
            .filter { it.isNotBlank() }
}
