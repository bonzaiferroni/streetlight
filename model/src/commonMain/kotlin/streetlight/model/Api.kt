package streetlight.model

import kabinet.api.ApiNode
import kabinet.api.DeleteEndpoint
import kabinet.api.GetByTableIdEndpoint
import kabinet.api.GetEndpoint
import kabinet.api.PostEndpoint
import kabinet.api.QueryEndpoint
import kabinet.api.SpeechApi
import kabinet.api.UpdateEndpoint
import kabinet.clients.GeminiMessage
import kabinet.gemini.GeminiApi
import kabinet.model.GeoPoint
import kabinet.model.ImageGenRequest
import kabinet.model.ImageUrls
import kabinet.model.SpeechRequest
import streetlight.model.data.Street
import streetlight.model.data.StreetId
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.EventSong
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.NewStreet
import streetlight.model.data.NewEvent
import streetlight.model.data.NewLocation
import streetlight.model.data.NewSong
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.model.data.Rendition
import streetlight.model.data.RenditionId
import streetlight.model.data.NewRendition
import streetlight.model.data.NewRequest
import streetlight.model.data.RequestId
import streetlight.model.data.StreetTransit

object Api: ApiNode(ApiNode(null, "api"), "v1") {

    object EventProfile: GetByTableIdEndpoint<EventId, Event>(this, "event") {
        object Update: UpdateEndpoint<Event>(this, "update")
    }

    object EventFeed: GetEndpoint<List<Event>>(this, "events") {
        object Create: PostEndpoint<NewEvent, Event>(this, "create")
        object Delete: DeleteEndpoint<EventId>(this, "delete")
        object LocationEvents: QueryEndpoint<GeoPoint, List<Event>>(this, "location")
        // object UserEvents: ApiDaoEndpoint<Event, EventId, NewEvent>(this, "user")
    }

    object StreetFeed: GetEndpoint<List<Street>>(this, "areas") {
        object Create: PostEndpoint<NewStreet, StreetId>(this, "create")
    }

    object LocationFeed: GetByTableIdEndpoint<LocationId, Location>(this, "locations") {
        object Create: PostEndpoint<NewLocation, LocationId>(this, "create")
        object Street: GetByTableIdEndpoint<StreetId, List<Location>>(this, "street")
        object Update: PostEndpoint<Location, Boolean>(this, "update")
        object Search: GetEndpoint<List<Location>>(this, "search") {
            val query = addStringParam("q")
        }
        object ReadTop: GetEndpoint<List<Location>>(this, "read_top") {
            val count = addIntParam("count")
        }
    }

    object SongFeed: GetEndpoint<List<Song>>(this, "songs") {
        object Create: PostEndpoint<NewSong, SongId>(this, "create")
        object TakeNextSong: GetByTableIdEndpoint<EventId, EventSong>(this, "take_next_song") {
            val since = addInstantParam("since")
        }
    }

    object SongProfile: GetByTableIdEndpoint<SongId, Song>(this, "song") {
        object Update: PostEndpoint<Song, Boolean>(this, "update")
    }

    object RenditionFeed: GetByTableIdEndpoint<RenditionId, Rendition>(this, "renditions") {
        object BySong: GetByTableIdEndpoint<SongId, List<Rendition>>(this, "by_song")
        object Create: PostEndpoint<NewRendition, RenditionId>(this, "create")
        object Update: PostEndpoint<Rendition, Boolean>(this, "update")
        object Delete: DeleteEndpoint<RenditionId>(this, "delete")
        object ReadAllSince: GetEndpoint<List<Rendition>>(this, "read_all_since") {
            val since = addInstantParam("since")
        }
    }

    object RequestBox: PostEndpoint<NewRequest, RequestId>(this, "request_box")

    object Gemini: ApiNode(this, "gemini"), GeminiApi {
        override val chat = PostEndpoint<List<GeminiMessage>, String>(this, "chat")
        override val image = PostEndpoint<ImageGenRequest, ImageUrls>(this, "image")
        override val speechUrl = PostEndpoint<SpeechRequest, String>(this, "speechUrl")
        override val speech = PostEndpoint<SpeechRequest, ByteArray>(this, "speech")
    }

    object Speech: ApiNode(this, "speech"), SpeechApi {
        override val wav = PostEndpoint<SpeechRequest, ByteArray>(this, "wav")
        override val url = PostEndpoint<SpeechRequest, String>(this, "url")
    }

    object Gtfs: ApiNode(this, "gtfs") {
        object VehiclePosition: GetEndpoint<Unit>(this, "VehiclePosition.pb")
        object Routes: GetEndpoint<StreetTransit>(this, "routes")
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