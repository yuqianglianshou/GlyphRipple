package com.lq.glyphripple.core

import java.text.BreakIterator

object Graphemes {
    /** 把文本拆成用户眼中的一个个字符（emoji、组合字符算一个），最多 [limit] 个。 */
    fun split(text: String, limit: Int = 200): List<String> {
        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(text)
        val result = ArrayList<String>()
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE && result.size < limit) {
            result += text.substring(start, end)
            start = end
            end = iterator.next()
        }
        return result
    }
}
