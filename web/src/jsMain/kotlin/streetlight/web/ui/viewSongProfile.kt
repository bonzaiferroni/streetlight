package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.SongProfileRoute
import streetlight.web.model.SongProfile

fun ScopedDOM.viewSongProfile() {

    flowBlock(portal.routeFlowOf<SongProfileRoute>()) { route ->
        val model = SongProfile(route.songId, renderScope, api)

        column {
            row {
                row(modify(Flex1, FlexItems1)) {
                    textField(
                        label = "title",
                        placeholder = "Song title",
                        flow = model.titleFlow,
                        onValue = model::setTitle
                    )
                    textField(
                        label = "artist",
                        placeholder = "Artist",
                        flow = model.artistFlow,
                        onValue = model::setArtist
                    )
                }
                button("Update", modify(Accent), onClick = model::updateSong)
            }
            textBlock(model.updatedAtFlow)
        }
    }
}