package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import koala.SvgPack
import androidx.compose.ui.graphics.shadow.Shadow as DropShadow

/**
 * A button reading [text], with its [flair] before it when given, in [style], that calls [onClick] unless it is not
 * [isEnabled]. A [backdrop] is drawn within its shape beneath its background, such as a blur of what lies behind it.
 * The cousin of the web's `btn` and `button`, which compose needs as one: a button that navigates and one that acts are
 * both a click.
 */
@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    flair: SvgPack? = null,
    isEnabled: Boolean = true,
    backdrop: Modifier = Modifier,
) {
    val theme = currentTheme
    val colors = theme.colors
    val shape = RoundedCornerShape(theme.radius(RadiusUnits))
    val background = when {
        !isEnabled -> colors.grayBg
        else -> when (style) {
            ButtonStyle.Primary -> colors.primaryButton
            ButtonStyle.Accent -> colors.accentButton
            ButtonStyle.Secondary -> colors.grayBg
            ButtonStyle.Zen -> colors.zenBg
            ButtonStyle.Danger -> colors.dangerBg
        }
    }
    // the glow the web casts with its shadow, none for a quiet or disabled button
    val glow = when {
        !isEnabled -> null
        else -> when (style) {
            ButtonStyle.Primary -> colors.primaryButton
            ButtonStyle.Accent -> colors.accentButton
            ButtonStyle.Danger -> colors.red
            ButtonStyle.Secondary, ButtonStyle.Zen -> null
        }
    }
    val isQuiet = style == ButtonStyle.Zen
    val textColor = when {
        !isEnabled -> colors.inkDisabled
        isQuiet -> colors.ink
        else -> Color.White
    }
    val textStyle = theme.text.small.copy(
        fontWeight = FontWeight.Medium,
        lineHeight = 1.em / 0.9f,
        textAlign = TextAlign.Center,
        shadow = if (isQuiet || !isEnabled) null else Shadow(Color.Black.copy(alpha = 0.8f), Offset(0f, 1f), 2f),
    )

    // as wide as its content, at least the minimum, so the text centers in whatever room the flair leaves
    Row(
        modifier
            .defaultMinSize(minWidth = theme.unit * MinWidthUnits)
            .width(IntrinsicSize.Max)
            .height(theme.unit * HeightUnits)
            .then(
                if (glow == null) Modifier
                else Modifier
                    .dropShadow(shape, DropShadow(radius = theme.unit * GlowUnits, color = glow))
                    .dropShadow(shape, DropShadow(radius = 1.dp, color = Color.Black.copy(alpha = 0.2f), offset = DpOffset(1.dp, 1.dp)))
            )
            .clip(shape)
            .then(backdrop)
            .background(background)
            .then(if (isQuiet) Modifier.border(theme.line, colors.outlineMid, shape) else Modifier)
            .clickable(enabled = isEnabled, role = Role.Button, interactionSource = null, indication = null, onClick = onClick)
            .then(if (isEnabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
            .pillPadding(hasIcon = flair != null),
        horizontalArrangement = Arrangement.spacedBy(theme.unit),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        flair?.let { Icon(it.small, color = { textColor }, modifier = Modifier.alpha(FlairAlpha), size = theme.unit * FlairUnits) }
        BasicText(
            text.uppercase(),
            modifier = Modifier.weight(1f),
            style = textStyle,
            color = { textColor },
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** The looks of a [Button], each the web's button class of its name. */
enum class ButtonStyle {
    /** The web's plain `btn`: the primary color, glowing. */
    Primary,

    /** The accent color, glowing. */
    Accent,

    /** Gray, without a glow. */
    Secondary,

    /** The quiet background in the ink, outlined, without a glow. */
    Zen,

    /** The danger color, glowing. */
    Danger,
}

/** The corner radius in units, the web's `--btn-border-radius` of 1.25rem. */
private const val RadiusUnits = 2.5f

/** The narrowest a button is in units, the web's `min-width`. */
private const val MinWidthUnits = 16

/** The reach of a button's glow in units, the web's `.8rem`. */
private const val GlowUnits = 1.6f

/** The height of a button in units, as every button and field is: the web's padding around its 1rem line. */
private const val HeightUnits = 5

/** The height of a flair in units, the web's `SmallIconHeight`. */
private const val FlairUnits = 3

/** The opacity of a button's flair, the web's `.75`. */
private const val FlairAlpha = 0.75f
