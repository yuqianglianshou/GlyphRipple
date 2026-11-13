package com.lq.glyphripple.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RippleFieldTest {

    @Test
    fun `圆环在寿命结束时被移除`() {
        val field = RippleField()
        field.spawn(0f, 0f, 10f, 0, lifetime = 1f)
        field.step(0.9f)
        assertEquals(1, field.size)
        field.step(0.2f)
        assertEquals(0, field.size)
    }

    @Test
    fun `移除后保持其余圆环的顺序和数据`() {
        val field = RippleField()
        field.spawn(1f, 0f, 10f, 0, lifetime = 3f)
        field.spawn(2f, 0f, 10f, 0, lifetime = 1f)
        field.spawn(3f, 0f, 10f, 0, lifetime = 3f)
        field.step(1.5f)
        assertEquals(2, field.size)
        assertEquals(1f, field.x(0), 0f)
        assertEquals(3f, field.x(1), 0f)
        assertEquals(0.5f, field.progress(1), 1e-6f)
    }

    @Test
    fun `结果只取决于经过的时间而不是步数`() {
        val coarse = RippleField().apply { spawn(0f, 0f, 10f, 0) }
        val fine = RippleField().apply { spawn(0f, 0f, 10f, 0) }
        coarse.step(1f)
        repeat(100) { fine.step(0.01f) }
        assertEquals(coarse.radius(0), fine.radius(0), 1e-3f)
        assertEquals(coarse.alpha(0), fine.alpha(0), 1e-3f)
    }

    @Test
    fun `延迟中的圆环进度为负`() {
        val field = RippleField()
        field.spawn(0f, 0f, 10f, 0, delay = 0.5f)
        field.step(0.2f)
        assertTrue(field.progress(0) < 0f)
        field.step(0.4f)
        assertTrue(field.progress(0) > 0f)
    }

    @Test
    fun `半径递增而不透明度递减`() {
        val field = RippleField()
        field.spawn(0f, 0f, 10f, 0, lifetime = 1f)
        var radius = field.radius(0)
        var alpha = field.alpha(0)
        assertEquals(0f, radius, 0f)
        assertEquals(1f, alpha, 0f)
        repeat(9) {
            field.step(0.1f)
            assertTrue(field.radius(0) > radius)
            assertTrue(field.alpha(0) < alpha)
            radius = field.radius(0)
            alpha = field.alpha(0)
        }
        assertTrue(radius <= 10f)
    }

    @Test
    fun `满了以后丢弃新圆环`() {
        val field = RippleField(capacity = 2)
        assertTrue(field.spawn(0f, 0f, 1f, 0))
        assertTrue(field.spawn(0f, 0f, 1f, 0))
        assertFalse(field.spawn(0f, 0f, 1f, 0))
        assertEquals(2, field.size)
    }
}
