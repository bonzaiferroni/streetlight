package koala.interop

import koala.modifier.Attribute
import koala.modifier.DayTheme
import koala.modifier.jsScriptOf

// a root setting restored in the head, with the value applied when nothing is stored
data class RootSwitch<T>(
    val attribute: Attribute<T>,
    val fallback: T,
) {
    val fallbackValue get() = attribute.toStringValue(fallback)
}

data class HeadScriptConfig(
    val rootSwitches: List<RootSwitch<*>> = emptyList(),
)

// runs in the head before the body renders, so stored settings are the first style shown
fun buildHeadScript(config: HeadScriptConfig) = with(KoalaFun) {
    jsScriptOf {
        define(initRootModifier)
        define(initRootSwitch)
        invoke(initRootModifier, DayTheme)
        config.rootSwitches.forEach {
            invoke(initRootSwitch, it.attribute.identifier, it.fallbackValue)
        }
    }
}
