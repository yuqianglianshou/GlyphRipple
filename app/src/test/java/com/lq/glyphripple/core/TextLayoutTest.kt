package com.lq.glyphripple.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TextLayoutTest {

    private fun solid(width: Int, height: Int) = GlyphGrid(width, height, BooleanArray(width * height) { true })

    private fun assertInside(glyphs: List<PlacedGlyph>, width: Float, height: Float) {
        for (glyph in glyphs) {
            for (i in 0 until glyph.count) {
                assertTrue("x=${glyph.xs[i]}", glyph.xs[i] in 0f..width)
                assertTrue("y=${glyph.ys[i]}", glyph.ys[i] in 0f..height)
            }
        }
    }

    @Test
    fun `随机摆放的字都落在画布内`() {
        val grids = List(50) { solid(24, 24) }
        val placed = TextLayout.scattered(grids, 1080f, 1920f, 400f, Random(1))
        assertEquals(50, placed.size)
        assertInside(placed, 1080f, 1920f)
    }

    @Test
    fun `画布比字小时缩小到放得下`() {
        val placed = TextLayout.scattered(listOf(solid(24, 24)), 100f, 100f, 400f, Random(1))
        assertInside(placed, 100f, 100f)
    }

    @Test
    fun `随机摆放跳过空白字符`() {
        val grids = listOf(solid(24, 24), GlyphGrid.blank(12, 24), solid(24, 24))
        assertEquals(2, TextLayout.scattered(grids, 1000f, 1000f, 200f).size)
    }

    @Test
    fun `整句排版自动换行并留在画布内`() {
        val grids = List(14) { solid(24, 24) }
        val placed = TextLayout.sentence(grids, 1080f, 1920f, maxCell = 10f, padding = 60f)
        assertEquals(14, placed.size)
        assertInside(placed, 1080f, 1920f)
        val lineCount = placed.map { it.centerY }.distinct().size
        assertTrue("应分成多行: $lineCount", lineCount > 1)
        // 同一行内从左到右
        placed.zipWithNext().forEach { (a, b) ->
            if (a.centerY == b.centerY) assertTrue(a.centerX < b.centerX)
        }
    }

    @Test
    fun `字太多时缩小格子直到排得下`() {
        val grids = List(200) { solid(24, 24) }
        val placed = TextLayout.sentence(grids, 1080f, 1920f, maxCell = 10f, padding = 60f)
        assertEquals(200, placed.size)
        assertTrue(placed.first().cell < 10f)
        assertInside(placed, 1080f, 1920f)
    }

    @Test
    fun `整句排版整体居中`() {
        val placed = TextLayout.sentence(listOf(solid(24, 24)), 1000f, 600f, maxCell = 5f)
        assertEquals(500f, placed.single().centerX, 1e-3f)
        assertEquals(300f, placed.single().centerY, 1e-3f)
    }
}
