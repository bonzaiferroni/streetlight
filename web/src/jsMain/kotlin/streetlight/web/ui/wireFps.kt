package streetlight.web.ui

import koala.dom.ViewScope
import web.animations.requestAnimationFrame
import web.performance.performance

fun ViewScope.wireFps() {
    console.log("yer fps")
}
private var frameCount = 0
private var lastTick = performance.now()

fun startFpsCounter(onFps: (Int) -> Unit) {
    fun tick(now: Double) {
        frameCount++
        val elapsed = now - lastTick
        if (elapsed >= 1000.0) {
            onFps((frameCount * 1000.0 / elapsed).toInt())
            frameCount = 0
            lastTick = now
        }
        requestAnimationFrame(::tick)
    }
    requestAnimationFrame(::tick)
}