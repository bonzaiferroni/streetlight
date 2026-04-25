package koala.html

interface AppRoute {
    val screen: AppScreen
    fun toHashPath() = basePath
    val title: String
    val basePath get() = "#/${screen.pathRoot}"

    companion object {
        fun routeOf(hashPath: String, screens: List<AppScreen>): AppRoute? {
            val fragment = hashPath.dropStart('#').dropStart('/')
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
}

private fun String.dropStart(char: Char) = if (startsWith(char)) drop(1) else this