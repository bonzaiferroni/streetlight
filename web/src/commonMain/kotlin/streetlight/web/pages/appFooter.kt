package streetlight.web.pages

import koala.LottieFile
import koala.SvgFile
import koala.modifier.*
import koala.component.navigation
import koala.layout.column
import koala.component.filigree
import koala.component.icon
import koala.component.lottie
import koala.layout.row
import koala.component.textBlock
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink
import streetlight.web.doc.SiteDoc
import streetlight.web.layouts.route

/** The footer of a page, with a link to the source at [sourcePath] when given. */
fun FlowContent.appFooter(sourcePath: String? = null) {
    column {
        configureAppFooter(sourcePath)
    }
}

/** Renders an [appFooter] into this div, listing the [additional] source links beneath the main one. */
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
                        icon(SvgFile.Github.large, Height(4))
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

/** The lines the footer picks from at random. */
val prayers = listOf(
    "May we build a world of faithful giants.",
)