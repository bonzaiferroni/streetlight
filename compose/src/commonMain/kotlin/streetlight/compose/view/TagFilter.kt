package streetlight.compose.view

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import kampfire.model.MutableTap
import koala.SvgFile
import streetlight.compose.modifier.cardBg
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState
import streetlight.model.data.EventCategory
import streetlight.model.data.EventTag

/**
 * The tag that filters a feed, bound to [state]: a quiet button reading the tag with its flair, or "filter" with the
 * label flair without one, opening the [EventTagMenu] in a [Popover], then a control that clears the tag while one is
 * set. The cousin of the web's `tagFilterMenu`.
 */
@Composable
fun TagFilter(state: MutableTap<EventTag?>, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val tag by state.collectAsState()
    var isOpen by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(EventCategory.General) }

    Row(1, modifier, verticalAlignment = Alignment.CenterVertically) {
        Box {
            Button(tag?.label ?: "filter", onClick = { isOpen = true }, style = ButtonStyle.Zen, flair = tag?.svg ?: SvgFile.Label)
            Popover(isOpen, onDismiss = { isOpen = false }) {
                EventTagMenu(tag, category, onCategory = { category = it }) { chosen ->
                    state.set(chosen)
                    isOpen = false
                }
            }
        }
        if (tag != null) {
            Icon(
                SvgFile.X.small,
                color = { theme.colors.ink },
                modifier = Modifier.clickable { state.set(null) }.pointerHoverIcon(PointerIcon.Hand),
            )
        }
    }
}

/**
 * The event categories on a rail beside the tags of [category], each a button that calls [onSelect], the [selected] tag
 * in the primary style; the general category starts with All, which clears the tag. The cousin of the web's
 * `eventTagMenu`.
 */
@Composable
fun EventTagMenu(
    selected: EventTag?,
    category: EventCategory,
    onCategory: (EventCategory) -> Unit,
    onSelect: (EventTag?) -> Unit,
) {
    val theme = currentTheme
    fun styleOf(isSelected: Boolean) = if (isSelected) ButtonStyle.Primary else ButtonStyle.Zen

    Row(
        Modifier.widthIn(max = theme.unit * MenuMaxWidthUnits).heightIn(max = theme.unit * MenuMaxHeightUnits)
    ) {
        // as wide as its longest label, so each label's background spans the rail and leaves the rest to the tags
        Column(0, Modifier.width(IntrinsicSize.Max).verticalScroll(rememberScrollState())) {
            EventCategory.entries.forEach { option ->
                BasicText(
                    text = option.label.uppercase(),
                    modifier = Modifier.fillMaxWidth()
                        .then(if (option == category) Modifier.background(theme.colors.primaryBg) else Modifier.cardBg())
                        .clickable { onCategory(option) }
                        .pointerHoverIcon(PointerIcon.Hand)
                        .padding(horizontal = 2, vertical = 1),
                    style = theme.text.small,
                    color = { theme.colors.ink },
                )
            }
        }
        FlowRow(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(1),
            horizontalArrangement = Arrangement.spacedBy(theme.unit, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(theme.unit),
        ) {
            if (category == EventCategory.General) {
                Button("All", onClick = { onSelect(null) }, style = styleOf(selected == null))
            }
            category.tags.forEach { tag ->
                Button(tag.label, onClick = { onSelect(tag) }, style = styleOf(tag == selected), flair = tag.svg)
            }
        }
    }
}

/** The widest and tallest the menu grows in units, the web's `MaxWidth(72)` and `MaxHeight(48)`. */
private const val MenuMaxWidthUnits = 72
private const val MenuMaxHeightUnits = 48
