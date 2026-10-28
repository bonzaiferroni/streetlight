package streetlight.compose.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import streetlight.compose.model.StarSession
import streetlight.compose.utility.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import koala.Svg
import koala.SvgFile
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.cardBg
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick

/**
 * The helm button at the top start of the page and the star button at its end, floating over the page in the logo's
 * colors, each opening its menu from its corner. The button's icon is the same element as the menu's, so it stays in
 * place as the menu grows around it. A click outside an open menu closes it. The cousin of the web's `helmBar`.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HelmBar(modifier: Modifier = Modifier) {
    var openMenu by remember { mutableStateOf<HelmBarMenu?>(null) }
    val close = { openMenu = null }

    SharedTransitionLayout(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            if (openMenu != null) {
                Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { close() } })
            }
            Row(Modifier.fillMaxWidth().padding(1)) {
                HelmButton(HelmBarMenu.Helm, isOpen = openMenu == HelmBarMenu.Helm) { openMenu = HelmBarMenu.Helm }
                Spacer(Modifier.weight(1f))
                HelmButton(HelmBarMenu.Star, isOpen = openMenu == HelmBarMenu.Star) { openMenu = HelmBarMenu.Star }
            }
            CornerMenu(openMenu == HelmBarMenu.Helm, isEnd = false, Modifier.align(Alignment.TopStart)) {
                HelmMenu(onClose = close) { HelmIcon(HelmBarMenu.Helm, isOpen = true, Modifier.pointerClick(close)) }
            }
            CornerMenu(openMenu == HelmBarMenu.Star, isEnd = true, Modifier.align(Alignment.TopEnd)) {
                StarMenu(onClose = close) { HelmIcon(HelmBarMenu.Star, isOpen = true, Modifier.pointerClick(close)) }
            }
        }
    }
}

/** A menu of the [HelmBar], shown from the icon of its button. */
private enum class HelmBarMenu(val closedSvg: Svg, val openSvg: Svg) {
    Helm(SvgFile.Helm, SvgFile.HelmAnimated),
    Star(SvgFile.Someone, SvgFile.Someone),
}

/** The button of [menu], its icon alone, shown while its menu is not [isOpen], calling [onClick]. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(stage: SharedTransitionScope)
private fun HelmButton(menu: HelmBarMenu, isOpen: Boolean, onClick: () -> Unit) {
    val magic = currentTheme.magic
    AnimatedVisibility(!isOpen, enter = fadeIn(magic.eased()), exit = fadeOut(magic.eased())) {
        HelmIcon(menu, isOpen = false, Modifier.pointerClick(onClick))
    }
}

/**
 * The icon of [menu], the one element shared by its button and its open menu: on the button in a color of the logo,
 * the helm in the accent and the star in the primary, and on the menu the menu's own icon in its own color, its tint
 * turning between them as it passes from one to the other. The star's is the signed-in star's thumbnail, when they
 * have one.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(stage: SharedTransitionScope, visibility: AnimatedVisibilityScope)
private fun HelmIcon(menu: HelmBarMenu, isOpen: Boolean, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val colors = theme.colors
    val openTint = if (menu == HelmBarMenu.Helm) colors.primaryFg else colors.ink.copy(alpha = theme.opacity.high)
    val closedTint = if (menu == HelmBarMenu.Helm) colors.accentFg else colors.primaryFg
    val ownTint = if (isOpen) openTint else closedTint
    val otherTint = if (isOpen) closedTint else openTint
    val tint = visibility.transition.animateColor { if (it == EnterExitState.Visible) ownTint else otherTint }
    val iconMod = with(stage) {
        modifier.sharedElement(rememberSharedContentState(menu), visibility)
    }
    val size = theme.unit * HelmIconUnits
    val star by koinInject<StarSession>().starState.collectAsState()
    val thumb = star?.image?.thumb?.takeIf { menu == HelmBarMenu.Star }

    when {
        thumb != null -> AsyncImage(
            model = thumb,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = iconMod.size(size).clip(CircleShape),
        )
        isOpen -> AnimatedIcon(menu.openSvg, color = { tint.value }, modifier = iconMod, size = size)
        else -> Icon(menu.closedSvg, color = { tint.value }, modifier = iconMod, size = size)
    }
}

/**
 * The card of a menu of the [HelmBar], flush in the corner at its start, or at its end when [isEnd], while [isOpen].
 * It grows from that corner with the magic bounce, fading in, and shrinks back into it as it closes, blurring the page
 * behind it, and rounds only the corner across from its own. Its outline runs only along the edges that face the page.
 */
@Composable
private fun CornerMenu(
    isOpen: Boolean,
    isEnd: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val theme = currentTheme
    val magic = theme.magic
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    AnimatedVisibility(isOpen, modifier, enter = fadeIn(magic.eased()), exit = fadeOut(magic.eased())) {
        val scale by transition.animateFloat(
            transitionSpec = { if (targetState == EnterExitState.Visible) magic.bounced() else magic.eased() },
        ) { if (it == EnterExitState.Visible) 1f else magic.initialScale }
        Box(
            Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(if (isEnd == isLtr) 1f else 0f, 0f)
                }
                .then(
                    when (isEnd) {
                        false -> Modifier.borderRadius(0, 0, MenuRadiusUnits, 0, moonShadow = true)
                        true -> Modifier.borderRadius(0, 0, 0, MenuRadiusUnits, moonShadow = true)
                    }
                )
                .hazeBlur(HazeInput.Sources(LocalPageHaze.current), MenuBlur)
                .cardBg()
                .drawWithContent {
                    drawContent()
                    drawInnerOutline(isFarRight = isEnd != isLtr, theme.line.toPx(), (theme.unit * MenuRadiusUnits).toPx(), theme.colors.outlineMid)
                }
                // the card takes its own clicks, so only a click outside it closes the menu
                .pointerInput(Unit) { detectTapGestures { } }
        ) {
            content()
        }
    }
}

/**
 * Draws an outline of [width] in [color] inside the edges of a card flush in its corner: the edge across from the
 * corner and the bottom, through the far corner of [radius], on the right when [isFarRight]. The edges that lie against
 * the window's edges run outside the card, where its clip cuts them.
 */
private fun DrawScope.drawInnerOutline(isFarRight: Boolean, width: Float, radius: Float, color: Color) {
    val inset = width / 2
    val corner = CornerRadius((radius - inset).coerceAtLeast(0f))
    val edge = when (isFarRight) {
        true -> RoundRect(-width * 2, -width * 2, size.width - inset, size.height - inset, bottomRightCornerRadius = corner)
        false -> RoundRect(inset, -width * 2, size.width + width * 2, size.height - inset, bottomLeftCornerRadius = corner)
    }
    drawPath(Path().apply { addRoundRect(edge) }, color, style = Stroke(width))
}

/** The radius of the far corner of a menu's card in units. */
private const val MenuRadiusUnits = 3

/** The size of a helm icon in units, the web's `HelmBar.IconMod`. */
const val HelmIconUnits = 6

/** The blur of the page behind an open menu, the web's `--stronger-blur` of 18px. */
private val MenuBlur = HazeBlurStyle { blurRadius(18.dp) }
