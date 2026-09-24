package koala.interop

import koala.modifier.DayTheme
import koala.modifier.jsScriptOf

//language="JS"
object KoalaFun {
    val ScrollToId = JsSignature("scrollToId")
    val ToggleAncestor = JsSignature("toggleAncestor")
    val toggleRootModifier = JsSignature("toggleRootModifier")
    val toggleRootModifierWithTransition = JsSignature("toggleRootModifierWithTransition")
    val applyRootAttribute = JsSignature("applyRootAttribute")
    val findAndInitTabs = JsSignature("findAndInitTabs")

    val initRootModifier = jsFunctionOf("""
        function initRootModifier(mod) {
            if (localStorage.getItem(mod) === 'true') {
                document.documentElement.classList.add(mod);
            }
        } 
    """.trimIndent())

    val initRootAttribute = jsFunctionOf("""
        function initRootAttribute(name, fallback) {
            const value = localStorage.getItem(name) ?? fallback;
            document.documentElement.setAttribute(name, value);
        }
    """.trimIndent())
}

val KoalaHeadScript get() = with(KoalaFun) {
    jsScriptOf {
        define(initRootModifier)
        define(initRootAttribute)
        invoke(initRootModifier, DayTheme)
    }
}
