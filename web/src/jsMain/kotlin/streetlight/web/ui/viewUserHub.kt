package streetlight.web.ui

import kampfire.model.UserInfo
import koala.css.*
import koala.dom.*
import streetlight.web.CreateLocationRoute
import streetlight.web.EditTalentRoute
import streetlight.web.HomeRoute
import streetlight.web.EventScoutRoute
import streetlight.web.GalaxyFoundryRoute
import streetlight.web.GalaxyListRoute
import streetlight.web.ScoutMapRoute
import streetlight.web.TalentProfileRoute
import streetlight.web.model.AppContext

fun RenderContext.viewUserHub(
    app: AppContext,
    user: UserInfo,
) {
    // val model = UserHub(renderScope, app.client.api)
    val userCache = app.userCache
    val portal = app.portal
    val gate = app.gate

    column {
        row {
            textBlock("Hello ${user.username}!", modify(Flex1))
            button("go home", onClick = { portal.go(HomeRoute()) })
            button("sign out", onClick = gate::signOut)
        }

        card {
            row {
                textBlock("Add things to the map.", modify(Flex1))
                button("post event", onClick = { portal.go(EventScoutRoute())})
                button("post location", onClick = { portal.go(CreateLocationRoute)})
                button("scout map", onClick = { portal.go(ScoutMapRoute) })
                button("galaxy foundry", modify(Accent), onClick = { portal.go(GalaxyFoundryRoute)} )
                button("galaxy list", modify(Accent), onClick = { portal.go(GalaxyListRoute) })
            }
        }

        card {
            row {
                textBlock("Share and grow your talents.", modify(Flex1))
                button("add talent", modify(Accent), onClick = { portal.go(EditTalentRoute()) })
            }
        }

        itemsBlock(userCache.talents.flow) { talent ->
            action(TalentProfileRoute(talent.talentId)) {
                card {
                    textBlock(talent.name)
                }
            }
        }
    }
}

