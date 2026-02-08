package koala.html

interface AppRoute {
    val screen: AppScreen<*>
    fun toHashPath() = "#/${screen.pathRoot}"

    companion object {
        fun <T: AppRoute> routeOf(hashPath: String, screens: List<AppScreen<T>>, provideDefault: () -> T): T {
            val fragment = hashPath.dropStart('#').dropStart('/')
            val segments = fragment.split('/')
            val root = segments[0].lowercase()
            val screen = screens.firstOrNull { it.pathRoot == root }
            return screen?.provideRoute(segments) ?: provideDefault()
        }
    }
}

interface AppScreen <T: AppRoute> {
    val pathRoot: String
    val provideRoute: (List<String>) -> T?
}

private fun String.dropStart(char: Char) = if (startsWith(char)) drop(1) else this