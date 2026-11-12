package streetlight.web.dom.layout

import koala.SvgFile
import koala.component.heading5
import koala.dom.AppendScope
import koala.dom.component.div
import koala.dom.component.filigree
import koala.dom.component.row
import koala.dom.component.section
import koala.model.IconAction
import koala.modifier.*

/**
 * A section headed by [label] in a small heading, modified by [headingMod], centered in a filigree between a back
 * arrow that calls [onBack] and [icon], above its [content]. It heads content that opens with tabs, whose row divides
 * it from the heading, and leaves the hierarchy of headings to what the tabs hold.
 *
 * A [key], unique on the page and free of spaces, names the heading `heading-` and the key for view transitions.
 */
fun AppendScope.topSection(
    label: String,
    onBack: () -> Unit,
    headingMod: Modifier? = null,
    key: String? = null,
    icon: IconAction? = null,
    content: AppendScope.() -> Unit,
) = section {
    // each side is one icon wide, with or without an icon, so the heading stays centered between them
    row(modify(AlignItemsCenter, LargeIconHeight)) {
        iconActionButton(IconAction(SvgFile.ArrowLeft.large, "Back", onClick = onBack))
        filigree(Flex1) {
            heading5(label, modify(headingMod, key?.let { transition("heading-$it") }))
        }
        if (icon == null) div(modify(Height100Pct, Aspect1)) else iconActionButton(icon)
    }
    content()
}
