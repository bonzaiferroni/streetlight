package koala.dom

import kampfire.model.Labeled
import koala.modifier.*
import koala.html.AppRoute

/** An entry in a menu. */
sealed interface MenuOption: Labeled

/** A menu entry that only labels the entries after it. */
data class MenuLabel(override val label: String): MenuOption

/** A menu entry that navigates to [route]. */
data class MenuRoute(
    val route: AppRoute,
    override val label: String = route.label,
    val mod: Modifier? = null,
): MenuOption

/** A menu entry that runs [onClick]. */
data class MenuAction(
    override val label: String,
    val mod: Modifier? = null,
    val onClick: () -> Unit,
): MenuOption

/** A choice of [value] in a menu, shown as [label]. */
data class MenuValue<T>(
    val label: String,
    val value: T,
    val mod: Modifier? = null,
)