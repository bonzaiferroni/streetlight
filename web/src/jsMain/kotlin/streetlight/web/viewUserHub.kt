package streetlight.web

import kampfire.model.UserInfo
import koala.css.*
import koala.dom.*
import koala.html.Id

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
                textBlock("Share and grow your talents.", modify(Flex1))
                button("add talent", modify(Accent), onClick = { portal.go(EditTalentRoute()) })
            }
        }

        card {
            row {
                textBlock("Host an event or post an event.")
                button("add event", modify(Accent), onClick = { portal.go()})
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

private const val intro = "Streetlight is a place to explore a deeper level of expression. "