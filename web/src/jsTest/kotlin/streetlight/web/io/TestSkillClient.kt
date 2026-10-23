package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.data.SkillContent
import streetlight.model.data.SkillId

class TestSkillClient: SkillClient {
    override suspend fun readSkillContent(skillId: SkillId): Outcome<SkillContent> = TODO()
}
