package koala.external

import kotlinx.js.JsPlainObject
import web.html.HTMLElement

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
    var rendererSettings: RendererSettings?
}

external interface LottieAnimation {
    fun play()
    fun stop()
    fun destroy()
}

@JsPlainObject
external interface RendererSettings {
    var preserveAspectRatio: String?    // "xMidYMid meet", "slice", etc
    var clearCanvas: Boolean?           // default true
    var progressiveLoad: Boolean?       // lazy-load shapes
    var hideOnTransparent: Boolean?     // hides if alpha = 0
    var className: String?              // applied to svg/canvas
    var id: String?                    // DOM id
}