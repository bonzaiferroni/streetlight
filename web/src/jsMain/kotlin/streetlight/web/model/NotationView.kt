package streetlight.web.model

import kampfire.model.Labeled
import kotlinx.serialization.Serializable

/** The parts of a track's notation its editor shows: the text, the notes drawn from it, or both. */
@Serializable
enum class NotationView(override val label: String) : Labeled {
    TextOnly("Text only"),
    NotesOnly("Notes only"),
    Both("Text and notes"),
}
