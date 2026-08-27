package com.gtladd.gtladditions.api.guide

import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.ResourceLocation

import com.mojang.blaze3d.platform.GlStateManager
import guideme.document.LytRect
import guideme.document.block.LytBlock
import guideme.document.interaction.InteractiveElement
import guideme.layout.LayoutContext
import guideme.render.GuidePageTexture
import guideme.render.RenderContext
import org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL12C.GL_LINEAR_MIPMAP_LINEAR
import org.lwjgl.opengl.GL12C.GL_TEXTURE_MAX_LEVEL
import org.lwjgl.opengl.GL12C.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL30C.glGenerateMipmap
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

private const val TEXT_SIZE = 10f
private const val SUPER_SAMPLE = 4

class LytLatex(private val latexExpression: String) : LytBlock(), InteractiveElement {
    private var texture = GuidePageTexture.missing()
    private var mipmapped: AbstractTexture? = null

    init {
        convertLatexToTexture()
    }

    private fun convertLatexToTexture() {
        try {
            val icon =
                TeXFormula(latexExpression).setColor(Color.white)
                    .createTeXIcon(TeXConstants.STYLE_DISPLAY, TEXT_SIZE * SUPER_SAMPLE)

            val image = BufferedImage(icon.iconWidth, icon.iconHeight, BufferedImage.TYPE_INT_ARGB)
            val g2 = image.createGraphics()

            g2.composite = AlphaComposite.Src
            g2.color = Color(0x00FFFFFF, true)
            g2.fillRect(0, 0, icon.iconWidth, icon.iconHeight)
            g2.composite = AlphaComposite.SrcOver

            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

            icon.paintIcon(null, g2, 0, 0)
            g2.dispose()

            val baos = ByteArrayOutputStream()
            ImageIO.write(image, "png", baos)

            this.texture = GuidePageTexture.load(ResourceLocation("latex", UUID.randomUUID().toString()), baos.toByteArray())
        } catch (_: Exception) {
            this.texture = GuidePageTexture.missing()
        }
    }

    private fun enableMipmaps(tex: AbstractTexture) {
        if (mipmapped === tex) return
        val maxDim = maxOf(texture.size.width(), texture.size.height())
        var levels = 0
        var d = maxDim
        while (d > 1) {
            d = d shr 1
            levels++
        }
        tex.bind()
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, levels)
        glGenerateMipmap(GL_TEXTURE_2D)
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        mipmapped = tex
    }

    override fun computeLayout(context: LayoutContext?, x: Int, y: Int, availableWidth: Int): LytRect {
        val size = this.texture.size
        var width = size.width() / SUPER_SAMPLE
        var height = size.height() / SUPER_SAMPLE
        if (width > availableWidth) {
            val f = (availableWidth.toFloat() / width.toFloat())
            width = (width.toFloat() * f).toInt()
            height = (height.toFloat() * f).toInt()
        }

        return LytRect(x, y, width, height)
    }

    override fun onLayoutMoved(i: Int, i1: Int) = Unit

    override fun renderBatch(renderContext: RenderContext, multiBufferSource: MultiBufferSource) = Unit

    override fun render(context: RenderContext) {
        val gpuTexture = texture.use()
        enableMipmaps(gpuTexture)
        context.fillTexturedRect(getBounds(), texture)
    }
}
