package streetlight.web

import koala.css.*
import koala.dom.*

fun RenderContext.viewSongProfile(app: AppContext) {
    val portal = app.portal

    flowBlock(portal.routeFlowOf<SongProfileRoute>()) { route ->
        val model = SongProfile(route.songId, renderScope, app.client.api)

        column {
            row {
                row(modify(Flex1, FlexItems1)) {
                    textField(
                        label = "title",
                        placeholder = "Song title",
                        binding = model.titleFlow,
                        onChangeValue = model::setTitle
                    )
                    textField(
                        label = "artist",
                        placeholder = "Artist",
                        binding = model.artistFlow,
                        onChangeValue = model::setArtist
                    )
                }
                button("Update", modify(Accent), onClick = model::updateSong)
            }
            textBlock(model.updatedAtFlow)
        }
    }
}