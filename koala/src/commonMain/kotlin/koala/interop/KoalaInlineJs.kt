package koala.interop

import koala.css.DayTheme
import koala.css.jsScriptOf

//language="JS"
object KoalaInlineJs {
    val ScrollToId = JsSignature("scrollToId")
    val ToggleAncestor = JsSignature("toggleAncestor")
    val toggleRootModifier = JsSignature("toggleRootModifier")
    val toggleRootModifierWithTransition = JsSignature("toggleRootModifierWithTransition")

//    val toggleRootModifier = jsFunctionOf("""
//        function toggleRootModifier(mod) {
//            document.documentElement.classList.toggle(mod);
//            const isToggled = document.documentElement.classList.contains(mod);
//            localStorage.setItem(mod, isToggled ? 'true' : 'false');
//        }
//    """.trimIndent())
//
//    val toggleRootModifierWithTransition = jsFunctionOf("""
//        function toggleRootModifierWithTransition(mod) {
//            document.startViewTransition(() => {
//                ${toggleRootModifier.name}(mod);
//            })
//        }
//    """.trimIndent())

    val initRootModifier = jsFunctionOf("""
        function initRootModifier(mod) {
            if (localStorage.getItem(mod) === 'true') {
                document.documentElement.classList.add(mod);
            }
        } 
    """.trimIndent())
}

val KoalaHeadScript get() = with(KoalaInlineJs) {
    jsScriptOf {
        define(initRootModifier)
        invoke(initRootModifier, DayTheme)
    }
}
