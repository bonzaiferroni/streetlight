package koala.html

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
            val screen = screens.firstOrNull { it.pathRoot == root }
            return screen?.provideRoute(segments)
        }
    }
}

interface AppScreen {
    val pathRoot: String
    val provideRoute: (List<String>) -> AppRoute?
    val parameter: ScreenParameter? // could make this a collection to handle more complex paths
}

private fun String.dropStart(char: Char) = if (startsWith(char)) drop(1) else this

enum class ScreenParameter(val label: String) {
    Id("id"),
    Slug("slug")
}