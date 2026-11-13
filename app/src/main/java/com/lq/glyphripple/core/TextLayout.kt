package com.lq.glyphripple.core

import kotlin.math.max
import kotlin.random.Random

/** 已经换算成画布坐标的一个字：每个墨点的圆心，以及字的中心和格子边长。 */
class PlacedGlyph(
    val xs: FloatArray,
    val ys: FloatArray,
    val centerX: Float,
    val centerY: Float,
    val cell: Float,
) {
    val count: Int get() = xs.size
}

object TextLayout {

    private const val GLYPH_GAP_CELLS = 2
    private const val LINE_GAP_CELLS = 4
    private const val MIN_CELL = 1.5f

    /**
     * 逐字随机摆放：每个字高 [glyphHeight] 像素，落在画布内的随机位置。
     * 空白字符被跳过。
     */
    fun scattered(
        grids: List<GlyphGrid>,
        width: Float,
        height: Float,
        glyphHeight: Float,
        random: Random = Random.Default,
    ): List<PlacedGlyph> = grids.filterNot { it.isEmpty }.map { grid ->
        var cell = glyphHeight / grid.height
        // 画布放不下时缩小到刚好放得下
        val fit = minOf(width / (grid.width * cell), height / (grid.height * cell))
        if (fit < 1f) cell *= fit
        val left = random.nextFloat() * max(0f, width - grid.width * cell)
        val top = random.nextFloat() * max(0f, height - grid.height * cell)
        place(grid, left, top, cell)
    }

    /**
     * 整句排版：从左到右、自动换行、整体居中。格子边长从 [maxCell] 开始，
     * 排不下就逐步缩小。空白字符只占位，不出现在结果里。
     */
    fun sentence(
        grids: List<GlyphGrid>,
        width: Float,
        height: Float,
        maxCell: Float,
        padding: Float = 0f,
    ): List<PlacedGlyph> {
        if (grids.isEmpty()) return emptyList()
        val availableWidth = width - 2 * padding
        val availableHeight = height - 2 * padding
        val rows = grids.maxOf { it.height }
        var cell = maxCell
        var lines = breakLines(grids, availableWidth / cell)
        while (cell > MIN_CELL && !fits(lines, rows, cell, availableWidth, availableHeight)) {
            cell = max(MIN_CELL, cell * 0.9f)
            lines = breakLines(grids, availableWidth / cell)
        }

        val lineHeight = (rows + LINE_GAP_CELLS) * cell
        val blockHeight = lines.size * lineHeight - LINE_GAP_CELLS * cell
        var top = (height - blockHeight) / 2f
        val placed = ArrayList<PlacedGlyph>(grids.size)
        for (line in lines) {
            var left = (width - lineWidthCells(line) * cell) / 2f
            for (grid in line) {
                if (!grid.isEmpty) placed += place(grid, left, top + (rows - grid.height) * cell, cell)
                left += (grid.width + GLYPH_GAP_CELLS) * cell
            }
            top += lineHeight
        }
        return placed
    }

    private fun fits(
        lines: List<List<GlyphGrid>>,
        rows: Int,
        cell: Float,
        availableWidth: Float,
        availableHeight: Float,
    ): Boolean {
        val blockHeight = (lines.size * (rows + LINE_GAP_CELLS) - LINE_GAP_CELLS) * cell
        return blockHeight <= availableHeight && lines.all { lineWidthCells(it) * cell <= availableWidth }
    }

    private fun lineWidthCells(line: List<GlyphGrid>): Int =
        line.sumOf { it.width } + GLYPH_GAP_CELLS * (line.size - 1)

    private fun breakLines(grids: List<GlyphGrid>, maxCells: Float): List<List<GlyphGrid>> {
        val lines = ArrayList<MutableList<GlyphGrid>>()
        var current = ArrayList<GlyphGrid>()
        var used = 0
        for (grid in grids) {
            val needed = if (current.isEmpty()) grid.width else used + GLYPH_GAP_CELLS + grid.width
            if (current.isNotEmpty() && needed > maxCells) {
                lines += current
                current = ArrayList()
                used = grid.width
            } else {
                used = needed
            }
            current += grid
        }
        if (current.isNotEmpty()) lines += current
        return lines
    }

    private fun place(grid: GlyphGrid, left: Float, top: Float, cell: Float): PlacedGlyph {
        val xs = FloatArray(grid.count)
        val ys = FloatArray(grid.count)
        var n = 0
        for (row in 0 until grid.height) {
            for (col in 0 until grid.width) {
                if (grid[col, row]) {
                    xs[n] = left + (col + 0.5f) * cell
                    ys[n] = top + (row + 0.5f) * cell
                    n++
                }
            }
        }
        return PlacedGlyph(
            xs, ys,
            centerX = left + grid.width * cell / 2f,
            centerY = top + grid.height * cell / 2f,
            cell = cell,
        )
    }
}
