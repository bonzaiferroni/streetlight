package streetlight.web.model

import kampfire.model.MutableTap
import kampfire.model.Outcome
import kampfire.model.combine
import kampfire.model.storeOf
import kampfire.model.toDataOr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Skill
import streetlight.model.data.SkillId
import streetlight.model.io.ApiClient

/**
 * Links an edited record to the caller's skills through [skillIdsState], the edit's `skillIds`.
 *
 * It loads the caller's skills, and, through [readLinked], the skills the record is linked to already, which it writes
 * into the edit only while [skillIdsState] is still `null`.
 */
class SkillLinkEditor(
    private val skillIdsState: MutableTap<List<SkillId>?>,
    readLinked: (suspend () -> Outcome<List<SkillId>>)?,
    scope: CoroutineScope,
    api: ApiClient,
    toaster: Toaster,
) {
    private val skills = storeOf<List<Skill>?>(null)

    /** The caller's skills the record is linked to, and those it is not; `null` until they load. */
    val linkedSkillsState = skills.combine(skillIdsState) { all, ids -> all?.filter { it.skillId in ids.orEmpty() } }
    val unlinkedSkillsState = skills.combine(skillIdsState) { all, ids -> all?.filterNot { it.skillId in ids.orEmpty() } }

    init {
        scope.launch {
            skills.set(api.user.readSkills().toDataOr(toaster) { return@launch })
        }
        readLinked?.let { read ->
            scope.launch {
                val skillIds = read().toDataOr(toaster) { return@launch }
                // a choice made while loading wins over the stored links
                if (skillIdsState.now == null) skillIdsState.set(skillIds)
            }
        }
    }

    /** Links the record to [skillId], or unlinks it when it is linked. */
    fun toggleSkill(skillId: SkillId) {
        val skillIds = skillIdsState.now.orEmpty()
        skillIdsState.set(if (skillId in skillIds) skillIds - skillId else skillIds + skillId)
    }
}
