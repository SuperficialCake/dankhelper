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
    lateinit var hideUIKey: KeyBinding
    lateinit var resetSessionKey: KeyBinding
    lateinit var moveUIKey: KeyBinding
    lateinit var clothConfigKey: KeyBinding
    lateinit var trendsUIKey: KeyBinding

    fun init() {
        val holder = AutoConfig.getConfigHolder(DankConfig::class.java)
        val config = holder.config

        resetSessionKey =
            registerKeyBinding(
                KeyBinding(
                    "key.dankhelper.resetSessionStats",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_END,
                    "key.categories.dankhelper",
                ),
            )

        hideUIKey =
            registerKeyBinding(
                KeyBinding(
                    "key.dankhelper.hideUi",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_H,
                    "key.categories.dankhelper",
                ),
            )

        moveUIKey =
            registerKeyBinding(
                KeyBinding(
                    "key.dankhelper.moveUI",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_COMMA,
                    "key.categories.dankhelper",
                ),
            )

        clothConfigKey =
            registerKeyBinding(
                KeyBinding(
                    "key.dankhelper.openConfig",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_BACKSLASH,
                    "key.categories.dankhelper",
                ),
            )

        trendsUIKey =
            registerKeyBinding(
                KeyBinding(
                    "key.dankhelper.openTrendsScreen",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_PAGE_DOWN,
                    "key.categories.dankhelper",
                ),
            )

        ClientTickEvents.END_CLIENT_TICK.register(
            ClientTickEvents.EndTick { client ->
                while (hideUIKey.wasPressed()) {
                    config.showHUD = !config.showHUD
                    holder.save()
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
