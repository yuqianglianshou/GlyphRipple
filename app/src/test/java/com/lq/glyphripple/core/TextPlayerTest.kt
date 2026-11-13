package com.lq.glyphripple.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextPlayerTest {

    /** 三个字，分别有 1、2、3 个墨点。 */
    private fun glyphs() = (1..3).map { n ->
        PlacedGlyph(FloatArray(n), FloatArray(n), centerX = 0f, centerY = 0f, cell = 10f)
    }

    private fun player(field: RippleField, loop: Boolean) = TextPlayer(field).apply {
        interval = 1f
        this.loop = loop
    }

    @Test
    fun `按间隔逐字出现`() {
        val field = RippleField()
        val player = player(field, loop = false)
        player.play(::glyphs)
        player.step(0.01f)
        assertEquals(1, field.size)
        player.step(0.5f)
        assertEquals(1, field.size)
        player.step(0.5f)
        assertEquals(3, field.size)
        player.step(1f)
        assertEquals(6, field.size)
        assertTrue(player.finished)
    }

    @Test
    fun `不循环时出完就停`() {
        val field = RippleField()
        val player = player(field, loop = false)
        player.play(::glyphs)
        repeat(100) { player.step(0.5f) }
        assertEquals(6, field.size)
    }

    @Test
    fun `循环时歇息后重新排版再来一轮`() {
        val field = RippleField()
        val player = player(field, loop = true)
        var rounds = 0
        player.play { rounds++; glyphs() }
        repeat(3) { player.step(1f) }
        assertTrue(player.finished)
        assertEquals(1, rounds)
        repeat(8) { player.step(0.5f) }
        assertEquals(2, rounds)
        assertFalse(player.finished)
        assertTrue(field.size > 6)
    }

    @Test
    fun `一帧很长时不会漏字`() {
        val field = RippleField()
        val player = player(field, loop = false)
        player.play(::glyphs)
        player.step(10f)
        assertEquals(6, field.size)
    }
}
