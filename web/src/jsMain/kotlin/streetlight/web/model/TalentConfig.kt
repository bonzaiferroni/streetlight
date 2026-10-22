package streetlight.web.model

import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.utility.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Talent
import streetlight.model.data.TalentEdit
import streetlight.model.data.toEdit
import streetlight.web.io.ApiClient

/**
 * The star's talents, and the [TalentConfigState.focus] the panel shows in place of them: a [TalentEdit] for a
 * [TalentEditor], `null` while none is staged.
 */
class TalentConfig(
    scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(TalentConfigState())

    val focusState = state.tapOf { it.focus }
    val talentsState = state.tapOf { it.talents }

    init {
        scope.launch("read talents") {
            val talents = api.user.readTalents().toDataOr(toaster) { return@launch }
            state.set { copy(talents = talents) }
        }
    }

    fun startNewTalent() = state.set { copy(focus = TalentEdit()) }

    fun startTalentEdit(talent: Talent) = state.set { copy(focus = talent.toEdit()) }

    fun cancelFocus() = state.set { copy(focus = null) }

    /** Puts [talent] in the list, in place of the one it updates or last when it is new, and ends the edit. */
    fun finishTalentEdit(talent: Talent) = state.set {
        val talents = talents ?: emptyList()
        val isUpdate = talents.any { it.talentId == talent.talentId }
        copy(
            talents = if (isUpdate) talents.map { if (it.talentId == talent.talentId) talent else it }
                else talents + talent,
            focus = null,
        )
    }
}

data class TalentConfigState(
    val focus: TalentEdit? = null,
    val talents: List<Talent>? = null,
)
