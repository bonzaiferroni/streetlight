package koala.interop

import koala.modifier.Attribute
import koala.modifier.DayTheme
import koala.modifier.jsScriptOf

/** A root setting restored in the head, with the [fallback] applied when nothing is stored. */
data class RootSwitch<T>(
    val attribute: Attribute<T>,
    val fallback: T,
) {
    val fallbackValue get() = attribute.toStringValue(fallback)
}

/** The settings an app restores in the head of every page. */
data class HeadScriptConfig(
    val rootSwitches: List<RootSwitch<*>> = emptyList(),
)

/**
 * The head script for [config], which runs before the body renders so the stored settings are the first style
 * shown.
 *
 * It restores the day theme and each of the config's root switches.
 */
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
