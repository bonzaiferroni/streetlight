package streetlight.model

import kampfire.api.ApiNode
import kampfire.api.DeleteEndpoint
import kampfire.api.GetByTableIdEndpoint
import kampfire.api.GetEndpoint
import kampfire.api.PostEndpoint
import kampfire.api.QueryEndpoint
import kampfire.api.SpeechApi
import kampfire.api.UpdateEndpoint
import streetlight.model.data.LocationEventsRequest
import kampfire.model.SpeechRequest
import streetlight.model.data.Area
import streetlight.model.data.AreaId
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.EventSong
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.NewArea
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
import streetlight.model.data.AreaTransit

object Api: ApiNode(ApiNode(null, "api"), "v1") {

    object EventProfile: GetByTableIdEndpoint<EventId, Event>(this, "event") {
        object Update: UpdateEndpoint<Event>(this, "update")
    }

    object EventFeed: GetEndpoint<List<Event>>(this, "events") {
        object Create: PostEndpoint<NewEvent, Event>(this, "create")
        object Delete: DeleteEndpoint<EventId>(this, "delete")
        object LocationEvents: QueryEndpoint<LocationEventsRequest, List<Event>>(this, "location")
        // object UserEvents: ApiDaoEndpoint<Event, EventId, NewEvent>(this, "user")
    }

    object StreetFeed: GetEndpoint<List<Area>>(this, "areas") {
        object Create: PostEndpoint<NewArea, AreaId>(this, "create")
    }

    object LocationFeed: GetByTableIdEndpoint<LocationId, Location>(this, "locations") {
        object Create: PostEndpoint<NewLocation, LocationId>(this, "create")
        object Street: GetByTableIdEndpoint<AreaId, List<Location>>(this, "street")
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

//    object Gemini: ApiNode(this, "gemini"), GeminiApi {
//        override val chat = PostEndpoint<List<GeminiMessage>, String>(this, "chat")
//        override val image = PostEndpoint<ImageGenRequest, ImageUrls>(this, "image")
//        override val speechUrl = PostEndpoint<SpeechRequest, String>(this, "speechUrl")
//        override val speech = PostEndpoint<SpeechRequest, ByteArray>(this, "speech")
//    }

    object Speech: ApiNode(this, "speech"), SpeechApi {
        override val wav = PostEndpoint<SpeechRequest, ByteArray>(this, "wav")
        override val url = PostEndpoint<SpeechRequest, String>(this, "url")
    }

    object Gtfs: ApiNode(this, "gtfs") {
        object VehiclePosition: GetEndpoint<Unit>(this, "VehiclePosition.pb")
        object Routes: GetEndpoint<AreaTransit>(this, "routes")
    }
}