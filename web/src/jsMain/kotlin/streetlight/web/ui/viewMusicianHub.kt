package streetlight.web.ui

import kampfire.model.UserInfo
import koala.css.Accent
import koala.css.AlignItemsCenter
import koala.css.Flex1
import koala.css.FlexItems1
import koala.css.Width64
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.action
import koala.dom.button
import koala.dom.card
import koala.dom.column
import koala.dom.defaultMagic
import koala.dom.itemsBlock
import koala.dom.row
import koala.dom.tab
import koala.dom.tabs
import koala.dom.textBlock
import koala.dom.textField
import koala.html.Id
import streetlight.web.SongProfileRoute
import streetlight.web.model.Streetlight
import streetlight.web.model.MusicianHub

fun RenderContext.viewMusicianHub(
    app: Streetlight,
    user: UserInfo,
) {
    val model = MusicianHub(renderScope, app.client.api)
    val portal = app.portal
    val gate = app.gate

    column {
        tabs(Id("user-hub-tabs")) {
            tab("Songs") {
                column(modify(AlignItemsCenter)) {
                    row(modify(Width64)) {
                        row(modify(Flex1, FlexItems1)) {
                            textField(
                                label = "title",
                                placeholder = "Song title",
                                bindFlow = model.titleFlow,
                                onChangeValue = model::setSongTitle
                            )
                            textField(
                                label = "artist",
                                placeholder = "Artist",
                                bindFlow = model.artistFlow,
                                onChangeValue = model::setArtist
                            )
                        }
                        button("Add new song", modify(Accent), onClick = model::addSong)
                    }
                    itemsBlock(model.songsFlow, defaultMagic, true) { song ->
                        action(SongProfileRoute(song.songId)) {
                            card {
                                textBlock(song.title)
                            }
                        }
                    }
                }
            }
        }
    }
}