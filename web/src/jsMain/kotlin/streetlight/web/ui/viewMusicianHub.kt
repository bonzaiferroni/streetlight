package streetlight.web.ui

import koala.modifier.*
import koala.dom.ViewScope
import koala.dom.button
import koala.dom.column
import koala.dom.lazyTabs
import koala.dom.row
import koala.html.Id
import streetlight.web.model.MusicianHub

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