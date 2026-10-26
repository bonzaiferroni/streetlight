package streetlight.web.component

import koala.SvgFile
import koala.page.Id
import koala.component.button
import koala.component.icon
import koala.modifier.*
import koala.interop.JsSignature
import koala.interop.ThisElement
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.web.page.PopoverId

/**
 * Opens the menu that filters a feed by tag, labeled with the tag in [TagFilterMenu.Tag] or a prompt without one,
 * followed by a button that clears the tag, shown while one is set.
 */
fun FlowContent.tagFilterMenu() {
    button(modify(TagFilterMenu.Button, AlignSelfCenter, DisplayFlex, AlignItemsCenter, Gap(1))) {
        setPopoverTarget(TagFilterMenu.PopoverId)
        icon(SvgFile.Label.small, SmallIconHeight)
    }
    button(modify(TagFilterMenu.Clear, AlignSelfCenter)) {
        onClick = TagFilterMenu.ClearTag.invokeJs(ThisElement)
        icon(SvgFile.X.small, SmallIconHeight)
    }
}

object TagFilterMenu {
    val PopoverId = Id("tag-filter-popover")
    val Button = Class("tag-filter-button")
    val Clear = Class("tag-filter-clear")
    val Tag = stringAttributeOf("feed-tag")

    val ClearTag = JsSignature("clearFeedTag")
}

// language="CSS"
val TagFilterMenuCss get() = with(TagFilterMenu) { """
$Button::after {
    content: "filter by type";
    font-style: italic;
    opacity: var(--opacity-high);
}

$Button${Tag.selector}::after { content: attr(${Tag.identifier}); font-style: normal; opacity: 1; }

$Button:not(${Tag.selector}) + $Clear { display: none; }
""" }
