package com.lq.glyphripple.core

import kotlin.math.hypot
import kotlin.random.Random

/**
 * 按节奏把文字一个个放进 [field]：每隔 [interval] 秒出一个字，
 * 字里的墨点从中心向外依次起波。全部出完后，若 [loop] 为真则歇一会儿重来。
 */
class TextPlayer(
    private val field: RippleField,
    private val random: Random = Random.Default,
) {
    /** 相邻两个字之间的间隔（秒）。 */
    var interval: Float = 0.9f

    var loop: Boolean = true

    /** 圆环颜色（ARGB），每个墨点随机取一个。 */
    var colors: IntArray = intArrayOf(-1)

    private var layout: () -> List<PlacedGlyph> = { emptyList() }
    private var glyphs: List<PlacedGlyph> = emptyList()
    private var next = 0
    private var timer = 0f

    /** 已经出完全部文字（循环模式下指正在歇息）。 */
    val finished: Boolean get() = next >= glyphs.size

    /** 开始播放。每一轮都重新调用 [layout]，所以随机摆放每轮位置不同。 */
    fun play(layout: () -> List<PlacedGlyph>) {
        this.layout = layout
        restart()
    }

    fun step(dt: Float) {
        if (glyphs.isEmpty()) return
        timer += dt
        if (!finished) {
            while (timer >= interval && !finished) {
                timer -= interval
                emit(glyphs[next++])
            }
        } else if (loop && timer >= interval + REST) {
            restart()
        }
    }

    private fun restart() {
        glyphs = layout()
        next = 0
        timer = interval // 第一个字立刻出现
    }

    private fun emit(glyph: PlacedGlyph) {
        val maxRadius = glyph.cell * RADIUS_CELLS
        val reach = glyph.cell * SPREAD_CELLS
        for (i in 0 until glyph.count) {
            val x = glyph.xs[i]
            val y = glyph.ys[i]
            val distance = hypot(x - glyph.centerX, y - glyph.centerY)
            field.spawn(
                x, y, maxRadius,
                color = colors[random.nextInt(colors.size)],
                delay = distance / reach * SPREAD_SECONDS,
            )
        }
    }

    private companion object {
        /** 圆环最大半径，以格子边长为单位；略大于 1 让相邻圆环相互交叠。 */
        const val RADIUS_CELLS = 1.1f
        const val SPREAD_CELLS = 17f
        const val SPREAD_SECONDS = 0.35f
        const val REST = RippleField.DEFAULT_LIFETIME
    }
}
