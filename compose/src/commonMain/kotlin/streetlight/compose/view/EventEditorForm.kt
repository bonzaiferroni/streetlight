package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import koala.SvgFile
import streetlight.compose.model.EventEditor
import streetlight.compose.modifier.blockLabel
import streetlight.compose.modifier.opacityHigh
import streetlight.model.data.EventProperty
import streetlight.compose.utility.collectAsState

/** The form of an event's edit: its details and its links, each on its card. The web's `eventEditFormBody`. */
@Composable
context(editor: EventEditor)
fun EventEditorForm() {
    Column(4) {
        EventDetailsCard()
        EventLinksCard()
    }
}

/**
 * The event's title, its cost or that it is free, its times and date with steppers to nudge them, its image, and its
 * description. The cousin of the web's `eventDetailsForm` and `eventImageForm`.
 */
@Composable
context(editor: EventEditor)
private fun EventDetailsCard() = FormCard("Event Details") {
    val theme = currentTheme
    val isFree by editor.isFreeState.collectAsState()
    val validity by editor.validityState.collectAsState()

    ColumnRow {
        FormSection("Title") {
            TextField(
                editor.titleState,
                "title",
                icon = SvgFile.Point,
                capitalization = KeyboardCapitalization.Words,
                isValid = validity.isValid(EventProperty.Title),
            )
        }
        FormSection("Cost") {
            Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CheckBox(editor.isFreeState, "Free event")
                if (!isFree) {
                    TextField(
                        editor.costTextState,
                        "cost",
                        Modifier.width(theme.unit * CostFieldUnits),
                        icon = SvgFile.Ticket,
                        keyboardType = KeyboardType.Decimal,
                        isValid = validity.isValid(EventProperty.Cost),
                    )
                }
            }
        }
        FormSection("Time") {
            EventTimeFields()
            Column(0, Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                StepperNote("that's")
                DayStepper(editor.dateState)
                StepperNote("at")
                TimeStepper(editor.startTimeState, "some time")
            }
        }
        FormSection("Image") {
            context(editor.imageEditor) { ImageField() }
            FormBullets(
                "This image will appear in the feed and at the top of the event page.",
                "Suitable for all audiences.",
            )
        }
    }
    ColumnRow {
        FormSection("Description") { MarkdownEditor(editor.descriptionState, "description") }
    }
}

/** The fields of the start time, the end time, which not every event has, and the date, each under its label. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
context(editor: EventEditor)
private fun EventTimeFields() {
    val theme = currentTheme
    val validity by editor.validityState.collectAsState()
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.unit, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(theme.unit),
    ) {
        TimeField(editor.startTimeState, Modifier.blockLabel("start time", validity.isValid(EventProperty.StartTime)))
        TimeField(editor.endTimeState, Modifier.blockLabel("end time"), isOptional = true)
        DateField(editor.dateState, Modifier.blockLabel("date", validity.isValid(EventProperty.Date)))
    }
}

/** The event's website and its other links. The cousin of the web's `eventLinksForm`. */
@Composable
context(editor: EventEditor)
private fun EventLinksCard() = FormCard("Event Links") {
    ColumnRow {
        FormSection("Website") {
            TextField(editor.websiteState, "website", icon = SvgFile.World, keyboardType = KeyboardType.Uri)
            CenteredText("Is there more information about this event on the web?")
        }
        LinksSection(editor.linksState)
    }
}

/** [text] small and faint, between the steppers. */
@Composable
private fun StepperNote(text: String) {
    val theme = currentTheme
    BasicText(text, Modifier.opacityHigh(), style = theme.text.small, color = { theme.colors.ink })
}

/** The width of the cost's field in units, the web's `Width(12)`. */
private const val CostFieldUnits = 12
