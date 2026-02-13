package koala.external

import kotlinx.js.JsPlainObject
import org.w3c.dom.HTMLElement

external object lottie {
    fun loadAnimation(params: LottieOptions): LottieAnimation
}

@JsPlainObject
external interface LottieOptions {
    var container: HTMLElement
    var renderer: String
    var loop: Boolean
    var autoplay: Boolean
    var path: String
}

external interface LottieAnimation {
    fun play()
    fun stop()
    fun destroy()
}