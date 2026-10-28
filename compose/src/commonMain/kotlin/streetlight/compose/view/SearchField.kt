package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
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
import streetlight.compose.utility.mixSrgb
import kampfire.model.MutableTap
import koala.SvgFile
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState

/**
 * A pill five units tall holding the search icon before a field bound to [state], showing [placeholder] while empty and
 * unfocused, fading it out when focused, and a clear button while it holds text. It keeps the width of its placeholder
 * and the clear button until its text outgrows them. The field takes focus only when tapped, so the keyboard stays down
 * until wanted. A [backdrop] is drawn within the pill beneath its background, such as a blur of what lies behind it.
 * The cousin of the web's `searchField`.
 */
@Composable
fun SearchField(
    state: MutableTap<String>,
    modifier: Modifier = Modifier,
    placeholder: String = "search",
    backdrop: Modifier = Modifier,
) {
    val theme = currentTheme
    val text by state.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(percent = 50)
    val textStyle = theme.text.regular.copy(color = theme.colors.ink)
    val placeholderStyle = textStyle.copy(fontStyle = FontStyle.Italic)
    // the field is as wide as its placeholder or its text, whichever is wider, and the cursor after it
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val fieldWidth = with(density) {
        val placeholderWidth = textMeasurer.measure(placeholder, placeholderStyle).size.width
        val textWidth = textMeasurer.measure(text, textStyle).size.width
        maxOf(placeholderWidth, textWidth).toDp() + theme.line
    }
    // the focus outline fades in and out, read while drawing
    val focus = animateFloatAsState(if (isFocused) 1f else 0f, theme.magic.eased())
    val placeholderAlpha by animateFloatAsState(
        targetValue = if (text.isEmpty() && !isFocused) theme.opacity.high else 0f,
        animationSpec = theme.magic.eased(),
    )

    Row(
        1,
        modifier.clip(shape)
            .then(backdrop)
            .background(theme.colors.zenBg)
            .pillOutline(theme.line) { theme.colors.outlineMid.mixSrgb(theme.colors.primary, 1f - focus.value) }
            .height(theme.unit * HeightUnits)
            // the icon sits a unit from the start, as a text field's does
            .padding(start = 1, end = 2),
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
                // the placeholder sits on the field's baseline, as the two lay out their lines differently
                Layout(
                    content = {
                        BasicText(
                            placeholder,
                            Modifier.graphicsLayer { alpha = placeholderAlpha },
                            style = placeholderStyle,
                            maxLines = 1,
                            softWrap = false,
                        )
                        field()
                    },
                ) { measurables, constraints ->
                    val placeholderText = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0))
                    val input = measurables[1].measure(constraints)
                    val shift = input[FirstBaseline] - placeholderText[FirstBaseline]
                    layout(input.width, input.height) {
                        placeholderText.place(0, shift)
                        input.place(0, 0)
                    }
                }
            },
        )
        // the clear button keeps its place while hidden, so the pill holds its width as typing starts
        val hasText = text.isNotEmpty()
        Icon(
            SvgFile.X.small,
            color = { theme.colors.ink },
            modifier = Modifier.graphicsLayer { alpha = if (hasText) 1f else 0f }
                .clickable(enabled = hasText) { state.set("") }
                .then(if (hasText) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier),
        )
    }
}

/** The height of the pill in units, as every button and field is. */
private const val HeightUnits = 5

/** The widest the field grows in units, the web's `24ch`. */
private const val MaxWidthUnits = 24
