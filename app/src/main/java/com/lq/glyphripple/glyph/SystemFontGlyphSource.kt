package com.lq.glyphripple.glyph

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.lq.glyphripple.core.GlyphGrid
import com.lq.glyphripple.core.GlyphSource
import kotlin.math.ceil

/**
 * 用系统字体生成点阵：把字符画到一张 [size] 像素高的小位图上，
 * 一个像素对应一个格子，不透明度过阈值即为有墨。支持系统能显示的任何字符。
 */
class SystemFontGlyphSource : GlyphSource {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }

    override fun glyph(grapheme: String, size: Int): GlyphGrid? {
        if (grapheme.isBlank()) return null
        paint.textSize = size * TEXT_SCALE
        val width = ceil(paint.measureText(grapheme)).toInt().coerceIn(1, size * 2)
        val metrics = paint.fontMetrics
        val baseline = (size - metrics.ascent - metrics.descent) / 2f

        val bitmap = Bitmap.createBitmap(width, size, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawText(grapheme, 0f, baseline, paint)
        val pixels = IntArray(width * size)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, size)
        bitmap.recycle()

        val cells = BooleanArray(pixels.size) { Color.alpha(pixels[it]) >= ALPHA_THRESHOLD }
        return GlyphGrid(width, size, cells)
    }

    private companion object {
        const val TEXT_SCALE = 0.92f
        const val ALPHA_THRESHOLD = 0x58
    }
}
