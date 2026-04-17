package koala.html

import koala.css.Class

object ShellBoxKey {
    val Class = Class("shell-box")
}

val ShellBoxCss get() = """
.shell-box {
    width: 100%;
    height: 100%;
}
"""