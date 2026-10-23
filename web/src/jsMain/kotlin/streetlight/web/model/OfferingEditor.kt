package streetlight.web.model

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import kampfire.model.Messenger
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOrNull
import streetlight.model.data.ImageTarget
import streetlight.model.data.Offering
import streetlight.model.data.OfferingEdit
import streetlight.model.data.OfferingTag
import streetlight.model.data.toCentsLabel
import streetlight.model.data.toCentsOrNull
import streetlight.model.data.toPercentLabel
import kotlinx.coroutines.CoroutineScope
import streetlight.web.io.ApiClient

/** Edits [edit], a new offering or one of the star's, and saves it. */
class OfferingEditor(
    edit: OfferingEdit,
    scope: CoroutineScope,
    private val api: ApiClient,
    toaster: Toaster,
) {
    private val state = storeOf(OfferingEditorState(
        edit = edit,
        priceText = edit.priceCents?.toCentsLabel() ?: "",
        platformCutText = edit.platformCut.toPercentLabel(),
    ))
    val stateNow get() = state.now

    val editState = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val titleState = editState.mutableTapOf({ it.title ?: "" }) { copy(title = it) }
    val descriptionState = editState.mutableTapOf({ it.description ?: "".toMarkdown() }) {
        copy(description = it.takeIf(Markdown::isNotBlank))
    }
    val imageState = editState.mutableTapOf({ it.image }) { copy(image = it) }
    val priceState = state.mutableTapOf({ it.priceText }) {
        copy(priceText = it, edit = this.edit.copy(priceCents = it.toCentsOrNull()))
    }
    val platformCutState = state.mutableTapOf({ it.platformCutText }) {
        copy(platformCutText = it, edit = this.edit.copy(platformCut = it.toPlatformCut()))
    }
    val validityState = editState.tapOf { it.validity }

    val imageEditor = ImageEditor(imageState, ImageTarget.Offering, api)
    val skillLinkEditor = SkillLinkEditor(
        skillIdsState = editState.mutableTapOf({ it.skillIds }) { copy(skillIds = it) },
        readLinked = edit.offeringId?.let { offeringId -> { api.offering.readSkillIds(offeringId) } },
        scope = scope,
        api = api,
        toaster = toaster,
    )

    /** Whether the edit carries [tag]. */
    fun tagState(tag: OfferingTag) = editState.mutableTapOf({ tag in it.tags }) { isTagged ->
        copy(tags = if (isTagged) tags + tag else tags - tag)
    }

    /** Saves the offering, creating it when it is new, and returns it, or `null` when invalid or failed. */
    suspend fun submitSuspend(messenger: Messenger): Offering? {
        editState.now.validity.message?.let {
            messenger.deliver(it)
            return null
        }
        messenger.deliverSending()
        if (!imageEditor.finalizeImage(messenger)) return null
        val edit = editState.now
        return when (edit.offeringId) {
            null -> api.offering.create(edit)
            else -> api.offering.update(edit)
        }.toDataOrNull(messenger)
    }
}

data class OfferingEditorState(
    val edit: OfferingEdit,
    val priceText: String,
    val platformCutText: String,
)

/** The cut in basis points from a percent, `0` when blank, or `-1`, out of range, when it is not a number. */
fun String.toPlatformCut() = when {
    isBlank() -> 0
    else -> toCentsOrNull() ?: -1
}
