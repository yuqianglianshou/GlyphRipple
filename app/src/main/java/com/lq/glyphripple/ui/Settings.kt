package com.lq.glyphripple.ui

import androidx.compose.runtime.saveable.listSaver

data class Settings(
    val text: String,
    /** true 为整句排版，false 为逐字随机摆放。 */
    val sentence: Boolean = false,
    /** true 使用 HZK24 经典点阵，false 使用系统字体。 */
    val classic: Boolean = false,
    /** [Palettes] 的下标。 */
    val palette: Int = 0,
    val speed: Float = 1f,
    /** 系统字体模式下每个字的点阵行数。 */
    val grid: Int = 24,
    val loop: Boolean = true,
) {
    companion object {
        const val MIN_GRID = 16
        const val MAX_GRID = 40
        const val MIN_SPEED = 0.5f
        const val MAX_SPEED = 2.5f

        val Saver = listSaver<Settings, Any>(
            save = { listOf(it.text, it.sentence, it.classic, it.palette, it.speed, it.grid, it.loop) },
            restore = {
                Settings(
                    it[0] as String, it[1] as Boolean, it[2] as Boolean, it[3] as Int,
                    it[4] as Float, it[5] as Int, it[6] as Boolean,
                )
            },
        )
    }
}
