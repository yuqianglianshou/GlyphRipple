package com.lq.glyphripple

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lq.glyphripple.ui.GlyphRippleApp

/** 点字成涟：把文字变成点阵，每个点是一圈扩散的涟漪。 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { GlyphRippleApp() }
    }
}
