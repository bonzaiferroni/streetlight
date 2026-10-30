package streetlight.compose.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kampfire.model.Labeled
import kampfire.model.MutableTap
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.collectAsState
import androidx.compose.ui.graphics.shadow.Shadow as DropShadow

/**
 * A switch reading [label] on a pill that slides to the end and lights while [state] is `true`, toggling it on a
 * click. The cousin of koala's `switch`.
 */
@Composable
fun Switch(state: MutableTap<Boolean>, label: String, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val colors = theme.colors
    val isOn by state.collectAsState()
    val travel by animateDpAsState(if (isOn) theme.unit * TravelUnits else theme.unit * 0, theme.magic.eased())
    val pill by animateColorAsState(if (isOn) colors.primaryButton else colors.grayBg, theme.magic.eased())
    val glow by animateFloatAsState(if (isOn) 1f else 0f, theme.magic.eased())
    val shape = theme.pillShape

    // the hidden label sets the track's width, the pill's travel beyond it
    Box(
        modifier.height(theme.unit * PillUnits)
            .background(colors.primaryButton.copy(alpha = TrackAlpha), shape)
            .pointerClick { state.set(!isOn) },
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicText(
            label.uppercase(),
            Modifier.alpha(0f).padding(start = theme.unit * 2, end = theme.unit * (2 + TravelUnits)),
            style = pillTextStyle(),
        )
        Box(
            Modifier.matchParentSize()
                .padding(end = theme.unit * TravelUnits)
                .offset(x = travel)
                .dropShadow(shape, DropShadow(theme.unit * GlowUnits, colors.primaryButton.copy(alpha = glow)))
                .clip(shape)
                .background(pill),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(label.uppercase(), style = pillTextStyle(), color = { Color.White })
        }
    }
}

/**
 * A row of [options] by [label], each as wide as the widest, the one [state] holds lit on a pill that grows in. The
 * cousin of koala's `switchMenu`.
 */
@Composable
fun <T> SwitchMenu(
    state: MutableTap<T>,
    options: List<T>,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    val theme = currentTheme
    val colors = theme.colors
    val selected by state.collectAsState()
    val shape = RoundedCornerShape(theme.radius(RadiusUnits))

    Row(0, modifier.width(IntrinsicSize.Max).background(colors.grayBg, shape)) {
        options.forEach { option ->
            val isSelected = option == selected
            val light by animateFloatAsState(if (isSelected) 1f else 0f, theme.magic.eased())
            val scale by animateFloatAsState(
                if (isSelected) 1f else theme.magic.initialScale,
                if (isSelected) theme.magic.bounced() else theme.magic.eased(),
            )
            Box(
                Modifier.weight(1f).height(theme.unit * PillUnits).pointerClick { if (!isSelected) state.set(option) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.matchParentSize()
                        .scale(scale)
                        .dropShadow(shape, DropShadow(theme.unit * GlowUnits, colors.primaryButton.copy(alpha = light)))
                        .clip(shape)
                        .background(colors.primaryButton.copy(alpha = light))
                )
                BasicText(
                    label(option).uppercase(),
                    Modifier.fillMaxWidth().padding(horizontal = theme.unit * 2),
                    style = pillTextStyle(),
                    color = { Color.White.copy(alpha = if (isSelected) 1f else UnselectedAlpha) },
                )
            }
        }
    }
}

/** A [SwitchMenu] of every entry of the enum [E], by its label. */
@Composable
inline fun <reified E> SwitchMenu(state: MutableTap<E>, modifier: Modifier = Modifier) where E : Enum<E>, E : Labeled =
    SwitchMenu(state, enumValues<E>().toList(), { it.label }, modifier)

/** The text of a switch's pill, as a button's: small, medium, in capitals, shadowed. */
@Composable
private fun pillTextStyle(): TextStyle = currentTheme.text.small.copy(
    fontWeight = FontWeight.Medium,
    textAlign = TextAlign.Center,
    shadow = Shadow(Color.Black.copy(alpha = 0.8f), Offset(0f, 1f), 2f),
)

/** The pill's travel in units, the web's `1rem`. */
private const val TravelUnits = 2

/** The height of a pill in units, as every button and field is. */
private const val PillUnits = 5

/** The corner radius of a switch menu in units, a button's. */
private const val RadiusUnits = 2.5f

/** The reach of a lit pill's glow in units, a button's. */
private const val GlowUnits = 1.6f

/** The track's share of the primary button's color, the web's `25%`. */
private const val TrackAlpha = 0.25f

/** The opacity of an unselected option's text, the web's `.9`. */
private const val UnselectedAlpha = 0.9f
