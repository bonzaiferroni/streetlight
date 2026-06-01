package koala.css

object KoalaFun {
    val ToggleTheme = JsFun("toggleTheme")
    val ScrollToId = JsFun("scrollToId", idArg)

    val CallMenu = JsFun("callMenu")
    val ToggleAncestor = JsFun("toggleAncestor")
}

sealed interface KoalaArg

data object ThisElement: KoalaArg

private val idArg = "id"

// language="JS"
val KoalaJs get() = """
${KoalaFun.ToggleTheme} {
    document.documentElement.classList.toggle(`${DayTheme.identifier}`);
    const isDay = document.documentElement.classList.contains(`${DayTheme.identifier}`);
    localStorage.setItem('$THEME_KEY', isDay ? 'day' : 'night');
}

function initTheme() {
    if (localStorage.getItem('$THEME_KEY') === 'day') {
        document.documentElement.classList.add(`${DayTheme.identifier}`);
    }
}

initTheme();
"""

const val THEME_KEY = "streetlight.theme"