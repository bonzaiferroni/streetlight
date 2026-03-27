package streetlight.model

import kampfire.api.*
import kampfire.model.GeoBounds
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
        object QueryMap: QueryEndpoint<MapQuery, List<EventLocation>>(this, "bounds")
        // object UserEvents: ApiDaoEndpoint<Event, EventId, NewEvent>(this, "user")
        object ParseMultiEvents: PostEndpoint<ParseRequest, MultiEventParseResponse>(this, "parse-multi")
        object ParseSingleEvent: PostEndpoint<ParseRequest, SingleEventParseResponse>(this, "parse-single")
        object AtLocation: GetByTableIdEndpoint<LocationId, List<Event>>(this, "location")
        object EditEventStar: PostEndpoint<EventStar, Boolean>(this, "edit-event-star")
        object ReadEventStars: GetEndpoint<List<EventStar>>(this, "read-event-stars")
        object ReadEventLocations: PostEndpoint<List<EventId>, List<EventLocation>>(this, "read-event-locations")
    }

    object Locations: GetByTableIdEndpoint<LocationId, Location>(this, "locations") {
        @Deprecated("use edit")
        object Create: PostEndpoint<Place, LocationId>(this, "create")
        object Edit: PostEndpoint<LocationEdit, Location>(this, "edit")
        object Street: GetByTableIdEndpoint<GalaxyId, List<Location>>(this, "street")
        @Deprecated("use edit")
        object Update: PostEndpoint<Location, Boolean>(this, "update")
        object Search: GetEndpoint<List<Location>>(this, "search") {
            val query = addStringParam("q")
        }
        object ReadTop: GetEndpoint<List<Location>>(this, "read_top") {
            val count = addIntParam("count")
        }
        object QueryPoint: QueryEndpoint<GeoPoint, List<Location>>(this, "query_point")
        object ParseLocation: PostEndpoint<ParseRequest, LocationEdit>(this, "parse_location")
        object QueryBounds: PostEndpoint<GeoBounds, List<LocationInfo>>(this, "query_bounds")
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
        object TransitState: GetEndpoint<AreaTransitState>(this, "state") {
            val timestamp = addLongParam("timestamp")
        }
    }

    object Users: ApiNode(this, "users") {
        object Files: GetEndpoint<List<String>>(this, "files")
        object Talents: GetEndpoint<List<Talent>>(this, "talents")
        object EditTalent: PostEndpoint<TalentEdit, Talent>(this, "edit-talent")
        object UploadAvatar: PostEndpoint<ByteArray, String>(this, "upload-avatar")
        object UploadImage: PostEndpoint<ByteArray, String>(this, "upload")
    }

    object Stories: ApiNode(this, "story") {
        object ReadUrl: GetEndpoint<StoryParse>(this, "read-url") {
            val url = addStringParam("url")
        }
    }

    object Chat: ApiNode(this, "chat") { }

    object Map: ApiNode(this, "map") {
        object SpiritVision: ApiNode(this, "spirit-vision")
    }

    object Galaxies: ApiNode(this, "galaxy") {
        object Found: PostEndpoint<GalaxyEdit, Galaxy>(this, "found")
        object All: GetEndpoint<List<Galaxy>>(this, "areas")
        object Path: GetByIdEndpoint<PathId, Galaxy>(this, "path")
        object CreatePost: PostEndpoint<GalaxyPostEdit, GalaxyPostId>(this, "create-post")
        object ReadMultiPosts: PostEndpoint<List<GalaxyId>, List<GalaxyPost>>(this, "multi-posts")
        object ReadPosts: GetByTableIdEndpoint<GalaxyId, List<GalaxyPost>>(this, "posts")
        object ReadPost: GetByTableIdEndpoint<GalaxyPostId, GalaxyPost>(this, "post")
    }
}