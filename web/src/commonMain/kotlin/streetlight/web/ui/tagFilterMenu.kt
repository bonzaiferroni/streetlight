package streetlight.web.ui

import koala.SvgFile
import koala.html.Id
import koala.html.button
import koala.html.icon
import koala.modifier.*
import kotlinx.html.FlowContent

/** Opens the menu that filters a feed by tag, labeled with the tag in [TagFilterMenu.Tag] or a prompt without one. */
fun FlowContent.tagFilterMenu() {
    button(modify(TagFilterMenu.Button, AlignSelfCenter, DisplayFlex, AlignItemsCenter, Gap(1))) {
        setPopoverTarget(TagFilterMenu.PopoverId)
        icon(SvgFile.Label, SmallIconHeight)
    }
}

object TagFilterMenu {
    val PopoverId = Id("tag-filter-popover")
    val Button = Class("tag-filter-button")
    val Tag = stringAttributeOf("feed-tag")
}

// language="CSS"
val TagFilterMenuCss get() = with(TagFilterMenu) { """
$Button::after {
    content: "filter by tag";
    font-style: italic;
    opacity: var(--opacity-high);
}

$Button${Tag.selector}::after { content: attr(${Tag.identifier}); font-style: normal; opacity: 1; }
""" }
