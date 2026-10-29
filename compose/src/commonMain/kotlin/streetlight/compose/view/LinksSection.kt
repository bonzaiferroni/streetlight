package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kampfire.model.MutableTap
import kampfire.model.toAbsoluteUrlOrNull
import kampfire.model.storeOf
import koala.SvgFile
import koala.model.ExtraLink
import koala.model.toSvg
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState

/**
 * The links of [linksState] in an [ActionSectionCard], each edited or removed in its row, and new ones added, with
 * [instructions] while there are none. The cousin of the web's `linksFormSection`.
 */
@Composable
fun FlowRowScope.LinksSection(
    linksState: MutableTap<List<ExtraLink>>,
    instructions: String = "You may provide your original source, a youtube video, or any useful link.",
) = FormSection("additional links") {
    val theme = currentTheme
    val links by linksState.collectAsState()

    ActionSectionCard("Links", actions = {
        LinkAction("add new", ExtraLink.Empty) { linksState.set(linksState.now + it) }
    }) {
        links.forEachIndexed { index, link ->
            LinkRow(link, onEdit = { edited ->
                linksState.set(linksState.now.toMutableList().also { it[index] = edited })
            }, onRemove = {
                linksState.set(linksState.now.filterIndexed { i, _ -> i != index })
            })
        }
        if (links.isEmpty()) {
            BasicText(
                instructions,
                Modifier.fillMaxWidth().opacityHigh(),
                style = theme.text.small.copy(textAlign = TextAlign.Center, fontStyle = FontStyle.Italic),
                color = { theme.colors.ink },
            )
        }
    }
}

/** [link], its flair beside its label over its url, with the ways to [edit][onEdit] and [remove][onRemove] it. */
@Composable
private fun LinkRow(link: ExtraLink, onEdit: (ExtraLink) -> Unit, onRemove: () -> Unit) {
    val theme = currentTheme
    ActionRow(actions = {
        LinkAction("edit", link, onEdit)
        ActionButton("remove", onRemove)
    }) {
        Icon(link.toSvg().large, { theme.colors.ink }, size = theme.unit * 5)
        Column(0, Modifier.weight(1f)) {
            BasicText(
                link.label,
                style = theme.text.regular.copy(fontWeight = FontWeight.Bold),
                color = { theme.colors.ink },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                link.url.value,
                Modifier.opacityHigh(),
                style = theme.text.small,
                color = { theme.colors.ink },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** An [ActionButton] reading [label] that opens the form of [link], calling [onSave] with the link as written. */
@Composable
private fun LinkAction(label: String, link: ExtraLink, onSave: (ExtraLink) -> Unit) {
    var isOpen by remember { mutableStateOf(false) }
    Box {
        ActionButton(label, onClick = { isOpen = true })
        Popover(isOpen, onDismiss = { isOpen = false }) {
            LinkForm(link) {
                onSave(it)
                isOpen = false
            }
        }
    }
}

/** The label and url of [link], saved by [onSave] once they make a valid link. The cousin of the web's `linkPopover`. */
@Composable
private fun LinkForm(link: ExtraLink, onSave: (ExtraLink) -> Unit) {
    val theme = currentTheme
    val labelState = remember(link) { storeOf(link.label) }
    val urlState = remember(link) { storeOf(link.url.value) }
    val label by labelState.collectAsState()
    val url by urlState.collectAsState()
    // a url typed without its scheme takes https, as the web's
    val edited = url.toAbsoluteUrlOrNull()?.let { ExtraLink(label.trim(), it) }

    Column(1, Modifier.width(theme.unit * LinkFormUnits).padding(1), horizontalAlignment = Alignment.End) {
        TextField(labelState, "label", Modifier.fillMaxWidth(), icon = SvgFile.Label)
        TextField(urlState, "url", Modifier.fillMaxWidth(), icon = SvgFile.Link, keyboardType = KeyboardType.Uri)
        Button("Save", onClick = { edited?.let(onSave) }, isEnabled = edited?.isValid == true)
    }
}

/** The width of the link form in units. */
private const val LinkFormUnits = 48
