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
            textBlock("Hello ${user.username}! $intro", modify(Flex1))
            button("go home", onClick = { portal.go(HomeRoute()) })
            button("sign out", onClick = gate::signOut)
        }

        row {
            textBlock("What would you like to share?", modify(Flex1))
            button("add talent", modify(Accent), onClick = { portal.go(ShareTalentRoute) })
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

private const val intro = "Streetlight is a place to share, experience, and find a deeper level of human expression. " +
        "Share and grow your talents."