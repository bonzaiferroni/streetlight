package koala.dom

import kampfire.model.Labeled
import koala.css.ModifierSet
import koala.html.AppRoute

sealed interface MenuOption: Labeled

data class MenuLabel(override val label: String): MenuOption

data class MenuRoute(
    val route: AppRoute,
    override val label: String = route.label,
    val mod: ModifierSet? = null,
): MenuOption

data class MenuAction(
    override val label: String,
    val mod: ModifierSet? = null,
    val onClick: () -> Unit,
): MenuOption

data class MenuValue<T>(
    val label: String,
    val value: T,
    val mod: ModifierSet? = null,
)