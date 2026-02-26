package streetlight.model

import kampfire.api.*
import kampfire.model.GeoPoint
import kampfire.model.SpeechRequest
import streetlight.model.data.*

object Api: ApiNode(ApiNode(null, "api"), "v1") {

    object EventProfile: GetByTableIdEndpoint<EventId, Event>(this, "event") {
        // todo: consolidate with Events
        object Update: UpdateEndpoint<Event>(this, "update")
    }

    object Events: GetEndpoint<List<Event>>(this, "events") {
        object Edit: PostEndpoint<EventEdit, Event>(this, "create")
        object Delete: DeleteEndpoint<EventId>(this, "delete")
        object QueryMap: QueryEndpoint<MapQuery, List<EventInfo>>(this, "bounds")
        // object UserEvents: ApiDaoEndpoint<Event, EventId, NewEvent>(this, "user")
        object Upload: PostEndpoint<ByteArray, String>(this, "upload")
        object ReadUrl: PostEndpoint<ReadEventRequest, EventParse>(this, "read-url")
    }

    object StreetFeed: GetEndpoint<List<Community>>(this, "areas") {
        object Create: PostEndpoint<NewCommunity, CommunityId>(this, "create")
    }

    object Locations: GetByTableIdEndpoint<LocationId, Location>(this, "locations") {
        @Deprecated("use edit")
        object Create: PostEndpoint<Place, LocationId>(this, "create")
        object Edit: PostEndpoint<LocationEdit, Location>(this, "edit")
        object Street: GetByTableIdEndpoint<CommunityId, List<Location>>(this, "street")
        @Deprecated("use edit")
        object Update: PostEndpoint<Location, Boolean>(this, "update")
        object Search: GetEndpoint<List<Location>>(this, "search") {
            val query = addStringParam("q")
        }
        object ReadTop: GetEndpoint<List<Location>>(this, "read_top") {
            val count = addIntParam("count")
        }
        object QueryPoint: QueryEndpoint<GeoPoint, List<Location>>(this, "query_point")
        object ParseLocation: PostEndpoint<String, LocationParse>(this, "parse_location")
    }

    object Songs: GetEndpoint<List<Song>>(this, "songs") {
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

    object Users: ApiNode(this, "users") {
        object Files: GetEndpoint<List<String>>(this, "files")
        object Talents: GetEndpoint<List<Talent>>(this, "talents")
        object EditTalent: PostEndpoint<TalentEdit, Talent>(this, "edit-talent")
    }

    object Stories: ApiNode(this, "story") {
        object ReadUrl: GetEndpoint<StoryParse>(this, "read-url") {
            val url = addStringParam("url")
        }
    }

    object Chat: ApiNode(this, "chat") {

    }
}