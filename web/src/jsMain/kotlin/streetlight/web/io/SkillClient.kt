package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.SkillContent
import streetlight.model.data.SkillId

/** The calls of `Api.Skills`. */
interface SkillClient {
    suspend fun readSkillContent(skillId: SkillId): Outcome<SkillContent>
}

class BrowserSkillClient(private val client: FetchClient): SkillClient {
    override suspend fun readSkillContent(skillId: SkillId) = client.getApi(Api.Skills.ReadContent, skillId)
}
