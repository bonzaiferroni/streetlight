package streetlight.web.ui

import koala.css.columnsOf
import koala.dom.*
import kotlinx.css.LinearDimension
import kotlinx.css.fr
import kotlinx.html.js.p

fun AppScope.viewSandbox() {
    column {
        grid(columnsOf(1.fr, LinearDimension.auto)) {
            val swap = swap {
                box {
                    textBlock("one")
                }
                box {
                    textBlock("two")
                    textBlock("three")
                }
            }
            button(onClick = swap::next) {
                +"swap"
            }
        }
        textBlock("content below")
    }
}


