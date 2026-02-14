package dev.superficialcake.dankhelper.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.superficialcake.dankhelper.config.DankConfig
import me.shedaniel.autoconfig.AutoConfig

class ModMenuIntegration : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent ->
            AutoConfig.getConfigScreen(DankConfig::class.java, parent).get()
        }
    }

}