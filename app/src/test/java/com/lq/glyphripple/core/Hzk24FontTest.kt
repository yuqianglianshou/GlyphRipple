package com.lq.glyphripple.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Hzk24FontTest {

    private val font = Hzk24Font(File("src/main/assets/Hzk24s").readBytes())

    private fun rows(grid: GlyphGrid) = (0 until grid.height).map { row ->
        (0 until grid.width).joinToString("") { col -> if (grid[col, row]) "#" else "." }
    }

    @Test
    fun `一 是一条横线`() {
        val grid = font.glyph("一", 24)!!
        assertEquals(24, grid.width)
        assertEquals(24, grid.height)
        val inked = rows(grid).filter { '#' in it }
        assertTrue("墨迹应集中在少数几行: $inked", inked.size <= 4)
        assertTrue("应有一行几乎横贯全宽", inked.any { row -> row.count { it == '#' } >= 18 })
    }

    @Test
    fun `口 上下左右基本对称且中间留空`() {
        val grid = font.glyph("口", 24)!!
        assertTrue(grid.count in 40..140)
        assertTrue("中心应为空白", !grid[12, 12])
    }

    @Test
    fun `第一个和最后一个汉字都能取到`() {
        assertNotNull(font.glyph("啊", 24))
        assertNotNull(font.glyph("齄", 24))
        assertTrue(font.glyph("啊", 24)!!.count > 0)
    }

    @Test
    fun `字库之外的字符返回 null`() {
        assertNull(font.glyph("A", 24))
        assertNull(font.glyph("😀", 24))
        assertNull(font.glyph("龘", 24)) // 不在 GB2312 中
        assertNull(font.glyph("你好", 24))
        assertNull(font.glyph("", 24))
    }
}
