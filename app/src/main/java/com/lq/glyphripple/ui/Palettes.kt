package com.lq.glyphripple.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

class Palette(val name: String, val background: Color, rings: List<Color>) {
    val rings: IntArray = IntArray(rings.size) { rings[it].toArgb() }
}

val Palettes = listOf(
    Palette(
        "霓虹", Color(0xFF0B0B14),
        listOf(Color(0xFFFF4D6D), Color(0xFFFFD166), Color(0xFF06D6A0), Color(0xFF4CC9F0), Color(0xFFC77DFF)),
    ),
    Palette(
        "深海", Color(0xFF04121F),
        listOf(Color(0xFF5BC0EB), Color(0xFF9BE7FF), Color(0xFF2EC4B6), Color(0xFFCBF3F0)),
    ),
    Palette(
        "余烬", Color(0xFF140A08),
        listOf(Color(0xFFFF6B35), Color(0xFFF7C59F), Color(0xFFFFD23F), Color(0xFFEE4266)),
    ),
    Palette(
        "水墨", Color(0xFFF4F1EA),
        listOf(Color(0xFF1A1A1A), Color(0xFF4A4A4A), Color(0xFF7A7A7A), Color(0xFF8C2F2F)),
    ),
    // 2015 年初版的配色：半透明蓝底上的六种纯色
    Palette(
        "初版", Color(0xFF9999FF),
        listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Cyan, Color.Magenta),
    ),
)
