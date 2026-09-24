package streetlight.web.model

import web.device.devicePixelRatio
import web.navigator.navigator
import web.window.window

/** Describes the browser for a bug report: user agent, viewport, and pixel ratio. */
fun readDeviceAgent() = listOf(
    navigator.userAgent,
    "viewport=${window.innerWidth}x${window.innerHeight}",
    "dpr=$devicePixelRatio",
).joinToString(" | ")
