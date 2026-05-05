package dev.superficialcake.dankhelper.handlers

import dev.superficialcake.dankhelper.config.DankConfig
import dev.superficialcake.dankhelper.util.RewardsWebhook
import dev.superficialcake.dankhelper.util.UtilFunctions
import dev.superficialcake.dankhelper.util.UtilFunctions.parseSuffixedNum
import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.slf4j.LoggerFactory

object MessageHandler {
    private var lastProcessTime: Long = 0
    private val MINING_PATTERN =
        """\$([\d.,\w]+),\s+([\d.,]+)\s+tokens,\s+([\d.,]+)\s+Crates\s+and\s+([\d.,]+)\s+Keys\s+from\s+([\d.,]+)\s+blocks\s+with\s+([\d.,]+)\s+swings"""
            .toRegex()
    private val FF_SUMMARY_PATTERN =
        """([\d.,]+)\s+Tokens,\s+and\s+([\d.,]+)\s+rare keys\s+from\s+([\d.,]+)\s+fish\s+with\s+([\d.,]+)\s+casts"""
            .toRegex()
    private val FORTUNE_PATTERN = """^\((.*)\) Increased Fortune: \+(\d+)""".toRegex()
    private val MOMENTUM_PATTERN = """^\((Enchants)\) Increased Momentum: \+(\d+)""".toRegex()
    private val ARTIFACT_PATTERN =
        """^(\(Mining\)|\(Fishing\)|\(AutoMiner\)|\(OverDrive\)).*? (\d+)x (?!Random)(.*?) (Artifact)"""
            .toRegex(RegexOption.IGNORE_CASE)
    private val RANKUP_PATTERN = """\(Rankup\).*?Cost:\s*\$?([\d,]+)""".toRegex()
    private val REWARDS_PATTERN = """.* has (Mined|Fished) ([\d]+)x (.*)""".toRegex()
    private var inCF: Boolean = false
    private val configHolder = AutoConfig.getConfigHolder(DankConfig::class.java)
    private val config get() = configHolder.config

    private val logger = LoggerFactory.getLogger("dankhelper-chat")

    private val username = MinecraftClient.getInstance().session.username
    private val uuid = MinecraftClient.getInstance().gameProfile.id
    private val strippedUUID = uuid.toString().replace("-", "")

    fun onGameMessage(
        message: Text,
        overlay: Boolean,
    ) {
        val text = message.string
        if (text.startsWith("Personal Champion Frenzy Event has been Activated")) {
            inCF = true
            if (config.championFrenzyHudLogging) DataHandler.prepareCFFile()
            val toastMsg = if (config.championFrenzyHudLogging) {
                "Champion Frenzy has started"
            }
            else {
                "Champion Frenzy has started. UI updating paused"
            }
            UtilFunctions.showToast("Champion Frenzy Started", toastMsg)
        }
        if (text.startsWith("Personal Champion Frenzy Event has been Deactivated")) {
            inCF = false
            val toastMsg =
                if (config.championFrenzyHudLogging) {
                    "Champion Frenzy has ended"
                } else {
                    "Champion Frenzy has ended . UI updating resumed"
                }
            UtilFunctions.showToast("Champion Frenzy Ended", toastMsg)
        }
        if (text.startsWith("(Rankup)")) {
            val match = RANKUP_PATTERN.find(text) ?: return
            val costStr = match.groupValues[1].replace(",", "")
            val costVal = costStr.toBigDecimalOrNull() ?: return

            StatsManager.addMoneySpent(costVal)
            return
        }

        when {
            text.contains("Increased Fortune") -> {
                val matchFortune = FORTUNE_PATTERN.find(text) ?: return
                val (_, amount) = matchFortune.destructured

                StatsManager.addFortune(amount.toLong())
                logger.info("Fortune increased to ${StatsManager.sumFortune}")
            }

            text.contains("Artifact") -> {
                val matchArtifact = ARTIFACT_PATTERN.find(text) ?: return
                val (_, amount) = matchArtifact.destructured

                StatsManager.addArtifact(amount.toLong())
                logger.info("Found ${StatsManager.sumArtifact} this session")
            }

            text.contains("Increased Momentum") -> {
                val matchMomentum = MOMENTUM_PATTERN.find(text) ?: return
                val (_, amount) = matchMomentum.destructured

                StatsManager.addMomentum(amount.toLong())
                logger.info("Momentum increased to ${StatsManager.sumMomentum}")
            }

            text.contains(username) -> {
                val matchRewards = REWARDS_PATTERN.find(text) ?: return

                val (action, amount, reward) = matchRewards.destructured
                if (config.webhookURL.isNotBlank()) {
                    RewardsWebhook.sendReward(username, strippedUUID, action, amount, reward)
                }
            }

            text.startsWith("(ChampionFrenzy) You've earned") -> {
                val match = MINING_PATTERN.find(text) ?: return
                val (money, tokens, crates, keys, blocks, swings) = match.destructured
                val moneyVal = parseSuffixedNum(money).toPlainString()

                val csvRow =
                    "$moneyVal,${tokens.replace(",", "")},${crates.replace(",", "")}," +
                        "${keys.replace(",", "")},${blocks.replace(",", "")},${swings.replace(",", "")}"

                if (config.championFrenzyHudLogging) {
                    DataHandler.saveFrenzy("champion", "Money,Tokens,Crates,Keys,Blocks,Swings", csvRow)
                }
            }

            text.startsWith("(FishingFrenzy) You've earned") -> {
                val match = FF_SUMMARY_PATTERN.find(text) ?: return
                val (tokens, keys, fish, casts) = match.destructured

                val csvRow =
                    "${tokens.replace(",", "")},${keys.replace(",", "")}," +
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

    private fun processMiningMessage(
        text: String,
        isCF: Boolean,
    ) {
        val match = MINING_PATTERN.find(text) ?: return
        val (moneyStr, tokensStr, crates, keys, blocks, swings) = match.destructured

        val moneyVal = parseSuffixedNum(moneyStr)
        val tokensVal = tokensStr.replace(",", "").toDoubleOrNull()?.toLong() ?: 0L

        val cratesVal = crates.replace(",", "").toLongOrNull() ?: 0L
        val swingsVal = swings.replace(",", "").toLongOrNull() ?: 0L
        val keysVal = keys.replace(",", "").toLongOrNull() ?: 0L
        val blocksVal = blocks.replace(",", "").toLongOrNull() ?: 0L

        val shouldSendCF = isCF && config.championFrenzyHudLogging
        StatsManager.updateStats(moneyVal, tokensVal, cratesVal, keysVal, swingsVal, blocksVal, shouldSendCF)
    }
}
