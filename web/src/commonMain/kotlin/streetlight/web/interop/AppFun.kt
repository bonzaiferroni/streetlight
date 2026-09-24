package streetlight.web.interop

import koala.interop.JsSignature

/** The app's functions callable from HTML event handlers, beyond those of its components. */
object AppFun {
    val UpdateMark = JsSignature("updateMark")
}