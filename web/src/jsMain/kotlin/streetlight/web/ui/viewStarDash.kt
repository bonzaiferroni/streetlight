package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.btn
import streetlight.model.data.Star
import streetlight.web.EditStarRoute
import streetlight.web.EditTalentRoute
import streetlight.web.HomeRoute
import streetlight.web.GalaxyListRoute
import streetlight.web.SandboxRoute
import streetlight.web.TalentProfileRoute
import streetlight.web.model.DataCache
import streetlight.web.model.UserGate

fun RenderScope.viewStarDash(star: Star) {
    // val model = UserHub(renderScope, app.client.api)
    val userCache = app.get<DataCache>()
    val gate = app.get<UserGate>()

    column {
        row {
            textBlock("Hello ${star.username}!", modify(Flex1))
            button("go home", onClick = { portal.go(HomeRoute) })
            button("sign out", onClick = gate::signOut)
        }

        card {
            row {
                textBlock("Add things to the map.", modify(Flex1))
                btn("Edit Profile", EditStarRoute)
                btn("Go to sandbox", SandboxRoute)
                button("galaxy list", modify(Accent), onClick = { portal.go(GalaxyListRoute) })
            }
        }

        card {
            row {
                textBlock("Share and grow your talents.", modify(Flex1))
                button("add talent", modify(Accent), onClick = { portal.go(EditTalentRoute()) })
            }
        }

        itemsBlock(userCache.talent.flow) { talent ->
            navigation(TalentProfileRoute(talent.talentId)) {
                card {
                    textBlock(talent.name)
                }
            }
        }
    }
}

