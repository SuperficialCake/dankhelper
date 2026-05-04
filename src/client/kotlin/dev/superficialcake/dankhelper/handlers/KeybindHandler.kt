package dev.superficialcake.dankhelper.handlers

import com.mojang.blaze3d.platform.InputConstants
import dev.superficialcake.dankhelper.DankHelper
import dev.superficialcake.dankhelper.compat.buildConfigScreen
import dev.superficialcake.dankhelper.ui.EditHud
import dev.superficialcake.dankhelper.util.UtilFunctions
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

    var showUI: Boolean = true

    fun init() {
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
                    "key.dankhelper.hideUI",
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

        ClientTickEvents.END_CLIENT_TICK.register(
            ClientTickEvents.EndTick { client ->
                while (hideUIKey.consumeClick()) {
                    showUI = !showUI
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
            },
        )
    }
}
