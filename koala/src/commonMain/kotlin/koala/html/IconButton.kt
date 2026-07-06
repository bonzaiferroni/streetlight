package koala.html

import koala.Svg

sealed interface IconButton

data class IconRoute(
    val svg: Svg,
    val route: AppRoute
): IconButton

data class IconAction(
    val svg: Svg,
    val action: () -> Unit
): IconButton