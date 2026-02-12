package dev.superficialcake.dankhelper.handlers

import java.math.BigDecimal

object StatsManager {

    private var totalUpdates = 0
    private var sumMoney = BigDecimal.ZERO
    private var sumTokens = 0L
    private var sumBlocks = 0L
    private var sumSwings = 0L
    private var sumCrates = 0L
    private var sumKeys = 0L

    var avgMpm = "0"
    var avgTpm = "0"
    var avgBpm = "0"
    var avgCpm = "0"
    var avgSpm = "0"
    var avgKpm = "0"
    val moneyHistory = mutableListOf<Double>()
    val tokenHistory = mutableListOf<Double>()
    val maxHistory = 15

    fun resetStats(){
        avgMpm = "0"
        avgTpm = "0"
        avgBpm = "0"
        avgCpm = "0"
        avgSpm = "0"
        avgKpm = "0"

        totalUpdates = 0
        sumMoney = BigDecimal.ZERO
        sumTokens = 0L
        sumBlocks = 0L
        sumSwings = 0L
        sumCrates = 0L
        sumKeys = 0L

        moneyHistory.clear()
        tokenHistory.clear()
    }

    fun updateStats(money: BigDecimal, tokens: Long, crates: Long, keys: Long, swings: Long, blocks: Long){

        if(moneyHistory.size >= maxHistory) moneyHistory.removeAt(0)
        if(tokenHistory.size >= maxHistory) tokenHistory.removeAt(0)

        moneyHistory.add(money.toDouble())
        tokenHistory.add(tokens.toDouble())

        totalUpdates++

        sumMoney = sumMoney.add(money)
        sumTokens += tokens
        sumCrates += crates
        sumKeys += keys
        sumBlocks += blocks
        sumSwings += swings

        avgMpm = formatMoney(sumMoney.divide(BigDecimal.valueOf(totalUpdates.toLong()), 2, java.math.RoundingMode.HALF_UP))
        avgTpm = "%,d".format(sumTokens / totalUpdates)
        avgCpm = "%,d".format(sumCrates / totalUpdates)
        avgKpm = "%,d".format(sumKeys / totalUpdates)
        avgBpm = "%,d".format(sumBlocks / totalUpdates)
        avgSpm = "%,d".format(sumSwings / totalUpdates)

    }

    private val suffixes = listOf("", "K", "M", "B", "T", "Qd", "Qt", "Sx", "Sp")

    private fun formatMoney(amount: BigDecimal): String {

        if (amount < BigDecimal.valueOf(1000)) return amount.toPlainString()

        val exp = (amount.precision() - amount.scale() -1)/3
        val index = exp.coerceAtMost(suffixes.size - 1)

        val divisor = BigDecimal.TEN.pow(index * 3)
        val shortNumber = amount.divide(divisor, 2, java.math.RoundingMode.HALF_UP)

        return "${shortNumber}${suffixes[index]}"
    }


}