package streetlight.compose.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import koala.SvgFile
import streetlight.compose.model.LocationEditor

/**
 * The form of a location's edit: its details, its image, and its links, each on its card. The cousin of the web's
 * `locationEditFormBody`.
 */
@Composable
context(editor: LocationEditor)
fun LocationEditForm() {
    Column(4) {
        LocationDetailsCard()
        LocationImageCard()
        LocationLinksCard()
    }
}

/**
 * The location's name, its address and city, and its description. The keyboard of a touch screen starts each word of
 * the name, address and city in capitals. The cousin of the web's `locationDetailsForm`.
 */
@Composable
context(editor: LocationEditor)
private fun LocationDetailsCard() = FormCard("Location Details") {
    ColumnRow {
        FormSection("Name") {
            TextField(editor.nameState, "title", icon = SvgFile.MapPin, capitalization = KeyboardCapitalization.Words)
        }
        FormSection("Address / City") {
            TextField(editor.addressState, "address", icon = SvgFile.Road, capitalization = KeyboardCapitalization.Words)
            TextField(editor.cityState, "city", icon = SvgFile.City, capitalization = KeyboardCapitalization.Words)
        }
    }
    ColumnRow {
        FormSection("Description") { MarkdownEditor(editor.descriptionState, "description") }
    }
}

/** The image at the top of the location's page. The cousin of the web's `locationImageForm`. */
@Composable
context(editor: LocationEditor)
private fun LocationImageCard() = FormCard(null) {
    ColumnRow {
        FormSection("Image") {
            context(editor.imageEditor) { ImageField() }
            FormBullets("This image will appear at the top of the location page.", "Suitable for all audiences.")
        }
    }
}

/**
 * The location's website and calendar, each on the keyboard a touch screen offers for a url. The cousin of the web's
 * `locationLinksForm`.
 */
@Composable
context(editor: LocationEditor)
private fun LocationLinksCard() = FormCard("Links") {
    ColumnRow {
        FormSection("Website") {
            TextField(editor.websiteState, "website", icon = SvgFile.World, keyboardType = KeyboardType.Uri)
            FieldNote("This location's home on the web.")
        }
        FormSection("Calendar") {
            TextField(editor.eventsUrlState, "calendar", icon = SvgFile.Calendar, keyboardType = KeyboardType.Uri)
            FieldNote("Does the location have an events page or calendar?")
        }
    }
}

/** A note on a field, [text] centered beneath it. */
@Composable
private fun FieldNote(text: String) {
    val theme = currentTheme
    BasicText(
        text,
        Modifier.fillMaxWidth(),
        style = theme.text.regular.copy(textAlign = TextAlign.Center),
        color = { theme.colors.ink },
    )
}
