package dev.superficialcake.dankhelper.handlers

import dev.superficialcake.dankhelper.Util.praseSuffixedNum
import net.minecraft.text.Text
import org.slf4j.LoggerFactory

object MessageHandler {

    private var lastProcessTime: Long = 0
    private val MINING_PATTERN = """\$([\d.,\w]+),\s+([\d.,]+)\s+tokens,\s+([\d.,]+)\s+Crates\s+and\s+([\d.,]+)\s+Keys\s+from\s+([\d.,]+)\s+blocks\s+with\s+([\d.,]+)\s+swings""".toRegex()
    private var inCF: Boolean = false

    private val logger = LoggerFactory.getLogger("dankhelper-chat")



    fun onGameMessage(message: Text, overlay: Boolean) {

        val text = message.string
        if (text.startsWith("Personal Champion Frenzy Event has been Activated")){
            inCF = true
        }
        if (text.startsWith("Personal Champion Frenzy Event has been Deactivated")){
            inCF = false
        }
        if (!text.startsWith("(Mining) You've earned")) return

        val currentTime = System.currentTimeMillis()

        if ((currentTime - lastProcessTime < 1000) || inCF) {
            return
        }

        lastProcessTime = currentTime
        processMiningMessage(text)
    }

    private fun processMiningMessage(text: String) {
        val match = MINING_PATTERN.find(text) ?: return
        val (moneyStr, tokensStr, crates, keys, blocks, swings) = match.destructured

        val moneyVal = praseSuffixedNum(moneyStr)
        val tokensVal = tokensStr.replace(",", "").toDoubleOrNull()?.toLong() ?: 0L

        val cratesVal = crates.replace(",", "").toLongOrNull() ?: 0L
        val swingsVal = swings.replace(",", "").toLongOrNull() ?: 0L
        val keysVal = keys.replace(",", "").toLongOrNull() ?: 0L
        val blocksVal = blocks.replace(",", "").toLongOrNull() ?: 0L

        StatsManager.updateStats(moneyVal, tokensVal, cratesVal, keysVal, swingsVal, blocksVal)
    }

}