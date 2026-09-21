package streetlight.web.pages

import koala.LottieFile
import koala.SvgFile
import koala.modifier.*
import koala.html.navigation
import koala.html.column
import koala.html.filigree
import koala.html.icon
import koala.html.lottie
import koala.html.row
import koala.html.textBlock
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import streetlight.web.doc.SiteDoc
import streetlight.web.layouts.route

fun FlowContent.appFooter(sourcePath: String? = null) {
    column {
        configureAppFooter(sourcePath)
    }
}

fun DIV.configureAppFooter(sourcePath: String?, vararg additional: ExtraLink) {
    addModifiers(modify(JustifyContentCenter, AlignItemsCenter, Gap0, MarginBottom(16)))
    lottie(LottieFile.SpinningCircles, Height(24))
    textBlock(prayers.random(), modify(Italic, OpacityHigh))
    row(JustifyContentCenter) {
        navigation(SiteDoc.About.route) {
            textBlock("about us")
        }
        textBlock("•")
        navigation(SiteDoc.Privacy.route) {
            textBlock("your privacy")
        }
        textBlock("•")
        textBlock("feedback")
        textBlock("•")
        textBlock("report a bug")
    }
    sourcePath?.let {
        column(modify(Gap0, MarginTop(4), AlignItemsCenter)) {
            navigation(sourceUrlOf(sourcePath)) {
                column(Gap0) {
                    filigree {
                        icon(SvgFile.Github, Height(4))
                    }
                    textBlock("source code for this content")
                }
            }
            additional.takeIf { it.isNotEmpty() }?.let {
                it.forEach { link ->
                    navigation(sourceUrlOf(link.url.value)) {
                        textBlock("+ ${link.label}", OpacityHalf)
                    }
                }
            }
        }
    }
}

private fun sourceUrlOf(path: String) = "https://github.com/bonzaiferroni/streetlight/blob/main/$path"

val prayers = listOf(
    "May we build a world of faithful giants.",
)