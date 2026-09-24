package koala.interop

//language="JS"
object KoalaFun {
    val ScrollToId = JsSignature("scrollToId")
    val ToggleAncestor = JsSignature("toggleAncestor")
    val toggleRootModifier = JsSignature("toggleRootModifier")
    val toggleRootModifierWithTransition = JsSignature("toggleRootModifierWithTransition")
    val applyRootSwitch = JsSignature("applyRootSwitch")
    val findAndInitTabs = JsSignature("findAndInitTabs")

    val initRootModifier = jsFunctionOf("""
        function initRootModifier(mod) {
            if (localStorage.getItem(mod) === 'true') {
                document.documentElement.classList.add(mod);
            }
        } 
    """.trimIndent())

    val initRootSwitch = jsFunctionOf("""
        function initRootSwitch(name, fallback) {
            const value = localStorage.getItem(name) ?? fallback;
            document.documentElement.setAttribute(name, value);
        }
    """.trimIndent())
}
