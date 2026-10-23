package streetlight.web.shell

import koala.page.*
import kotlinx.html.FlowContent
import streetlight.model.data.SkillContent
import streetlight.web.layout.feedSection
import streetlight.web.layout.mainBody
import streetlight.web.layout.pageHeader
import streetlight.web.model.toCells
import streetlight.web.model.toThemeColor

/** A skill's page: its header, then the feed of media linked to it. */
fun FlowContent.skillShell(content: SkillContent) {
    val skill = content.skill
    mainBody("skillShell.kt") {
        pageHeader(
            title = skill.name,
            descriptor = skill.username?.let { "a skill of $it" } ?: "a skill",
            image = skill.image,
            colorScheme = skill.toThemeColor(),
            description = skill.description,
            cells = skill.toCells(),
            links = skill.links,
        )
        feedSection(content.feed)
    }

    dataIsland(SkillShell.IslandId, content)
}

object SkillShell {
    val IslandId = Id("skill-shell__island")
}
