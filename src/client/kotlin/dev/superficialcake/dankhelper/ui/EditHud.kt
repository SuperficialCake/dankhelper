package dev.superficialcake.dankhelper.ui

import dev.superficialcake.dankhelper.config.DankConfig
import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.client.input.MouseButtonEvent

class EditHud : Screen(Component.literal("Edit HUD Position")) {

    private var dragging = false
    private var dragOffsetX = 0.0
    private var dragOffsetY = 0.0
    private val configHolder = AutoConfig.getConfigHolder(DankConfig::class.java)

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, delta)

        val config = configHolder.config
        val x = config.hudX
        val y = config.hudY
        val w = DankHud.currentWidth
        val h = DankHud.currentHeight

        graphics.fill(x - 4, y - 4, x + w + 4, y + h, 0x5500FF00)

        graphics.centeredText(
            minecraft.font,
            Component.translatable("text.ui.dankhelper.move_hud"),
            width / 2,
            20,
            0xFFFFFFFF.toInt()
        )
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val config = configHolder.config
        val x = config.hudX
        val y = config.hudY
        val w = DankHud.currentWidth
        val h = DankHud.currentHeight

        if (event.button() == 0 && event.x >= (x - 4) && event.x <= (x + w + 4) && event.y >= (y - 4) && event.y <= (y + h)) {
            dragging = true
            dragOffsetX = event.x - x
            dragOffsetY = event.y - y
            return true
        }
        return super.mouseClicked(event, doubleClick)
    }


    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
        if (dragging) {
            val config = configHolder.config
            val w = DankHud.currentWidth
            val h = DankHud.currentHeight

            config.hudX = (event.x - dragOffsetX).toInt().coerceIn(0, width - w)
            config.hudY = (event.y - dragOffsetY).toInt().coerceIn(0, height - h)
            return true
        }
        return super.mouseDragged(event, dx, dy)
    }


    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (dragging && event.button() == 0) {
            dragging = false
            configHolder.save()
            return true
        }
        return super.mouseReleased(event)
    }

    override fun isPauseScreen(): Boolean {
        return false
    }
}
