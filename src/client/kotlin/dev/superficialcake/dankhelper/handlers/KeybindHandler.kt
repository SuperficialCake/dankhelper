package dev.superficialcake.dankhelper.handlers

import com.mojang.blaze3d.platform.InputConstants
import dev.superficialcake.dankhelper.DankHelper
import dev.superficialcake.dankhelper.compat.buildConfigScreen
import dev.superficialcake.dankhelper.config.DankConfig
import dev.superficialcake.dankhelper.ui.EditHud
import dev.superficialcake.dankhelper.ui.TrendsScreen
import dev.superficialcake.dankhelper.util.UtilFunctions
import me.shedaniel.autoconfig.AutoConfig
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

object KeybindHandler {
    private val CATEGORY: KeyMapping.Category =
        KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(DankHelper.MOD_ID, "general"),
        )

    lateinit var hideUIKey: KeyMapping
    lateinit var resetSessionKey: KeyMapping
    lateinit var moveUIKey: KeyMapping
    lateinit var clothConfigKey: KeyMapping
    lateinit var trendsUIKey: KeyMapping

    fun init() {
        val config = AutoConfig.getConfigHolder(DankConfig::class.java).config

        resetSessionKey =
            registerKeyMapping(
                KeyMapping(
                    "key.dankhelper.resetSessionStats",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_END,
                    CATEGORY,
                ),
            )

        hideUIKey =
            registerKeyMapping(
                KeyMapping(
                    "key.dankhelper.hideUi",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_H,
                    CATEGORY,
                ),
            )
        moveUIKey =
            registerKeyMapping(
                KeyMapping(
                    "key.dankhelper.moveUi",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_COMMA,
                    CATEGORY,
                ),
            )

        clothConfigKey =
            registerKeyMapping(
                KeyMapping(
                    "key.dankhelper.openConfig",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_BACKSLASH,
                    CATEGORY,
                ),
            )

        trendsUIKey =
            registerKeyMapping(
                KeyMapping(
                    "key.dankhelper.openTrendsScreen",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_PAGE_DOWN,
                    CATEGORY,
                ),
            )

        ClientTickEvents.END_CLIENT_TICK.register(
            ClientTickEvents.EndTick { client ->
                while (hideUIKey.consumeClick()) {
                    config.showHud = !config.showHud
                }
                while (resetSessionKey.consumeClick()) {
                    UtilFunctions.resetAll()
                }
                while (moveUIKey.consumeClick()) {
                    if (client.screen == null) {
                        client.setScreen(EditHud())
                    }
                }
                while (clothConfigKey.consumeClick()) {
                    client.setScreen(buildConfigScreen(client.screen))
                }
                while (trendsUIKey.consumeClick()) {
                    if (client.screen == null) {
                        client.setScreen(TrendsScreen())
                    }
                }
            },
        )
    }
}
