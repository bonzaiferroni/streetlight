package koala.interop

//language="JS"
/** The global functions Koala defines, for markup to call. */
object KoalaFun {
    val ScrollToId = JsSignature("scrollToId")
    val ToggleAncestor = JsSignature("toggleAncestor")
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

    /** Sets a root setting from `localStorage`, or to its fallback. Defined and invoked by [buildHeadScript]. */
    val initRootSwitch = jsFunctionOf("""
        function initRootSwitch(name, fallback) {
            const value = localStorage.getItem(name) ?? fallback;
            document.documentElement.setAttribute(name, value);
        }
    """.trimIndent())
}
