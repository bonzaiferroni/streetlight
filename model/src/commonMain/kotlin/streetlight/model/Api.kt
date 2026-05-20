package streetlight.model

import kampfire.api.*
import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.SpeechRequest
import kampfire.model.Url
import koala.model.DocId
import koala.model.DocNode
import koala.model.DocTableItem
import streetlight.model.data.*

object Api: ApiNode(ApiNode(null, "api"), "v1") {

    object Content: ApiNode(this, "content") {
        object Home: GetEndpoint<HomeContent>(this, "home")
    }

    object Events: GetEndpoint<List<Event>>(this, "events") {
        object ReadById: GetByIdEndpoint<EventId, Event>(this, "read-by-id")
        object Edit: PostEndpoint<EventEdit, Event>(this, "create")
        object Delete: DeleteEndpoint<EventId>(this, "delete")
        object QueryMap: QueryEndpoint<MapQuery, List<EventLocation>>(this, "bounds")
        // object UserEvents: ApiDaoEndpoint<Event, EventId, NewEvent>(this, "user")

        object ParseMultiEvents: PostEndpoint<ParseRequest, MultiEventParseResponse>(this, "parse-multi")
        object ParseSingleEvent: PostEndpoint<ParseRequest, EventEdit>(this, "parse-single")
        object ParseEvent: PostEndpoint<ParseRequest, EventParseResult>(this, "parse-event")

        object AtLocation: GetByIdEndpoint<LocationId, List<Event>>(this, "location")
        object ReadEventLocations: PostEndpoint<List<EventId>, List<EventLocation>>(this, "read-event-locations")
        object ReadLights: GetEndpoint<List<EventId>>(this, "lights")
        object ReadBySlug: GetByIdEndpoint<Slug, Event>(this, "slug")
        object ReadEventLocationBySlug: GetByIdEndpoint<Slug, EventLocation>(this, "event-location-slug")
    }

    object Locations: GetByIdEndpoint<LocationId, Location>(this, "locations") {
        @Deprecated("use edit")
        object Create: PostEndpoint<PlaceProto, LocationId>(this, "create")
        object CreateOrEdit: PostEndpoint<LocationEdit, Location>(this, "edit")
        @Deprecated("use posts to associate locations with galaxies")
        object Street: GetByIdEndpoint<GalaxyId, List<Location>>(this, "street")
        @Deprecated("use edit")
        object Update: PostEndpoint<Location, Boolean>(this, "update")
        object Search: GetEndpoint<List<Location>>(this, "search") {
            val query = stringParamOf("q")
            val city = stringOrNullParamOf("city")
            val state = stringOrNullParamOf("state")
            val limit = intParamOf("limit")
        }
        object ReadTop: GetEndpoint<List<Location>>(this, "read-top") {
            val count = intParamOf("count")
        }
        object QueryPoint: QueryEndpoint<GeoPoint, List<Location>>(this, "query-point")
        object ParseLocation: PostEndpoint<ParseRequest, LocationEdit>(this, "parse-location")
        object QueryBounds: PostEndpoint<GeoBounds, List<LocationInfo>>(this, "query-bounds")
    }

    object Songs: GetEndpoint<List<Song>>(this, "songs") {
        object Create: PostEndpoint<NewSong, SongId>(this, "create")
        object TakeNextSong: GetByIdEndpoint<EventId, EventSong>(this, "take-next-song") {
            val since = instantParamOf("since")
        }
    }

    object SongProfile: GetByIdEndpoint<SongId, Song>(this, "song") {
        object Update: PostEndpoint<Song, Boolean>(this, "update")
    }

    object RenditionFeed: GetByIdEndpoint<RenditionId, Rendition>(this, "renditions") {
        object BySong: GetByIdEndpoint<SongId, List<Rendition>>(this, "by-song")
        object Create: PostEndpoint<NewRendition, RenditionId>(this, "create")
        object Update: PostEndpoint<Rendition, Boolean>(this, "update")
        object Delete: DeleteEndpoint<RenditionId>(this, "delete")
        object ReadAllSince: GetEndpoint<List<Rendition>>(this, "read-all-since") {
            val since = instantParamOf("since")
        }
    }

    object RequestBox: PostEndpoint<NewRequest, RequestId>(this, "request_box")

    object Speech: ApiNode(this, "speech"), SpeechApi {
        override val wav = PostEndpoint<SpeechRequest, ByteArray>(this, "wav")
        override val url = PostEndpoint<SpeechRequest, String>(this, "url")
    }

    object Gtfs: ApiNode(this, "gtfs") {
        object VehiclePosition: GetEndpoint<Unit>(this, "VehiclePosition.pb")
        object Routes: GetEndpoint<AreaTransit>(this, "routes")
        object TransitState: GetEndpoint<AreaTransitState>(this, "state") {
            val timestamp = longParamOf("timestamp")
        }
    }

    object Users: ApiNode(this, "users") {
        object Files: GetEndpoint<List<Url>>(this, "files")
        object Talents: GetEndpoint<List<Talent>>(this, "talents")
        object EditTalent: PostEndpoint<TalentEdit, Talent>(this, "edit-talent")
        object UploadAvatar: PostEndpoint<ByteArray, String>(this, "upload-avatar")
        object UploadImage: PostEndpoint<ByteArray, Url>(this, "upload")
    }

    object Chat: ApiNode(this, "chat") { }
    object Omni: ApiNode(this, "omni") {
        object Log: ApiNode(this, "log")
    }

    object Map: ApiNode(this, "map") {
        object SpiritVision: ApiNode(this, "spirit-vision")
    }

    object Galaxies: ApiNode(this, "galaxy") {
        object CreateOrEdit: PostEndpoint<GalaxyEdit, Galaxy>(this, "found")
        object Top: GetEndpoint<List<Galaxy>>(this, "areas")
        object ReadGalaxies: PostEndpoint<List<GalaxyId>, List<Galaxy>>(this, "read-galaxies")
        object ReadSlug: GetByIdEndpoint<String, Galaxy>(this, "slug")
        object ReadId: GetByIdEndpoint<StringId, Galaxy>(this, "id")
        object PostEvent: PostEndpoint<EventPostEdit, Post>(this, "post-event")
        object PostContent: PostEndpoint<StarPostEdit, Post>(this, "post-content")
        object EditContent: PostEndpoint<StarPostEdit, Post>(this, "edit-content")
        object PostLocation: PostEndpoint<LocationPostEdit, Post>(this, "post-location")
        object ReadMultiPosts: PostEndpoint<List<GalaxyId>, List<Post>>(this, "multi-posts")
        object ReadPosts: GetByIdEndpoint<GalaxyId, List<Post>>(this, "posts")
        object ReadPost: GetByIdEndpoint<StringId, Post>(this, "post")
        object ReadLights: GetEndpoint<List<GalaxyId>>(this, "lights")
        object RemovePost: PostEndpoint<PostId, Boolean>(this, "remove")
    }

    object Cities: ApiNode(this, "locality") {
        object Search: GetEndpoint<List<City>>(this, "search-city") {
            val query = stringParamOf("name")
            val country = stringParamOf("country")
            val limit = intParamOf("limit")
        }

        object SearchLocation: GetEndpoint<List<Location>>(this, "search-place") {
            val query = stringParamOf("name")
            val city = stringParamOf("city")
            val state = stringParamOf("state")
            val limit = intParamOf("limit")
        }
    }

    object Stars: ApiNode(this, "star") {
        object ReadByUsername: GetEndpoint<Star>(this, "star") {
            val username = stringParamOf("username")
        }

        object ValidateLogin: GetEndpoint<Star>(this, "validate-login")
        object EditStar: PostEndpoint<StarEdit, Star>(this, "edit")
        object EditLight: PostEndpoint<EditLightRequest, Boolean>(this, "edit-light")
    }

    object Docs: GetByIdEndpoint<DocId, DocNode>(this, "doc")
    object SiteDocTable: GetEndpoint<List<DocTableItem>>(this, "doc-table")

    object Talk: ApiNode(this, "talk") {
        object ReadGalaxy: GetByIdEndpoint<GalaxyId, List<Comment>>(this, "galaxy")
        object ReadHistory: GetEndpoint<List<Comment>>(this, "history") {
            val spaceId = uuidParamOf("space-id")
            val spaceType = enumParamOf<SpaceType>("space-type")
        }
        object Connect: ApiNode(this, "connect")
        object CreateComment: PostEndpoint<NewComment, CommentId>(this, "create")
        object UpdateComment: PostEndpoint<UpdatedComment, Boolean>(this, "update")
    }

}