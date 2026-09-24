package koala.html

import koala.Svg

/** An icon that either navigates or acts. */
sealed interface IconButton

/** An icon that navigates to [route]. */
data class IconRoute(
    val svg: Svg,
    val route: AppRoute
): IconButton

/** An icon that runs [action]. */
data class IconAction(
    val svg: Svg,
    val action: () -> Unit
): IconButton