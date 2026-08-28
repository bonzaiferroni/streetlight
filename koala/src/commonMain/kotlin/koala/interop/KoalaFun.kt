package koala.interop

import koala.css.DayTheme
import koala.css.jsScriptOf

//language="JS"
object KoalaFun {
    val ScrollToId = JsSignature("scrollToId")
    val ToggleAncestor = JsSignature("toggleAncestor")
    val toggleRootModifier = JsSignature("toggleRootModifier")
    val toggleRootModifierWithTransition = JsSignature("toggleRootModifierWithTransition")

    val initRootModifier = jsFunctionOf("""
        function initRootModifier(mod) {
            if (localStorage.getItem(mod) === 'true') {
                document.documentElement.classList.add(mod);
            }
        } 
    """.trimIndent())
}

val KoalaHeadScript get() = with(KoalaFun) {
    jsScriptOf {
        define(initRootModifier)
        invoke(initRootModifier, DayTheme)
    }
}
