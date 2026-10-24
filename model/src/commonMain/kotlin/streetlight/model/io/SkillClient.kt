package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.getApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.SkillContent
import streetlight.model.data.SkillId

/** The calls of `Api.Skills`. */
class SkillClient(private val transport: ApiTransport) {
    suspend fun readSkillContent(skillId: SkillId): Outcome<SkillContent> = transport.getApi(Api.Skills.ReadContent, skillId)
}
