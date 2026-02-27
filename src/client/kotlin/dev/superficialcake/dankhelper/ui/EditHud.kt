package dev.superficialcake.dankhelper.ui

import dev.superficialcake.dankhelper.config.DankConfig
import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class EditHud : Screen(Text.literal("Edit HUD Position")) {

    private var dragging = false
    private var dragOffsetX = 0.0
    private var dragOffsetY = 0.0
    private val configHolder = AutoConfig.getConfigHolder(DankConfig::class.java)

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        val config = configHolder.config
        val x = config.hudX
        val y = config.hudY

        val w = DankHud.currentWidth
        val h = DankHud.currentHeight

        context.fill(x - 4, y - 4, x + w + 4, y + h, 0x5500FF00)

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatable("text.ui.dankhelper.move_hud"),
            width / 2,
            20,
            0xFFFFFF
        )
    }

    override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
        val config = configHolder.config
        val x = config.hudX
        val y = config.hudY
        val w = DankHud.currentWidth
        val h = DankHud.currentHeight

        val mouseX = click.x()
        val mouseY = click.y()
        val button = click.button()

        // Check if the click is within the HUD bounds (+ padding)
        if (button == 0 && mouseX >= (x - 4) && mouseX <= (x + w + 4) && mouseY >= (y - 4) && mouseY <= (y + h)) {
            dragging = true
            dragOffsetX = mouseX - x
            dragOffsetY = mouseY - y
            return true // Tell Minecraft we are handling this click
        }
        return super.mouseClicked(click, doubled)
    }

    override fun mouseDragged(click: Click, deltaX: Double, deltaY: Double): Boolean {
        if (dragging) {
            val config = configHolder.config
            config.hudX = (click.x() - dragOffsetX).toInt()
            config.hudY = (click.y() - dragOffsetY).toInt()
            return true
        }
        return super.mouseDragged(click, deltaX, deltaY)
    }

    override fun mouseReleased(click: Click): Boolean {
        if (dragging && click.button() == 0) {
            dragging = false
            configHolder.save()
            return true
        }
        return super.mouseReleased(click)
    }

    override fun shouldPause(): Boolean {
        return false
    }
}