package dev.superficialcake.dankhelper.handlers

import dev.superficialcake.dankhelper.util.UtilFunctions
import dev.superficialcake.dankhelper.util.UtilFunctions.parseSuffixedNum
import net.minecraft.text.Text
import org.slf4j.LoggerFactory

object MessageHandler {

    private var lastProcessTime: Long = 0
    private val MINING_PATTERN = """\$([\d.,\w]+),\s+([\d.,]+)\s+tokens,\s+([\d.,]+)\s+Crates\s+and\s+([\d.,]+)\s+Keys\s+from\s+([\d.,]+)\s+blocks\s+with\s+([\d.,]+)\s+swings""".toRegex()
    private val FF_SUMMARY_PATTERN = """([\d.,]+)\s+Tokens,\s+and\s+([\d.,]+)\s+rare keys\s+from\s+([\d.,]+)\s+fish\s+with\s+([\d.,]+)\s+casts""".toRegex()
    private val FORTUNE_PATTERN = """\((.*)\) Increased Fortune: \+(\d+)""".toRegex()
    private var inCF: Boolean = false

    private val logger = LoggerFactory.getLogger("dankhelper-chat")



    fun onGameMessage(message: Text, overlay: Boolean) {

        val text = message.string
        if (text.startsWith("Personal Champion Frenzy Event has been Activated")){
            inCF = true
            DataHandler.prepareCFFile()
            UtilFunctions.showToast("Champion Frenzy Started", "A Champion Frenzy has started. UI updating paused")
        }
        if (text.startsWith("Personal Champion Frenzy Event has been Deactivated")){
            inCF = false
            UtilFunctions.showToast("Champion Frenzy Ended", "A Champion Frenzy has ended. UI updating resumed")
        }

        when {
            text.contains("Increased Fortune") ->{
                val matchFortune = FORTUNE_PATTERN.find(text) ?: return
                val (source, amount) = matchFortune.destructured

                StatsManager.addFortune(amount.toLong())
                logger.info("Fortune increased to ${StatsManager.sumFortune}")
            }

            text.startsWith("(ChampionFrenzy) You've earned") -> {
                val match = MINING_PATTERN.find(text) ?: return
                val (money, tokens, crates, keys, blocks, swings) = match.destructured
                val moneyVal = parseSuffixedNum(money).toPlainString()

                val csvRow = "$moneyVal,${tokens.replace(",", "")},${crates.replace(",", "")}," +
                        "${keys.replace(",", "")},${blocks.replace(",", "")},${swings.replace(",", "")}"

                DataHandler.saveFrenzy("champion", "Money,Tokens,Crates,Keys,Blocks,Swings", csvRow)
            }

            text.startsWith("(FishingFrenzy) You've earned") -> {
                val match = FF_SUMMARY_PATTERN.find(text) ?: return
                val (tokens, keys, fish, casts) = match.destructured

                val csvRow = "${tokens.replace(",", "")},${keys.replace(",", "")}," +
                        "${fish.replace(",", "")},${casts.replace(",", "")}"

                DataHandler.saveFrenzy("fishing", "Tokens,Keys,Fish,Casts", csvRow)
            }
        }

        if (!text.startsWith("(Mining) You've earned")) return

        val currentTime = System.currentTimeMillis()

        if ((currentTime - lastProcessTime < 1000)) {
            return
        }

        lastProcessTime = currentTime
        processMiningMessage(text, inCF)
    }

    private fun processMiningMessage(text: String, isCF: Boolean) {
        val match = MINING_PATTERN.find(text) ?: return
        val (moneyStr, tokensStr, crates, keys, blocks, swings) = match.destructured

        val moneyVal = parseSuffixedNum(moneyStr)
        val tokensVal = tokensStr.replace(",", "").toDoubleOrNull()?.toLong() ?: 0L

        val cratesVal = crates.replace(",", "").toLongOrNull() ?: 0L
        val swingsVal = swings.replace(",", "").toLongOrNull() ?: 0L
        val keysVal = keys.replace(",", "").toLongOrNull() ?: 0L
        val blocksVal = blocks.replace(",", "").toLongOrNull() ?: 0L

        if (isCF){
            DataHandler.logStats(
                moneyVal.toString(), tokensVal, cratesVal, keysVal, blocksVal, swingsVal, 0L, 0L, true
            )
        } else {
            StatsManager.updateStats(moneyVal, tokensVal, cratesVal, keysVal, swingsVal, blocksVal)
        }
    }
}