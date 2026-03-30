package streetlight.web.pages

import koala.LottieFile
import koala.SvgFile
import koala.css.*
import koala.html.action
import koala.html.column
import koala.html.filigree
import koala.html.icon
import koala.html.lottie
import koala.html.row
import koala.html.textBlock
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.appFooter(sourcePath: String? = null) {
    column {
        configureAppFooter(sourcePath)
    }
}

fun DIV.configureAppFooter(sourcePath: String?) {
    val prayer = "May we build a world of faithful giants."
    addModifiers(modify(JustifyContentCenter, AlignItemsCenter, Gap0, MarginBottom16))
    lottie(LottieFile.spinningCircles, modify(Height24))
    textBlock(prayer, modify(Italic, OpacityMost))
    row(modify(JustifyContentCenter)) {
        textBlock("about us")
        textBlock("•")
        textBlock("feedback")
        textBlock("•")
        textBlock("report a bug")
    }
    sourcePath?.let {
        action(sourceUrlOf(sourcePath), modify(MarginTop2)) {
            column(modify(Gap0)) {
                filigree {
                    icon(SvgFile.Github, modify(Height4))
                }
                textBlock("source code for this content", modify(LineHeight1))
            }
        }
    }
}

private fun sourceUrlOf(path: String) = "https://github.com/bonzaiferroni/streetlight/blob/main/$path"