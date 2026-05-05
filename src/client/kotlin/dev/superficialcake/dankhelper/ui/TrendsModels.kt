package dev.superficialcake.dankhelper.ui

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class DayStats(
    val date: LocalDate,
    val sessions: Int,
    val totalMoney: BigDecimal,
    val totalTokens: Long,
    val totalCrates: Long,
    val totalKeys: Long,
    val totalBlocks: Long,
    val totalSwings: Long,
    val minuteCount: Int,
    val totalMomentum: Long,
    val totalArtifact: Long,
    val moneyTimeline: List<Double>,
    val tokenTimeline: List<Double>,
) {
    val avgMpm: BigDecimal
        get() = if (minuteCount > 0) totalMoney.divide(BigDecimal(minuteCount), 2, RoundingMode.HALF_UP) else BigDecimal.ZERO
    val avgTpm: Long get() = if (minuteCount > 0) totalTokens / minuteCount else 0L
    val avgCpm: Long get() = if (minuteCount > 0) totalCrates / minuteCount else 0L
    val avgKpm: Long get() = if (minuteCount > 0) totalKeys / minuteCount else 0L
    val avgBpm: Long get() = if (minuteCount > 0) totalBlocks / minuteCount else 0L
    val avgSpm: Long get() = if (minuteCount > 0) totalSwings / minuteCount else 0L
}

data class CfEntry(
    val date: LocalDate,
    val timestamp: String,
    val totalMoney: BigDecimal,
    val totalTokens: Long,
    val totalCrates: Long,
    val totalKeys: Long,
    val totalBlocks: Long,
    val totalSwings: Long,
    val minuteCount: Int,
    val moneyTimeline: List<Double>,
    val tokenTimeline: List<Double>,
) {
    val avgMpm: BigDecimal
        get() = if (minuteCount > 0) totalMoney.divide(BigDecimal(minuteCount), 2, RoundingMode.HALF_UP) else BigDecimal.ZERO
    val avgTpm: Long get() = if (minuteCount > 0) totalTokens / minuteCount else 0L
    val avgCpm: Long get() = if (minuteCount > 0) totalCrates / minuteCount else 0L
    val avgKpm: Long get() = if (minuteCount > 0) totalKeys / minuteCount else 0L
    val avgBpm: Long get() = if (minuteCount > 0) totalBlocks / minuteCount else 0L
    val avgSpm: Long get() = if (minuteCount > 0) totalSwings / minuteCount else 0L
}

data class FfEntry(
    val date: LocalDate,
    val timestamp: String,
    val tokens: Double,
    val keys: Long,
    val fish: Long,
    val casts: Long,
)
