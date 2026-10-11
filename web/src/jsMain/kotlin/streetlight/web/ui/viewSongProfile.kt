package streetlight.web.ui

import koala.dom.*
import koala.dom.component.*
import koala.dom.layout.*

fun RouteScope.viewSongProfile() {
    // td: fix later
    // flowBlock(portal.routeFlowOf<SongProfileRoute>()) { route ->
    //     val model = SongProfile(route.songId, contentScope, api)
//
    //     column {
    //         row {
    //             row(modify(Flex1, FlexItems1)) {
    //                 // td: fix
    //                 // textField(
    //                 //     label = "title",
    //                 //     placeholder = "Song title",
    //                 //     flow = model.titleFlow,
    //                 //     onValue = model::setTitle
    //                 // )
    //                 // textField(
    //                 //     label = "artist",
    //                 //     placeholder = "Artist",
    //                 //     flow = model.artistFlow,
    //                 //     onValue = model::setArtist
    //                 // )
    //             }
    //             button("Update", model::updateSong, modify(Accent))
    //         }
    //         textBlock(model.updatedAtFlow)
    //     }
    // }
}