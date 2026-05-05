package dev.superficialcake.dankhelper.handlers

import dev.superficialcake.dankhelper.config.DankConfig
import dev.superficialcake.dankhelper.ui.EditHud
import dev.superficialcake.dankhelper.ui.TrendsScreen
import dev.superficialcake.dankhelper.util.UtilFunctions
import me.shedaniel.autoconfig.AutoConfig
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object KeybindHandler {
    private const val CATEGORY = "key.categories.dankhelper"

    lateinit var hideUIKey: KeyBinding
    lateinit var resetSessionKey: KeyBinding
    lateinit var moveUIKey: KeyBinding
    lateinit var clothConfigKey: KeyBinding
    lateinit var trendsUIKey: KeyBinding

    var showUI: Boolean = true

    fun init() {
        resetSessionKey =
            registerKeyBinding(
                KeyBinding(
                    "key.dankhelper.resetSessionStats",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_END,
                    CATEGORY,
                ),
            )

        hideUIKey =
            registerKeyBinding(
                KeyBinding(
                    "key.dankhelper.hideUI",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_H,
                    CATEGORY,
                ),
            )

        moveUIKey =
            registerKeyBinding(
                KeyBinding(
                    "key.dankhelper.moveUi",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_COMMA,
                    CATEGORY,
                ),
            )

        clothConfigKey =
            registerKeyBinding(
                KeyBinding(
                    "key.dankhelper.openConfig",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_BACKSLASH,
                    CATEGORY,
                ),
            )

        trendsUIKey =
            registerKeyBinding(
                KeyBinding(
                    "key.dankhelper.openTrendsScreen",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_PAGE_DOWN,
                    CATEGORY,
                ),
            )

        ClientTickEvents.END_CLIENT_TICK.register(
            ClientTickEvents.EndTick { client ->
                while (hideUIKey.wasPressed()) {
                    showUI = !showUI
                }
                while (resetSessionKey.wasPressed()) {
                    UtilFunctions.resetAll()
                }
                while (moveUIKey.wasPressed()) {
                    if (client.currentScreen == null) {
                        client.setScreen(EditHud())
                    }
                }
                while (clothConfigKey.wasPressed()) {
                    client.setScreen(
                        AutoConfig.getConfigScreen(DankConfig::class.java, client.currentScreen).get(),
                    )
                }
                while (trendsUIKey.wasPressed()) {
                    if (client.currentScreen == null) {
                        client.setScreen(TrendsScreen())
                    }
                }
            },
        )
    }
}
