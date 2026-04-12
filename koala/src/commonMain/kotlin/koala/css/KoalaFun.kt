package koala.css

object KoalaFun {
    val ToggleTheme = Fun("toggleTheme")
}

data class Fun(val identifier: String) {
    override fun toString() = "$identifier()"

    val invocation get() = "$identifier()"
}

// language="JS"
val KoalaJs get() = """
function ${KoalaFun.ToggleTheme} {
    document.documentElement.classList.toggle(`${DayTheme.identifier}`);
}
"""