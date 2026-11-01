package koala.model

import kampfire.model.Labeled
import koala.Svg
import koala.SvgPack
import koala.modifier.*
import kotlinx.html.BUTTON

/** An entry in a menu. */
sealed interface MenuOption: Labeled {
    val svg: Svg? get() = null
}

/** A menu entry that only labels the entries after it. */
data class MenuLabel(override val label: String): MenuOption

/** A menu entry that navigates to the route of [labeledRoute]. */
data class MenuRoute(
    val labeledRoute: LabeledRoute,
    val mod: Modifier? = null,
): MenuOption {
    override val label get() = labeledRoute.label
}

/** A menu entry that runs [onClick]. */
data class MenuAction(
    override val label: String,
    override val svg: Svg? = null,
    val mod: Modifier? = null,
    val config: BUTTON.() -> Unit = { },
    val onClick: () -> Unit,
): MenuOption

/** A choice of [value] in a menu, shown as [label]. */
data class MenuValue<T>(
    val label: String,
    val value: T,
    val mod: Modifier? = null,
)