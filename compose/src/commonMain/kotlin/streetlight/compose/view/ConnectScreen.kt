package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kampfire.api.toMarkdown
import koala.SvgFile
import koala.SvgPack
import koala.Video
import koala.VideoFile
import streetlight.compose.markdown.annotatedMarkdownOf
import streetlight.compose.model.ConnectRoute
import streetlight.compose.model.HomeRoute
import streetlight.compose.model.currentPortal
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.primaryBg
import streetlight.compose.modifier.zenBg
import streetlight.model.data.OnboardType
import streetlight.model.ui.TopBar

/**
 * The screen of [route]: the logo as its top bar, the way to connect, and the pitch to the type chosen, first the
 * route's, then the one picked here. The cousin of the web's `viewConnect`, which changes its route as the type changes.
 */
@Composable
fun ConnectScreen(route: ConnectRoute) {
    val scrollState = rememberScrollState()
    TopBarMount(TopBar.Logo, scrollState)
    var type by rememberSaveable(route) { mutableStateOf(route.onboardType) }
    PageThemeEffect(null)

    Column(
        4,
        Modifier.fillMaxSize().verticalScroll(scrollState).padding(currentPageInsets).padding(1),
    ) {
        ConnectIntro { type = it }
        when (type) {
            OnboardType.BusinessOwner -> ConnectBusiness()
            else -> ComingSoon(type)
        }
        AppFooter(ConnectViewSource)
    }
}

/** The picker of the way to connect, calling [onPick], beside the map's video from the web's container width. */
@Composable
private fun ConnectIntro(onPick: (OnboardType) -> Unit) {
    val theme = currentTheme
    SideBySide(
        {
            Column(1, it, horizontalAlignment = Alignment.CenterHorizontally) {
                BasicText(
                    "Connect to your community.",
                    style = theme.text.heading4.copy(textAlign = TextAlign.Center),
                    color = { theme.colors.ink },
                )
                ConnectPicker(Modifier.fillMaxWidth(PickerShare), onPick)
                BasicText(
                    "Find out what Streetlight can offer.",
                    Modifier.padding(1).opacityHigh(),
                    style = theme.text.regular.copy(fontStyle = FontStyle.Italic),
                    color = { theme.colors.ink },
                )
            }
        },
        { VideoBox(VideoFile.MapZoomClick, it) },
    )
}

/** The pitch to a business: what Streetlight offers, the look and the page it makes, and the paths to take. */
@Composable
private fun ConnectBusiness() {
    val theme = currentTheme
    val colors = theme.colors
    Column(8, Modifier.fillMaxWidth()) {
        Column(1, Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            BasicText(
                "Put your events on the map",
                Modifier.fillMaxWidth().filigree(),
                style = theme.text.heading2.copy(textAlign = TextAlign.Center),
                color = { colors.ink },
            )
            Prose(BusinessIntro, Modifier.fillMaxWidth(IntroShare).padding(horizontal = 2), TextAlign.Center)
        }
        OfferCards()
        ShowcaseRow("Choose your look", ThemeInfo, VideoFile.ThemeShowcase)
        ShowcaseRow("Build your page", LayoutInfo, VideoFile.LayoutShowcase)
        PathCard()
    }
}

/** The four offers on cards, four in a row from the web's container width and two by two below it. */
@Composable
private fun OfferCards() {
    val theme = currentTheme
    val colors = theme.colors
    val cards: List<@Composable (Modifier) -> Unit> = listOf(
        { OfferCard(SvgFile.MapPinShare, "Get on the map", MapOffer, colors.systemFg, it) },
        { OfferCard(SvgFile.CalendarRepeat, "Automatically share events", CalendarOffer, colors.accentFg, it) },
        { OfferCard(SvgFile.Home, "Web hosting and more", HostingOffer, colors.primaryFg, it) },
        { OfferCard(SvgFile.Seedling, "Evolving platform", PlatformOffer, colors.validFg, it) },
    )
    val gap = Arrangement.spacedBy(theme.line)
    BoxWithConstraints(Modifier.fillMaxWidth().borderRadius(2, outline = Outline.Mid)) {
        val rows = if (maxWidth >= SideBySideWidth) listOf(cards) else cards.chunked(2)
        androidx.compose.foundation.layout.Column(verticalArrangement = gap) {
            rows.forEach { row ->
                // the cards of a row share the height of the tallest
                androidx.compose.foundation.layout.Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = gap) {
                    row.forEach { card -> card(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** An offer: the icon of [svg] on a badge in [color], [title], and [text]. The web's `iconCard`. */
@Composable
private fun OfferCard(svg: SvgPack, title: String, text: String, color: Color, modifier: Modifier) {
    val theme = currentTheme
    Column(2, modifier.fillMaxHeight().zenBg().padding(2), horizontalAlignment = Alignment.CenterHorizontally) {
        IconBadge(svg, color)
        BasicText(title, style = theme.text.heading6.copy(textAlign = TextAlign.Center), color = { theme.colors.ink })
        BasicText(
            text,
            Modifier.opacityHigh(),
            style = theme.text.regular.copy(textAlign = TextAlign.Center),
            color = { theme.colors.ink },
        )
    }
}

/** [title] over [info] beside the [video] that shows it, from the web's container width, and above it below that. */
@Composable
private fun ShowcaseRow(title: String, info: String, video: Video) {
    val theme = currentTheme
    SideBySide(
        {
            Column(1, it.padding(2)) {
                BasicText(
                    title,
                    Modifier.fillMaxWidth(),
                    style = theme.text.heading3.copy(textAlign = TextAlign.Center),
                    color = { theme.colors.ink },
                )
                Prose(info)
            }
        },
        { VideoBox(video, it) },
    )
}

/** The two paths to take, each its icon, its button and what it means, on the primary background. */
@Composable
private fun PathCard() {
    val theme = currentTheme
    val portal = currentPortal
    Column(
        2,
        Modifier.fillMaxWidth().borderRadius(2, moonShadow = true, outline = Outline.Mid).primaryBg().padding(2),
    ) {
        BasicText(
            "Choose your path",
            Modifier.fillMaxWidth().filigree(),
            style = theme.text.heading3.copy(textAlign = TextAlign.Center),
            color = { theme.colors.ink },
        )
        Row(2, Modifier.fillMaxWidth().padding(1)) {
            // onboarding waits for its route in the app
            PathOption(SvgFile.Trekking, "get started", ButtonStyle.Primary, SelfPath, Modifier.weight(1f)) {}
            PathOption(SvgFile.Code, "get support", ButtonStyle.Accent, SupportPath, Modifier.weight(1f)) {
                portal.dock(HomeRoute)
            }
        }
    }
}

/** A path: the icon of [svg], a button of [label] in [style] calling [onClick], and the [lines] that describe it. */
@Composable
private fun PathOption(
    svg: SvgPack,
    label: String,
    style: ButtonStyle,
    lines: List<String>,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val theme = currentTheme
    Column(1, modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedIcon(svg.animated, color = { theme.colors.ink }, size = theme.unit * PathIconUnits)
        Button(label, onClick = onClick, style = style)
        Column(1, Modifier.padding(top = 2)) {
            lines.forEach { Prose(it) }
        }
    }
}

/** A pitch not yet written for [type]. */
@Composable
private fun ComingSoon(type: OnboardType) {
    val theme = currentTheme
    BasicText(
        "${type.label}: content coming soon.",
        Modifier.fillMaxWidth().zenBg().padding(4),
        style = theme.text.regular,
        color = { theme.colors.ink },
    )
}

/** [first] beside [second], sharing the width, from the web's container width, and above it below that. */
@Composable
private fun SideBySide(first: @Composable (Modifier) -> Unit, second: @Composable (Modifier) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= SideBySideWidth) {
            Row(2, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                first(Modifier.weight(1f))
                second(Modifier.weight(1f))
            }
        } else {
            Column(2, Modifier.fillMaxWidth()) {
                first(Modifier.fillMaxWidth())
                second(Modifier.fillMaxWidth())
            }
        }
    }
}

/** The markdown [text] as a paragraph, aligned by [align]. */
@Composable
private fun Prose(text: String, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Start) {
    val theme = currentTheme
    val annotated = remember(text, theme.colors.primaryFg) { annotatedMarkdownOf(text.toMarkdown(), theme.colors.primaryFg) }
    BasicText(annotated, modifier, style = theme.text.paragraph.copy(textAlign = align), color = { theme.colors.ink })
}

/** The width from which two parts sit side by side: the web's `ContainerMdRow`. */
private val SideBySideWidth = 600.dp

/** The picker's share of its column's width, the web's `75%`, and the intro's of the page's, the web's `80%`. */
private const val PickerShare = 0.75f
private const val IntroShare = 0.8f

/** The width of a path's icon in units, the web's `Width(8)`. */
private const val PathIconUnits = 8

private const val ConnectViewSource = "compose/src/commonMain/kotlin/streetlight/compose/view/ConnectScreen.kt"

private const val BusinessIntro = "*Streetlight is built upon the idea that the fullest human experience begins shoulder to " +
    "shoulder, face to face, in a warm and friendly place. You've worked hard to create a place for people to gather, " +
    "and we've made it easy for people to discover.*"

private const val MapOffer = "Our interactive map shows everything that is happening in your neighborhood."
private const val CalendarOffer = "We will read an existing calendar page once a day and share your events."
private const val HostingOffer = "Point users to an existing website or build one with our simple tools."
private const val PlatformOffer =
    "We are just getting started, share your ideas and shape the direction of Streetlight."

private const val ThemeInfo =
    "Create a page on Streetlight with a customized look. Choose your colors and presentation within our theme editor."

private const val LayoutInfo = "Need a homepage for your business? Host your content with your own custom branding by " +
    "creating a free subdomain, *mybusiness.streetlight.ing*.\n\nYour content integrates seamlessly with Streetlight, " +
    "and visitors to your subdomain see only the content you create."

private val SelfPath = listOf(
    "Add your business to Streetlight.",
    "**Free now, free later.** We are committed to an open and free platform.",
    "Follow a guided tour that will get you started.",
)

private val SupportPath = listOf(
    "We can build it for you.",
    "**Scaled to meet your needs.** We'll work with you to find a solution that fits your business.",
    "We'll connect you to a web developer from your area.",
)
