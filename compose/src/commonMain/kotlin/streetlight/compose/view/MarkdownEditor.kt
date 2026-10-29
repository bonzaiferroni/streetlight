package streetlight.compose.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.MutableTap
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.zenBg
import streetlight.compose.utility.collectAsState
import streetlight.compose.utility.mixSrgb
import streetlight.compose.markdown.MarkdownStyle
import streetlight.compose.markdown.MarkdownStyler

/**
 * An editor of the markdown in [state] that styles it as it is typed: the syntax in the primary's foreground, strong,
 * emphasized, struck and linked text as they read, inline code in green, and headings at their size in the theme's
 * font, the rest in a monospace font at the size of regular text. Each line takes its own height, so a heading's
 * letters keep their tails. The text is the markdown itself, so the caret, selection and undo are the
 * platform's own. It shows [placeholder] while empty, and is at least [minHeight] tall. The cousin of the web's
 * `markdownEditor`.
 */
@Composable
fun MarkdownEditor(
    state: MutableTap<Markdown>,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = currentTheme.unit * MinHeightUnits,
) {
    val theme = currentTheme
    val markdown by state.collectAsState()
    var value by remember { mutableStateOf(TextFieldValue(markdown.value)) }
    // a change from outside the editor replaces its text, keeping the caret where it still fits
    LaunchedEffect(markdown) {
        if (markdown.value != value.text) {
            value = TextFieldValue(markdown.value, TextRange(value.selection.end.coerceAtMost(markdown.value.length)))
        }
    }
    val styler = remember { MarkdownStyler() }
    val transformation = remember(styler, theme) { MarkdownTransformation(styler, theme) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    // the focus outline fades in and out, read while drawing
    val focus = animateFloatAsState(if (isFocused) 1f else 0f, theme.magic.eased())
    // each line takes the height of its own letters, as a heading's are taller than the rest
    val textStyle = theme.text.regular.copy(
        color = theme.colors.ink,
        fontFamily = FontFamily.Monospace,
        lineHeight = TextUnit.Unspecified,
    )

    BasicTextField(
        value = value,
        onValueChange = { changed ->
            value = changed
            if (changed.text != markdown.value) state.set(changed.text.toMarkdown())
        },
        modifier = modifier.fillMaxWidth().heightIn(min = minHeight),
        textStyle = textStyle,
        cursorBrush = SolidColor(theme.colors.ink),
        interactionSource = interactionSource,
        visualTransformation = transformation,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        decorationBox = { field ->
            Box(
                Modifier.fillMaxWidth()
                    .heightIn(min = minHeight)
                    .borderRadius(1)
                    .zenBg()
                    .drawWithContent {
                        drawContent()
                        val stroke = theme.line.toPx()
                        drawRoundRect(
                            color = theme.colors.outlineMid.mixSrgb(theme.colors.primary, 1f - focus.value),
                            topLeft = Offset(stroke / 2, stroke / 2),
                            size = Size(size.width - stroke, size.height - stroke),
                            cornerRadius = CornerRadius(theme.unit.toPx() - stroke / 2),
                            style = Stroke(stroke),
                        )
                    }
                    .padding(1),
            ) {
                if (value.text.isEmpty()) {
                    BasicText(
                        placeholder,
                        style = textStyle.copy(fontStyle = FontStyle.Italic),
                        color = { theme.colors.ink.copy(alpha = theme.opacity.half) },
                    )
                }
                Box(Modifier.fillMaxWidth(), propagateMinConstraints = true) { field() }
            }
        },
    )
}

/** The markdown as written, styled by its [styler] in the colors and type of [theme], each character where it stands. */
private class MarkdownTransformation(
    private val styler: MarkdownStyler,
    private val theme: KoalaAppTheme,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = AnnotatedString.Builder(text.text).apply {
            styler.stylesOf(text.text).forEach { range ->
                if (range.to <= range.from) return@forEach
                when (val style = range.style) {
                    is MarkdownStyle.Heading -> addStyle(headingStyleOf(style.level), range.from, range.to)
                    else -> spanStyleOf(style)?.let { addStyle(it, range.from, range.to) }
                }
            }
        }.toAnnotatedString()
        return TransformedText(styled, OffsetMapping.Identity)
    }

    /** The heading of [level] in the theme's font, at its size and weight, as the web's editor shows it. */
    private fun headingStyleOf(level: Int): SpanStyle {
        val text = theme.text
        val style = when (level) {
            1 -> text.heading1
            2 -> text.heading2
            3 -> text.heading3
            4 -> text.heading4
            5 -> text.heading5
            else -> text.paragraph
        }
        return style.toSpanStyle().copy(color = theme.colors.ink)
    }

    private fun spanStyleOf(style: MarkdownStyle): SpanStyle? {
        val colors = theme.colors
        return when (style) {
            MarkdownStyle.Extra -> SpanStyle(color = colors.primaryFg)
            MarkdownStyle.Strong -> SpanStyle(fontWeight = FontWeight.Bold)
            MarkdownStyle.Emphasis -> SpanStyle(fontStyle = FontStyle.Italic)
            MarkdownStyle.Strikethrough -> SpanStyle(textDecoration = TextDecoration.LineThrough)
            MarkdownStyle.InlineCode -> SpanStyle(color = colors.greenFg, background = colors.zenBg)
            MarkdownStyle.LinkText -> SpanStyle(color = colors.accentFg)
            MarkdownStyle.Url -> SpanStyle(color = colors.muted)
            MarkdownStyle.CodeBlock -> SpanStyle(background = colors.zenBg)
            MarkdownStyle.Text, is MarkdownStyle.Heading -> null
        }
    }
}

/** The least height of an editor in units, the web's `MinHeight(24)` of a description. */
private const val MinHeightUnits = 24
