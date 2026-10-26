package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import kampfire.model.MutableTap
import koala.SvgFile
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState

/**
 * A pill holding the search icon before a field bound to [state], showing [placeholder] while empty and a clear button
 * while it holds text. The field takes focus only when tapped, so the keyboard stays down until wanted. The cousin of
 * the web's `searchField`.
 */
@Composable
fun SearchField(state: MutableTap<String>, modifier: Modifier = Modifier, placeholder: String = "search") {
    val theme = currentTheme
    val text by state.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(percent = 50)
    val textStyle = theme.text.regular.copy(color = theme.colors.ink)
    val placeholderStyle = textStyle.copy(fontStyle = FontStyle.Italic)
    // the field is as wide as its text or its placeholder, and the cursor after it
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val fieldWidth = with(density) {
        val measured = when (text) {
            "" -> textMeasurer.measure(placeholder, placeholderStyle)
            else -> textMeasurer.measure(text, textStyle)
        }
        measured.size.width.toDp() + theme.line
    }

    Row(
        1,
        modifier.clip(shape)
            .background(theme.colors.zenBg)
            .border(theme.line, if (isFocused) theme.colors.primary else theme.colors.outlineMid, shape)
            .padding(horizontal = 2, vertical = 1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(SvgFile.Search.small, color = { theme.colors.ink })
        BasicTextField(
            value = text,
            onValueChange = { state.set(it) },
            modifier = Modifier.width(fieldWidth.coerceAtMost(theme.unit * MaxWidthUnits)),
            textStyle = textStyle,
            singleLine = true,
            cursorBrush = SolidColor(theme.colors.ink),
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            decorationBox = { field ->
                Box {
                    if (text.isEmpty()) {
                        BasicText(placeholder, Modifier.opacityHigh(), style = placeholderStyle, maxLines = 1, softWrap = false)
                    }
                    field()
                }
            },
        )
        if (text.isNotEmpty()) {
            Icon(
                SvgFile.X.small,
                color = { theme.colors.ink },
                modifier = Modifier.clickable { state.set("") }.pointerHoverIcon(PointerIcon.Hand),
            )
        }
    }
}

/** The widest the field grows in units, the web's `24ch`. */
private const val MaxWidthUnits = 24
