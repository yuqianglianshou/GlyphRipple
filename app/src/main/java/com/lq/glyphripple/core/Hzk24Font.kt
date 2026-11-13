package com.lq.glyphripple.core

import java.nio.charset.Charset

/**
 * HZK24 点阵字库（24×24，每字 72 字节，逐行存储、高位在左）。
 *
 * 字库按 GB2312 区位码顺序排列，从 1 区 1 位开始，每区 94 位，
 * 覆盖 6763 个汉字和 1–9 区的全角符号；其余字符（ASCII、emoji 等）
 * 返回 null，交给别的来源兜底。
 */
class Hzk24Font(private val data: ByteArray) : GlyphSource {

    override fun glyph(grapheme: String, size: Int): GlyphGrid? {
        val bytes = try {
            grapheme.toByteArray(GB2312)
        } catch (e: Exception) {
            return null
        }
        if (bytes.size != 2) return null
        val area = (bytes[0].toInt() and 0xFF) - 0xA0
        val pos = (bytes[1].toInt() and 0xFF) - 0xA0
        if (area < FIRST_AREA || pos !in 1..94) return null
        val offset = ((area - FIRST_AREA) * 94 + (pos - 1)) * BYTES_PER_GLYPH
        if (offset + BYTES_PER_GLYPH > data.size) return null

        val cells = BooleanArray(SIZE * SIZE)
        for (row in 0 until SIZE) {
            for (col in 0 until SIZE) {
                val byte = data[offset + row * 3 + col / 8].toInt()
                cells[row * SIZE + col] = (byte shr (7 - col % 8)) and 1 == 1
            }
        }
        return GlyphGrid(SIZE, SIZE, cells)
    }

    companion object {
        const val SIZE = 24
        private const val BYTES_PER_GLYPH = SIZE * SIZE / 8
        private const val FIRST_AREA = 1
        private val GB2312: Charset = Charset.forName("GB2312")
    }
}
