package streetlight.web.view

import koala.modifier.*
import koala.dom.ViewScope
import koala.dom.component.button
import koala.dom.component.column
import koala.dom.layout.lazyTabs
import koala.dom.component.row
import koala.page.Id
import streetlight.web.model.MusicianHub
import streetlight.web.model.api

fun ViewScope.viewMusicianHub(
    // user: BasicUserInfo,
) {
    val model = MusicianHub(contentScope, api)

    column {
        lazyTabs(Id("user-hub-tabs")) {
            tab("Songs") {
                column(AlignItemsCenter) {
                    row(Width(64)) {
                        row(modify(Flex1, FlexItems1)) {
                            // td: fix later
                            // textField(
                            //     label = "title",
                            //     placeholder = "Song title",
                            //     flow = model.titleFlow,
                            //     onValue = model::setSongTitle
                            // )
                            // textField(
                            //     label = "artist",
                            //     placeholder = "Artist",
                            //     flow = model.artistFlow,
                            //     onValue = model::setArtist
                            // )
                        }
                        button("Add new song", model::addSong, Accent)
                    }
//                    itemsBlock(model.songsFlow, defaultMagic) { song ->
//                        navigation(SongProfileRoute(song.songId)) {
//                            card {
//                                textBlock(song.title)
//                            }
//                        }
//                    }
                }
            }
        }
    }
}