package streetlight.web.model

import kampfire.api.toMarkdown
import kampfire.model.Messenger
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.model.Portal
import koala.utils.DropWhileBusy
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.CityEdit
import streetlight.model.ui.CityRoute
import streetlight.web.io.ApiClient

class CityEditor(
    edit: CityEdit,
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val portal: Portal,
) {
    private val sending = DropWhileBusy()
    private val state = storeOf(CityEditorState(edit))

    val editState = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val nameField = editState.mutableTapOf({ it.name ?: "" }) { copy(name = it) }
    val imageField = editState.mutableTapOf({ it.image }) { copy(image = it) }
    val descriptionField = editState.mutableTapOf({ it.description ?: "".toMarkdown() }) { copy(description = it) }
    val linksState = editState.mutableTapOf({ it.links ?: emptyList() }) { copy(links = it.takeIf { it.isNotEmpty() }) }
    val validityField = editState.tapOf { it.validity }

    val imageEditor = ImageEditor(imageField, api)

    fun submit(messenger: Messenger) {
        editState.now.validity.message?.let {
            messenger.deliver(it)
            return
        }
        sending.launch(scope, ::submit.name) {
            messenger.deliverSending()
            if (!imageEditor.finalizeImage(messenger)) return@launch
            val city = api.city.updateCity(editState.now).toDataOr(messenger, "City saved.") { return@launch }
            portal.go(CityRoute(city.slug))
        }
    }
}

data class CityEditorState(
    val edit: CityEdit,
)
