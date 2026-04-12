package koala.css

object KoalaFun {
    val ToggleTheme = Fun("toggleTheme")
}

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