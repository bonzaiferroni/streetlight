package streetlight.compose.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import streetlight.compose.utility.mixSrgb
import kampfire.model.MutableTap
import koala.SvgPack
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState

/**
 * A pill five units tall holding the small [icon], when given, before a field bound to [state], across the width it is
 * given, showing [placeholder] while empty and fading it out when focused. The icon names what the field holds, as the
 * web's `blockLabel` does in words. A field that [isPassword] hides what it holds. [onSubmit] is called by the
 * keyboard's done action. The cousin of koala's `textField`, styled as the [SearchField].
 */
@Composable
fun TextField(
    state: MutableTap<String>,
    placeholder: String,
    modifier: Modifier = Modifier,
    icon: SvgPack? = null,
    isPassword: Boolean = false,
    onSubmit: (() -> Unit)? = null,
) {
    val theme = currentTheme
    val text by state.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(percent = 50)
    val textStyle = theme.text.regular.copy(color = theme.colors.ink)
    // the focus outline fades in and out, read while drawing
    val focus = animateFloatAsState(if (isFocused) 1f else 0f, theme.magic.eased())
    val placeholderAlpha by animateFloatAsState(
        targetValue = if (text.isEmpty() && !isFocused) theme.opacity.high else 0f,
        animationSpec = theme.magic.eased(),
    )

    BasicTextField(
        value = text,
        onValueChange = { state.set(it) },
        modifier = modifier.fillMaxWidth(),
        textStyle = textStyle,
        singleLine = true,
        cursorBrush = SolidColor(theme.colors.ink),
        interactionSource = interactionSource,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
            imeAction = if (onSubmit != null) ImeAction.Done else ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit?.invoke() }),
        decorationBox = { field ->
            Row(
                1,
                Modifier.clip(shape)
                    .background(theme.colors.zenBg)
                    .pillOutline(theme.line) { theme.colors.outlineMid.mixSrgb(theme.colors.primary, 1f - focus.value) }
                    .height(theme.unit * HeightUnits)
                    // an icon sits a unit from the start, closer than text, which keeps its two
                    .padding(start = if (icon != null) 1 else 2, end = 2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon?.let { Icon(it.small, color = { theme.colors.ink }) }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    BasicText(
                        placeholder,
                        Modifier.graphicsLayer { alpha = placeholderAlpha },
                        style = textStyle.copy(fontStyle = FontStyle.Italic),
                        maxLines = 1,
                        softWrap = false,
                    )
                    field()
                }
            }
        },
    )
}

/** Draws an outline of [width] in [color], read while drawing, inside the edge of a pill over its content. */
fun Modifier.pillOutline(width: Dp, color: () -> Color) = drawWithContent {
    drawContent()
    val stroke = width.toPx()
    drawRoundRect(
        color = color(),
        topLeft = Offset(stroke / 2, stroke / 2),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius((size.height - stroke) / 2),
        style = Stroke(stroke),
    )
}

/** The height of the pill in units, as every button and field is. */
private const val HeightUnits = 5
