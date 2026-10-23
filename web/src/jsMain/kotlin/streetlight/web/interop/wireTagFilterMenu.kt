package streetlight.web.interop

import kampfire.model.storeOf
import koala.dom.*
import koala.modifier.*
import streetlight.model.data.EventTag
import web.dom.document
import koala.dom.component.*
import koala.dom.layout.*
import streetlight.model.data.EventCategory
import streetlight.web.component.TagFilterMenu
import streetlight.web.dom.component.popoverMenu
import streetlight.web.utility.requireElement

/**
 * The menu of tags a feed can be filtered by, read for the feed holding the button that opened it. A tag replaces
 * the feed with its events that carry it, and "All" clears it; the button of the tag in effect is set apart.
 */
fun ViewScope.wireTagFilterMenu() {
    popoverMenu(TagFilterMenu.PopoverId, { it }) { invoker ->
        val selected = invoker.getAttribute(TagFilterMenu.Tag)
        // the selected button drops Zen for the default button style
        eventTagMenu(selected) { tag ->
            document.requireElement(TagFilterMenu.PopoverId).closePopover()
            filterFeedByTag(invoker, tag)
        }
    }
}

val categoryState = storeOf(EventCategory.General)

/** The categories in [sideTabs], each showing its tags with [selected] set apart, each calling [filterBy]. */
fun ViewScope.eventTagMenu(selected: String?, filterBy: (EventTag?) -> Unit) {
    fun modOf(label: String?) = Zen.takeIf { label != selected }
    sideTabs(categoryState, modify(MaxWidth(72), MaxHeight(48), CardBg)) { category ->
        row(modify(FlexWrap, JustifyContentCenter, Padding(1), ScaleIn)) {
            if (category == EventCategory.General) {
                button("All", mod = modOf(null), onClick = { filterBy(null) })
            }
            category.tags.forEach { tag ->
                button(tag.label, { filterBy(tag) }, modOf(tag.label), flair = tag.svg)
            }
        }
    }
}
