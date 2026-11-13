package com.lq.glyphripple.core

/** 一个字形的点阵：[width] 列 × [height] 行，true 表示该格有墨。 */
class GlyphGrid(val width: Int, val height: Int, private val cells: BooleanArray) {

    init {
        require(width >= 0 && height >= 0 && cells.size == width * height) {
            "点阵尺寸与数据不符: ${width}x$height, ${cells.size}"
        }
    }

    /** 有墨的格子数。 */
    val count: Int = cells.count { it }

    val isEmpty: Boolean get() = count == 0

    operator fun get(col: Int, row: Int): Boolean = cells[row * width + col]

    companion object {
        /** 空白字形，只占宽度（空格、换行等）。 */
        fun blank(width: Int, height: Int) = GlyphGrid(width, height, BooleanArray(width * height))
    }
}

/** 字形点阵的来源。 */
interface GlyphSource {
    /**
     * 返回 [grapheme]（一个用户可见字符）的点阵，[size] 为期望的行数。
     * 该来源不支持这个字符时返回 null。
     */
    fun glyph(grapheme: String, size: Int): GlyphGrid?
}

/** 依次尝试多个来源，取第一个能给出点阵的。 */
class FallbackGlyphSource(private vararg val sources: GlyphSource) : GlyphSource {
    override fun glyph(grapheme: String, size: Int): GlyphGrid? {
        for (source in sources) source.glyph(grapheme, size)?.let { return it }
        return null
    }
}
