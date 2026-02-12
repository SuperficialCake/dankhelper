package dev.superficialcake.dankhelper.handlers

import dev.superficialcake.dankhelper.Util
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object KeybindHandler {

    private const val CATEGORY = "key.categories.dankhelper"

    lateinit var hideUIKey: KeyBinding
    lateinit var resetSessionKey: KeyBinding

    var showUI: Boolean = true

    fun register(){

        resetSessionKey = registerKeyBinding(
            KeyBinding(
                "key.dankhelper.resetSessionStats",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY
            )
        )

        hideUIKey = registerKeyBinding(
            KeyBinding(
                "key.dankhelper.hideUI",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                CATEGORY
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick{ client ->
            while (hideUIKey.wasPressed()){
                showUI = !showUI
            }
            while (resetSessionKey.wasPressed()){
                Util.resetAll()
            }
        })

    }

}