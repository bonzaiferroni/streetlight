package koala.html

import kotlin.uuid.Uuid

interface AppRoute {
    val screen: AppScreen
    fun toSitePath() = basePath
    val title: String
    val basePath get() = "/${screen.pathRoot}"

    companion object {
        fun routeOf(sitePath: String, screens: List<AppScreen>): AppRoute? {
            val fragment = sitePath.dropStart('/')
            val segments = fragment.split('/')
            val root = segments[0].lowercase()
            val screen = screens.firstOrNull { it.pathRoot == root } ?: return null

            return when (val parse = screen.routeParse) {
                is StaticParse -> parse.block()
                is UuidParse -> segments.getOrNull(1)?.let { parse.block(Uuid.parse(it)) }
                is IdParse -> segments.getOrNull(1)?.let { parse.block(it) }
                is IdOrNullParse -> parse.block(segments.getOrNull(1))
            }
        }
    }
}

interface AppScreen {
    val pathRoot: String
    val routeParse: RouteParse
}

private fun String.dropStart(char: Char) = if (startsWith(char)) drop(1) else this

sealed interface RouteParse

data class StaticParse(
    val block: () -> AppRoute
): RouteParse

data class UuidParse(
    val label: String = "id",
    val block: (Uuid) -> AppRoute
): RouteParse

data class IdParse(
    val label: String = "id",
    val block: (String) -> AppRoute
): RouteParse

data class IdOrNullParse(
    val label: String = "id",
    val block: (String?) -> AppRoute
): RouteParse