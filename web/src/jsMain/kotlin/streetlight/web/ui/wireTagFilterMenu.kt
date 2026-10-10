package streetlight.web.ui

import koala.dom.*
import koala.modifier.*
import streetlight.model.data.EventTag
import streetlight.web.interop.filterFeedByTag
import web.dom.document

/**
 * The menu of tags a feed can be filtered by, read for the feed holding the button that opened it. A tag replaces
 * the feed with its events that carry it, and "All" clears it; the button of the tag in effect is set apart.
 */
fun ViewScope.wireTagFilterMenu() {
    popoverMenu(TagFilterMenu.PopoverId, { it }) { invoker ->
        fun filterBy(tag: EventTag?) {
            document.requireElement(TagFilterMenu.PopoverId).closePopover()
            filterFeedByTag(invoker, tag)
        }
        val selected = invoker.getAttribute(TagFilterMenu.Tag)
        // the selected button drops Zen for the default button style
        fun modOf(label: String?) = Zen.takeIf { label != selected }
        row(modify(FlexWrap, JustifyContentCenter, Padding(1))) {
            button("All", mod = modOf(null), onClick = { filterBy(null) })
            EventTag.entries.forEach { tag ->
                button(tag.label, mod = modOf(tag.label), onClick = { filterBy(tag) })
            }
        }
    }
}
