package streetlight.model

import kabinet.api.ApiNode
import kabinet.api.GetByIdEndpoint
import kabinet.api.GetByTableIdEndpoint
import kabinet.api.GetEndpoint
import kabinet.api.PostEndpoint
import streetlight.model.data.Area
import streetlight.model.data.AreaId
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.NewArea
import streetlight.model.data.NewEvent
import streetlight.model.data.NewLocation
import streetlight.model.data.NewSong
import streetlight.model.data.Song
import streetlight.model.data.SongId

object Api: ApiNode(ApiNode(null, "api"), "v1") {

    object Events: GetEndpoint<List<Event>>(this, "events") {
        object Create: PostEndpoint<NewEvent, EventId>(this, "create")
    }

    object Areas: GetEndpoint<List<Area>>(this, "areas") {
        object Create: PostEndpoint<NewArea, AreaId>(this, "create")
    }

    object Locations: GetByTableIdEndpoint<LocationId, Location>(this, "locations") {
        object Create: PostEndpoint<NewLocation, LocationId>(this, "create")
        object Area: GetByTableIdEndpoint<AreaId, List<Location>>(this, "area")
        object Update: PostEndpoint<Location, Boolean>(this, "update")
    }

    object Songs: GetEndpoint<List<Song>>(this, "songs") {
        object Create: PostEndpoint<NewSong, SongId>(this, "create")
    }
}

// utility
//
//    // models
//    val area = Endpoint("/data/area")
//    val event = Endpoint("/data/event")
//    val location = Endpoint("/data/location")
//    val request = Endpoint("/data/request")
//
//    // request
//    val requestInfoEvent = Endpoint("/request_info/event")
//    val requestInfo = Endpoint("/request_info")
//    val requestInfoQueue = Endpoint("/request_info/queue")
//    val requestInfoRandom = Endpoint("/request_info/random")
//
//    // user
//    val song = Endpoint("/user/song")
//    val atlas = Endpoint("/user/atlas")
//
//    // event
//    val eventInfoCurrent = Endpoint("/event_info/current")
//    val eventInfo = Endpoint("/event_info")
//    val createEventRequest = Endpoint("/event_profile/request")
//    val readEventRequests = Endpoint("/event_profile")
//    val uploadEventImage = Endpoint("/event_profile/image")