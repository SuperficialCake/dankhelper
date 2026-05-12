package dev.superficialcake.dankhelper.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.superficialcake.dankhelper.config.DankConfig
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> = ConfigScreenFactory { parent -> buildConfigScreen(parent) }
}

fun buildConfigScreen(parent: Screen?): Screen {
    val holder = AutoConfig.getConfigHolder(DankConfig::class.java)
    val config = holder.config
    val eb =
        ConfigBuilder
            .create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("text.autoconfig.dankhelper.title"))
            .setSavingRunnable { holder.save() }

    val entries = eb.entryBuilder()

    // --- HUD Stats ---
    val statsCategory = eb.getOrCreateCategory(Component.literal("HUD Stats"))

    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show HUD"), config.showHUD)
            .setSaveConsumer { config.showHUD = it }
            .build(),
    )

    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show Session Time"), config.showSessionTime)
            .setSaveConsumer { config.showSessionTime = it }
            .build(),
    )
    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show MPM"), config.showMPM)
            .setSaveConsumer { config.showMPM = it }
            .build(),
    )
    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show -MPM (Spent)"), config.showASMPM)
            .setSaveConsumer { config.showASMPM = it }
            .build(),
    )
    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show TPM"), config.showTPM)
            .setSaveConsumer { config.showTPM = it }
            .build(),
    )
    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show CPM"), config.showCPM)
            .setSaveConsumer { config.showCPM = it }
            .build(),
    )
    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show KPM"), config.showKPM)
            .setSaveConsumer { config.showKPM = it }
            .build(),
    )
    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show SPM"), config.showSPM)
            .setSaveConsumer { config.showSPM = it }
            .build(),
    )
    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show BPM"), config.showBPM)
            .setSaveConsumer { config.showBPM = it }
            .build(),
    )
    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show BM"), config.showBM)
            .setSaveConsumer { config.showBM = it }
            .build(),
    )
    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show Fortune"), config.showFortune)
            .setSaveConsumer { config.showFortune = it }
            .build(),
    )
    statsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show Momentum"), config.showMomentum)
            .setSaveConsumer { config.showMomentum = it }
            .build(),
    )

    // --- Graphs ---
    val graphsCategory = eb.getOrCreateCategory(Component.literal("Graphs"))

    graphsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show Money Graph"), config.showMoneyGraph)
            .setSaveConsumer { config.showMoneyGraph = it }
            .build(),
    )
    graphsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show Spent Graph"), config.showSpentGraph)
            .setSaveConsumer { config.showSpentGraph = it }
            .build(),
    )
    graphsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show Token Graph"), config.showTokenGraph)
            .setSaveConsumer { config.showTokenGraph = it }
            .build(),
    )
    graphsCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Show Swings Graph"), config.showSwingsGraph)
            .setSaveConsumer { config.showSwingsGraph = it }
            .build(),
    )

    // --- Webhook ---

    val webhookCategory = eb.getOrCreateCategory(Component.literal("Webhook"))

    webhookCategory.addEntry(
        entries
            .startTextField(Component.literal("Webhook URL"), config.webhookURL)
            .setSaveConsumer { config.webhookURL = it }
            .build(),
    )

    // --- Champion Frenzy ---
    val frenzyCategory = eb.getOrCreateCategory(Component.literal("Champion Frenzy"))

    frenzyCategory.addEntry(
        entries
            .startBooleanToggle(Component.literal("Champion Frenzy HUD Logging"), config.championFrenzyHudLogging)
            .setSaveConsumer { config.championFrenzyHudLogging = it }
            .build(),
    )

    return eb.build()
}
