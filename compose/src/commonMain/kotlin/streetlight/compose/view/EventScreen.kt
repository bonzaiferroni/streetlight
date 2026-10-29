package streetlight.compose.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import kabinet.utils.toTimeFormat
import koala.SvgFile
import streetlight.compose.markdown.annotatedMarkdownOf
import streetlight.compose.model.EventRoute
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.cardBg
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.model.data.EntityDetail
import streetlight.model.data.EventLocation
import streetlight.model.data.costDetailOf

/** The screen of [route]: the logo as its top bar, and its event once inflated, shown in the [EventView]. */
@Composable
fun EventScreen(route: EventRoute) {
    val scrollState = rememberScrollState()
    TopBarMount(TopBar.Logo, scrollState)
    Inflated<EventLocation>(route) { EventView(it, scrollState) }
}

/**
 * The view of [EventRoute], showing [event] in a column scrolled by [scrollState] under the top bar: the header with
 * its time, cost and host, then its location and comments in tabs, and the footer. The cousin of the web's `eventShell`.
 */
@Composable
fun EventView(event: EventLocation, scrollState: ScrollState) {
    val theme = currentTheme
    PageThemeEffect(null)

    // the column is padded a unit all around below the top bar's room, as an item is
    Column(2, Modifier.fillMaxSize().verticalScroll(scrollState).padding(currentPageInsets).padding(1)) {
        EntityHeader(event, "at", event.headerDetails)
        Tabs {
            tab("Location") { EventLocationTab(event) }
            tab("Comments") {
                BasicText(
                    "coming soon",
                    Modifier.fillMaxWidth(),
                    style = theme.text.regular.copy(textAlign = TextAlign.Center),
                    color = { theme.colors.ink },
                )
            }
        }
        AppFooter(EventViewSource, Modifier.padding(top = 8))
    }
}

/** The details an event's header shows: the time it starts, its cost, and its host, as the web's `eventBody`. */
private val EventLocation.headerDetails get() = listOfNotNull(
    startsAt?.let { EntityDetail(it.toTimeFormat(), SvgFile.Clock.small) },
    cost?.let { costDetailOf(it, url) },
    EntityDetail(host.value, SvgFile.SomeoneSmall),
)

/** The location of [event]: the map at its point, its image, name and address, and its description. */
@Composable
private fun EventLocationTab(event: EventLocation) {
    val theme = currentTheme
    Column(0, Modifier.fillMaxWidth().borderRadius(2, outline = Outline.Mid).cardBg()) {
        MapMount(Modifier.fillMaxWidth().height(theme.unit * MapHeightUnits), center = event.geoPoint)
        Column(1, Modifier.fillMaxWidth().padding(2), horizontalAlignment = Alignment.CenterHorizontally) {
            event.locationImage?.let { ContainImage(it, Modifier.height(theme.unit * ImageHeightUnits).fillMaxWidth()) }
            BasicText(
                event.locationLabel,
                style = theme.text.heading2.copy(lineHeight = 1.em, textAlign = TextAlign.Center),
                color = { theme.colors.ink },
            )
            event.addressLine?.let {
                BasicText(
                    it,
                    Modifier.fillMaxWidth().filigree().opacityHigh(),
                    style = theme.text.heading4.copy(textAlign = TextAlign.Center),
                    color = { theme.colors.ink },
                )
            }
        }
        event.locationDescription?.let { description ->
            val text = remember(description, theme.colors.primaryFg) { annotatedMarkdownOf(description, theme.colors.primaryFg) }
            BasicText(text, Modifier.padding(4), style = theme.text.paragraph, color = { theme.colors.ink })
        }
    }
}

/** The height of the location's map in units, the web's `MinHeight(48)`. */
private const val MapHeightUnits = 48

/** The height of the location's image in units, the web's `MaxHeight(16)`. */
private const val ImageHeightUnits = 16

/** The path of this view's source, linked from its footer. */
private const val EventViewSource = "compose/src/commonMain/kotlin/streetlight/compose/view/EventScreen.kt"
