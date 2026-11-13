package com.lq.glyphripple.ui

import android.graphics.Paint
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.lq.glyphripple.core.FallbackGlyphSource
import com.lq.glyphripple.core.GlyphGrid
import com.lq.glyphripple.core.Graphemes
import com.lq.glyphripple.core.Hzk24Font
import com.lq.glyphripple.core.RippleField
import com.lq.glyphripple.core.TextLayout
import com.lq.glyphripple.core.TextPlayer
import com.lq.glyphripple.glyph.SystemFontGlyphSource
import kotlin.random.Random

private const val DEFAULT_TEXT = "问世间情为何物直教人生死相许"

/** 逐字随机摆放时两个字的间隔，以及整句排版时的间隔（秒）。 */
private const val SCATTER_INTERVAL = 0.9f
private const val SENTENCE_INTERVAL = 0.28f

/** 单帧最多推进这么久，避免从后台回来时动画跳变。 */
private const val MAX_FRAME_SECONDS = 0.05f

@Composable
fun GlyphRippleApp() {
    var settings by rememberSaveable(stateSaver = Settings.Saver) { mutableStateOf(Settings(DEFAULT_TEXT)) }
    var replay by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    val palette = Palettes[settings.palette]

    val field = remember { RippleField() }
    val player = remember { TextPlayer(field) }
    SideEffect {
        player.colors = palette.rings
        player.loop = settings.loop
    }

    val context = LocalContext.current
    val systemFont = remember { SystemFontGlyphSource() }
    val classicFont = remember {
        lazy {
            val hzk = Hzk24Font(context.assets.open("Hzk24s").use { it.readBytes() })
            FallbackGlyphSource(hzk, systemFont)
        }
    }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    // 文字、字形或画布变化时重新生成点阵并从头播放
    LaunchedEffect(settings.text, settings.sentence, settings.classic, settings.grid, canvasSize, replay) {
        if (canvasSize == IntSize.Zero) return@LaunchedEffect
        val source = if (settings.classic) classicFont.value else systemFont
        val rows = if (settings.classic) Hzk24Font.SIZE else settings.grid
        val grids = Graphemes.split(settings.text).map { grapheme ->
            source.glyph(grapheme, rows) ?: GlyphGrid.blank(rows / 2, rows)
        }
        val width = canvasSize.width.toFloat()
        val height = canvasSize.height.toFloat()
        field.clear()
        if (settings.sentence) {
            val maxCell = with(density) { 96.dp.toPx() } / rows
            val padding = with(density) { 24.dp.toPx() }
            player.interval = SENTENCE_INTERVAL
            player.play { TextLayout.sentence(grids, width, height, maxCell, padding) }
        } else {
            val glyphHeight = with(density) { 144.dp.toPx() }
            player.interval = SCATTER_INTERVAL
            player.play { TextLayout.scattered(grids, width, height, glyphHeight) }
        }
    }

    // 帧循环：只在前台运行，按真实时间推进
    var frame by remember { mutableLongStateOf(0L) }
    val speed by rememberUpdatedState(settings.speed)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var last = 0L
            while (true) {
                withFrameNanos { now ->
                    val dt = if (last == 0L) 0f else ((now - last) / 1e9f).coerceAtMost(MAX_FRAME_SECONDS)
                    last = now
                    player.step(dt * speed)
                    field.step(dt * speed)
                    frame = now
                }
            }
        }
    }

    // 状态栏图标的深浅跟随背景
    val activity = LocalActivity.current as? ComponentActivity
    LaunchedEffect(activity, palette) {
        val style = if (palette.background.luminance() > 0.5f) {
            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        } else {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        }
        activity?.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    val background by animateColorAsState(palette.background, tween(600), label = "background")
    val touchColors by rememberUpdatedState(palette.rings)
    val ringPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE } }

    Box(Modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    val minDistance = 8.dp.toPx()
                    val radius = 22.dp.toPx()
                    val lastSpawn = HashMap<PointerId, Offset>()
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            for (change in event.changes) {
                                if (!change.pressed) {
                                    lastSpawn.remove(change.id)
                                    continue
                                }
                                val last = lastSpawn[change.id]
                                if (last == null || (change.position - last).getDistance() > minDistance) {
                                    lastSpawn[change.id] = change.position
                                    field.spawn(
                                        change.position.x, change.position.y, radius,
                                        touchColors[Random.nextInt(touchColors.size)],
                                    )
                                }
                            }
                        }
                    }
                },
        ) {
            frame // 读取帧号，让每一帧都重绘
            drawRect(background)
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                for (i in 0 until field.size) {
                    if (field.progress(i) < 0f) continue
                    val r = field.radius(i)
                    ringPaint.color = field.color(i)
                    ringPaint.alpha = (field.alpha(i) * 255).toInt()
                    ringPaint.strokeWidth = r / 2.5f
                    native.drawCircle(field.x(i), field.y(i), r, ringPaint)
                }
            }
        }

        MaterialTheme(colorScheme = darkColorScheme()) {
            SmallFloatingActionButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp),
                containerColor = Color(0x66202028),
                contentColor = Color.White,
            ) {
                Text("文")
            }

            if (showSettings) {
                SettingsSheet(
                    settings = settings,
                    onChange = { settings = it },
                    onPlay = { text ->
                        settings = settings.copy(text = text)
                        replay++
                        showSettings = false
                    },
                    onDismiss = { showSettings = false },
                )
            }
        }
    }
}
