package koala.interop

//language="JS"
/** The global functions Koala defines, for markup to call. */
object KoalaFun {
    val ScrollToId = JsSignature("scrollToId")
    val toggleRootModifier = JsSignature("toggleRootModifier")
    val toggleRootModifierWithTransition = JsSignature("toggleRootModifierWithTransition")
    val applyRootSwitch = JsSignature("applyRootSwitch")
    val findAndInitTabs = JsSignature("findAndInitTabs")

    /** Adds a root class when `localStorage` holds it as on. Defined and invoked by [buildHeadScript]. */
    val initRootModifier = jsFunctionOf("""
        function initRootModifier(mod) {
            if (localStorage.getItem(mod) === 'true') {
                document.documentElement.classList.add(mod);
            }
        } 
    """.trimIndent())

    /** Sets the root's inline style from `localStorage` under [key]. Defined and invoked by [buildHeadScript]. */
    val initRootStyle = jsFunctionOf("""
        function initRootStyle(key) {
            const style = localStorage.getItem(key);
            if (style) document.documentElement.style.cssText = style;
        }
    """.trimIndent())

    /** Sets a root setting from `localStorage`, or to its fallback. Defined and invoked by [buildHeadScript]. */
    val initRootSwitch = jsFunctionOf("""
        function initRootSwitch(name, fallback) {
            const value = localStorage.getItem(name) ?? fallback;
            document.documentElement.setAttribute(name, value);
        }
    """.trimIndent())
}
