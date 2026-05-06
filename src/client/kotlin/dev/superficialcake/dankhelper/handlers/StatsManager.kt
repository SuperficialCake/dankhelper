package dev.superficialcake.dankhelper.handlers

import dev.superficialcake.dankhelper.DankHelperClient
import java.math.BigDecimal

object StatsManager {
    private var totalUpdates = 0
    private var sumMoney = BigDecimal.ZERO
    private var sumTokens = 0L
    private var sumBlocks = 0L
    private var sumSwings = 0L
    private var sumCrates = 0L
    private var sumKeys = 0L
    private var sumSpentMoney = BigDecimal.ZERO

    var sumFortune = 0L
    var sumMomentum = 0L
    var sumArtifact = 0L
    var avgMpm = "0"
    var avgSpentPerMinute = "0"
    var avgTpm = "0"
    var avgBpm = "0"
    var avgCpm = "0"
    var avgSpm = "0"
    var avgKpm = "0"
    val moneyHistory = mutableListOf<Double>()
    val tokenHistory = mutableListOf<Double>()
    val spentHistory = mutableListOf<Double>()
    val swingsHistory = mutableListOf<Double>()
    const val MAX_HISTORY = 15

    fun resetStats() {
        avgMpm = "0"
        avgTpm = "0"
        avgBpm = "0"
        avgCpm = "0"
        avgSpm = "0"
        avgKpm = "0"
        avgSpentPerMinute = "0"

        totalUpdates = 0
        sumMoney = BigDecimal.ZERO
        sumTokens = 0L
        sumBlocks = 0L
        sumSwings = 0L
        sumCrates = 0L
        sumKeys = 0L
        sumFortune = 0L
        sumMomentum = 0L
        sumSpentMoney = BigDecimal.ZERO
        sumArtifact = 0L

        moneyHistory.clear()
        spentHistory.clear()
        tokenHistory.clear()
        swingsHistory.clear()
    }

    fun updateStats(
        money: BigDecimal,
        tokens: Long,
        crates: Long,
        keys: Long,
        swings: Long,
        blocks: Long,
        isCF: Boolean = false,
    ) {
        if (moneyHistory.size >= MAX_HISTORY) moneyHistory.removeAt(0)
        if (spentHistory.size >= MAX_HISTORY) spentHistory.removeAt(0)
        if (tokenHistory.size >= MAX_HISTORY) tokenHistory.removeAt(0)
        if (swingsHistory.size >= MAX_HISTORY) swingsHistory.removeAt(0)

        totalUpdates++

        val activeMinutes = totalUpdates.toDouble()

        val elapsedMillis = System.currentTimeMillis() - DankHelperClient.startTime
        val sessionMinutes = elapsedMillis / 60000.0

        val currentAvgSpent =
            if (activeMinutes > 0) {
                sumSpentMoney.divide(BigDecimal.valueOf(activeMinutes), 2, java.math.RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

        moneyHistory.add(money.toDouble())
        tokenHistory.add(tokens.toDouble())
        swingsHistory.add(swings.toDouble())
        spentHistory.add(currentAvgSpent.toDouble())

        sumMoney = sumMoney.add(money)
        sumTokens += tokens
        sumCrates += crates
        sumKeys += keys
        sumBlocks += blocks
        sumSwings += swings

        avgMpm =
            formatMoney(sumMoney.divide(BigDecimal.valueOf(totalUpdates.toLong()), 2, java.math.RoundingMode.HALF_UP))
        avgTpm = "%,d".format(sumTokens / totalUpdates)
        avgCpm = "%,d".format(sumCrates / totalUpdates)
        avgKpm = "%,d".format(sumKeys / totalUpdates)
        avgBpm = "%,d".format(sumBlocks / totalUpdates)
        avgSpm = "%,d".format(sumSwings / totalUpdates)

        DataHandler.logStats(
            money.toPlainString(),
            tokens,
            crates,
            keys,
            blocks,
            swings,
            ScoreboardHandler.sessionBM,
            sumFortune,
            sumMomentum,
            sumArtifact,
            isCF,
        )

        avgSpentPerMinute = formatMoney(currentAvgSpent)
    }

    private val suffixes = listOf("", "K", "M", "B", "T", "Qd", "Qt", "Sx", "Sp")

    private fun formatMoney(amount: BigDecimal): String {
        if (amount < BigDecimal.valueOf(1000)) return amount.toPlainString()

        val exp = (amount.precision() - amount.scale() - 1) / 3
        val index = exp.coerceAtMost(suffixes.size - 1)

        val divisor = BigDecimal.TEN.pow(index * 3)
        val shortNumber = amount.divide(divisor, 2, java.math.RoundingMode.HALF_UP)

        return "${shortNumber}${suffixes[index]}"
    }

    fun addFortune(amount: Long) {
        sumFortune += amount
    }

    fun addMomentum(amount: Long) {
        sumMomentum += amount
    }

    fun addArtifact(amount: Long) {
        sumArtifact += amount
    }

    fun addMoneySpent(amount: BigDecimal) {
        sumSpentMoney = sumSpentMoney.add(amount)
    }

    fun forceSave() {
        if (totalUpdates == 0) return

        val n = totalUpdates.toLong()
        DataHandler.logStats(
            sumMoney.divide(BigDecimal.valueOf(n), 2, java.math.RoundingMode.HALF_UP).toPlainString(),
            sumTokens / n,
            sumCrates / n,
            sumKeys / n,
            sumBlocks / n,
            sumSwings / n,
            ScoreboardHandler.sessionBM,
            sumFortune,
            sumMomentum,
            sumArtifact,
            isCF = false,
        )
    }
}
