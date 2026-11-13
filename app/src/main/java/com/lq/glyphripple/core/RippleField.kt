package com.lq.glyphripple.core

/**
 * 一组正在扩散的圆环。
 *
 * 用并列数组存储，推进和绘制时都不分配对象。圆环按真实时间推进，
 * 所以动画快慢与帧率无关。
 */
class RippleField(val capacity: Int = 8000) {

    private val xs = FloatArray(capacity)
    private val ys = FloatArray(capacity)
    private val ages = FloatArray(capacity)
    private val lifetimes = FloatArray(capacity)
    private val maxRadii = FloatArray(capacity)
    private val colors = IntArray(capacity)

    /** 当前圆环数量（包含尚在延迟中、还没开始显示的）。 */
    var size: Int = 0
        private set

    /**
     * 在 ([x], [y]) 放一个圆环。[delay] 秒后才开始扩散。
     * 容量已满时丢弃并返回 false。
     */
    fun spawn(
        x: Float,
        y: Float,
        maxRadius: Float,
        color: Int,
        delay: Float = 0f,
        lifetime: Float = DEFAULT_LIFETIME,
    ): Boolean {
        if (size >= capacity) return false
        val i = size++
        xs[i] = x
        ys[i] = y
        ages[i] = -delay
        lifetimes[i] = lifetime
        maxRadii[i] = maxRadius
        colors[i] = color
        return true
    }

    /** 推进 [dt] 秒，移除已经完全消散的圆环（保持先后顺序）。 */
    fun step(dt: Float) {
        var write = 0
        for (read in 0 until size) {
            val age = ages[read] + dt
            if (age >= lifetimes[read]) continue
            if (write != read) {
                xs[write] = xs[read]
                ys[write] = ys[read]
                lifetimes[write] = lifetimes[read]
                maxRadii[write] = maxRadii[read]
                colors[write] = colors[read]
            }
            ages[write] = age
            write++
        }
        size = write
    }

    fun clear() {
        size = 0
    }

    fun x(i: Int): Float = xs[i]
    fun y(i: Int): Float = ys[i]
    fun color(i: Int): Int = colors[i]

    /** 生命进度 0..1；仍在延迟中时为负数，此时不应绘制。 */
    fun progress(i: Int): Float = ages[i] / lifetimes[i]

    /** 半径：先快后慢地涨到最大半径。 */
    fun radius(i: Int): Float {
        val t = progress(i).coerceIn(0f, 1f)
        return maxRadii[i] * (1f - (1f - t) * (1f - t))
    }

    /** 不透明度 0..1：前四分之三缓慢变淡，最后四分之一迅速消失。 */
    fun alpha(i: Int): Float {
        val t = progress(i).coerceIn(0f, 1f)
        return if (t < FADE_KNEE) {
            1f - (1f - KNEE_ALPHA) * (t / FADE_KNEE)
        } else {
            KNEE_ALPHA * (1f - (t - FADE_KNEE) / (1f - FADE_KNEE))
        }
    }

    companion object {
        const val DEFAULT_LIFETIME = 2.4f
        private const val FADE_KNEE = 0.75f
        private const val KNEE_ALPHA = 0.55f
    }
}
