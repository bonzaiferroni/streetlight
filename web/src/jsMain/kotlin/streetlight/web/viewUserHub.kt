package streetlight.web

import kampfire.model.UserInfo
import koala.css.*
import koala.dom.*
import koala.html.Id

fun RenderContext.viewUserHub(
    app: AppContext,
    user: UserInfo,
) {
    val model = app.userHub
    val portal = app.portal
    val gate = app.gate

    column {
        row {
            textBlock("Hello ${user.username}!", modify(Flex1))
            button("go home", onClickEvent = { portal.go(HomeRoute()) })
            button("sign out", modify(Accent), onClickEvent = { gate.signOut() })
        }
        tabs(Id("user-hub-tabs")) {
            tab("Songs") {
                column(modify(AlignItemsCenter)) {
                    row(modify(Width64)) {
                        row(modify(Flex1, FlexItems1)) {
                            textField(
                                label = "title",
                                placeholder = "Song title",
                                binding = model.titleFlow,
                                onChangeValue = model::setSongTitle
                            )
                            textField(
                                label = "artist",
                                placeholder = "Artist",
                                binding = model.artistFlow,
                                onChangeValue = model::setArtist
                            )
                        }
                        button("Add new song", modify(Accent), onClick = model::addSong)
                    }
                    itemsBlock(model.songsFlow, modify(MagicBlur, MagicSlideX), true) { song ->
                        card {
                            textBlock(song.title)
                        }
                    }
                }
            }
        }
    }
}