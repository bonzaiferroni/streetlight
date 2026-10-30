package streetlight.compose.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import koala.SvgPack
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick
import kotlin.math.sign

/** Builds the tabs of a [Tabs], each with [tab]. */
class TabScope {
    internal val tabs = mutableListOf<Tab>()

    /** Adds a tab labeled [label] after an optional [icon], showing [content] while it is chosen. */
    fun tab(label: String, icon: SvgPack? = null, content: @Composable () -> Unit) {
        tabs.add(Tab(label, icon, content))
    }
}

/** A tab of a [Tabs]: its button's [label] and [icon], and the [content] it shows. */
class Tab(val label: String, val icon: SvgPack?, val content: @Composable () -> Unit)

/**
 * Tabs built by [content] in a [TabScope], a row of their buttons across the container's width over the chosen tab's
 * content, the [initialTab] at first. A newly chosen tab slides in a short way from the side its button lies on as the
 * other slides out the other way, and the content's height eases between them. The cousin of koala's `tabs`.
 */
@Composable
fun Tabs(modifier: Modifier = Modifier, initialTab: Int = 0, content: TabScope.() -> Unit) {
    val tabs = TabScope().apply(content).tabs
    var chosen by rememberSaveable { mutableIntStateOf(initialTab) }
    val theme = currentTheme
    val magic = theme.magic
    val slide = with(LocalDensity.current) { SlideDistance.roundToPx() }

    Column(1, modifier.fillMaxWidth()) {
        FlowRow(
            Modifier.fillMaxWidth()
                .clip(tabShape(theme.curve))
                .background(theme.colors.voidBg)
                .border(theme.line, theme.colors.outlineMid, tabShape(theme.curve)),
            horizontalArrangement = Arrangement.spacedBy(theme.unit),
        ) {
            tabs.forEachIndexed { index, tab ->
                TabButton(tab, isChosen = index == chosen, Modifier.weight(1f).widthIn(min = MinButtonWidth)) { chosen = index }
            }
        }
        AnimatedContent(
            targetState = chosen.coerceIn(0, (tabs.size - 1).coerceAtLeast(0)),
            transitionSpec = {
                // a tab to the right enters from the right as the other leaves to the left, and the reverse
                val direction = (targetState - initialState).sign
                (slideInHorizontally(magic.eased()) { direction * slide } + fadeIn(magic.eased())) togetherWith
                    (slideOutHorizontally(magic.eased()) { -direction * slide } + fadeOut(magic.eased())) using
                    SizeTransform(clip = false) { _, _ -> magic.eased() }
            },
        ) { index ->
            tabs.getOrNull(index)?.content?.invoke()
        }
    }
}

/** The button of [tab], lit when hovered and lit more while [isChosen], choosing it with [onChoose]. */
@Composable
private fun TabButton(tab: Tab, isChosen: Boolean, modifier: Modifier, onChoose: () -> Unit) {
    val theme = currentTheme
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val schemeColor = LocalSchemeColor.current
    val background = when {
        isChosen -> theme.colors.primary.copy(alpha = ChosenAlpha)
        isHovered -> theme.colors.primary.copy(alpha = HoverAlpha)
        else -> theme.colors.primary.copy(alpha = 0f)
    }
    Row(
        modifier.clip(tabShape(theme.curve))
            .background(background)
            .hoverable(interactionSource)
            .then(if (isChosen) Modifier else Modifier.pointerClick(onChoose))
            .padding(horizontal = 2, vertical = 1),
        horizontalArrangement = Arrangement.spacedBy(theme.unit, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tab.icon?.let { Icon(it.small, color = { schemeColor }, modifier = Modifier.alpha(theme.opacity.high)) }
        BasicText(
            tab.label.uppercase(),
            style = theme.text.small.copy(textAlign = TextAlign.Center),
            color = { schemeColor },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The shape of the header and its buttons at [curve], the web's `24px 24px 12px 12px` at full curve. */
private fun tabShape(curve: Float) =
    RoundedCornerShape(topStart = 24.dp * curve, topEnd = 24.dp * curve, bottomEnd = 12.dp * curve, bottomStart = 12.dp * curve)

/** The narrowest a tab's button is before the header wraps, the web's `100px`. */
private val MinButtonWidth = 100.dp

/** The distance a tab slides as it comes and goes, the web's `20px`. */
private val SlideDistance = 20.dp

/** The light of a hovered button and of the chosen one, the web's `0.1` and `0.4` of the primary color. */
private const val HoverAlpha = 0.1f
private const val ChosenAlpha = 0.4f
