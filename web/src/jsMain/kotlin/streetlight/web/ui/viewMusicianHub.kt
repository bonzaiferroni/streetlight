package streetlight.web.ui

import koala.css.Accent
import koala.css.AlignItemsCenter
import koala.css.Flex1
import koala.css.FlexItems1
import koala.css.Width64
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.navigation
import koala.dom.button
import koala.dom.card
import koala.dom.column
import koala.dom.defaultMagic
import koala.dom.itemsBlock
import koala.dom.row
import koala.dom.tabs
import koala.dom.textBlock
import koala.dom.textField
import koala.html.Id
import streetlight.model.ui.SongProfileRoute
import streetlight.web.model.MusicianHub

fun ViewScope.viewMusicianHub(
    // user: BasicUserInfo,
) {
    val model = MusicianHub(contentScope, api)

    column {
        tabs(Id("user-hub-tabs")) {
            tab("Songs") {
                column(modify(AlignItemsCenter)) {
                    row(modify(Width64)) {
                        row(modify(Flex1, FlexItems1)) {
                            textField(
                                label = "title",
                                placeholder = "Song title",
                                flow = model.titleFlow,
                                onValue = model::setSongTitle
                            )
                            textField(
                                label = "artist",
                                placeholder = "Artist",
                                flow = model.artistFlow,
                                onValue = model::setArtist
                            )
                        }
                        button("Add new song", model::addSong, modify(Accent))
                    }
                    itemsBlock(model.songsFlow, defaultMagic) { song ->
                        navigation(SongProfileRoute(song.songId)) {
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