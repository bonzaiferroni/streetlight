package streetlight.web.model

import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.utility.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Skill
import streetlight.model.data.SkillEdit
import streetlight.model.data.toEdit
import streetlight.web.io.ApiClient

/**
 * The star's skills, and the [SkillConfigState.focus] the panel shows in place of them: a [SkillEdit] for a
 * [SkillEditor], `null` while none is staged.
 */
class SkillConfig(
    scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(SkillConfigState())

    val focusState = state.tapOf { it.focus }
    val skillsState = state.tapOf { it.skills }

    init {
        scope.launch("read skills") {
            val skills = api.user.readSkills().toDataOr(toaster) { return@launch }
            state.set { copy(skills = skills) }
        }
    }

    fun startNewSkill() = state.set { copy(focus = SkillEdit()) }

    fun startSkillEdit(skill: Skill) = state.set { copy(focus = skill.toEdit()) }

    fun cancelFocus() = state.set { copy(focus = null) }

    /** Puts [skill] in the list, in place of the one it updates or last when it is new, and ends the edit. */
    fun finishSkillEdit(skill: Skill) = state.set {
        val skills = skills ?: emptyList()
        val isUpdate = skills.any { it.skillId == skill.skillId }
        copy(
            skills = if (isUpdate) skills.map { if (it.skillId == skill.skillId) skill else it }
                else skills + skill,
            focus = null,
        )
    }
}

data class SkillConfigState(
    val focus: SkillEdit? = null,
    val skills: List<Skill>? = null,
)
