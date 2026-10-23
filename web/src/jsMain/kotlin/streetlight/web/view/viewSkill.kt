package streetlight.web.view

import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.routeBlock
import koala.dom.page.shellBox
import streetlight.model.data.SkillContent
import streetlight.model.ui.SkillRoute
import streetlight.web.shell.SkillShell
import streetlight.web.shell.skillShell

fun ViewScope.viewSkill(content: SkillContent) {
    shellBox {
        skillShell(content)
    }
}

fun RouteScope.viewSkillRoute() {
    routeBlock<SkillRoute, SkillContent>(SkillShell.IslandId) { content ->
        viewSkill(content)
    }
}
