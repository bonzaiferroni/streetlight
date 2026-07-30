package streetlight.model

import kampfire.api.*
import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.PasswordChange
import kampfire.model.PasswordResetRequest
import kampfire.model.SpeechRequest
import kampfire.model.Url
import koala.model.DocId
import koala.model.DocTableItem
import streetlight.model.data.*
import streetlight.model.data.Feedback as FeedbackDto

object Api: ApiNode(ApiNode(null, "api"), "v1") {

    object Content: ApiNode(this) {
        object Home: GetEndpoint<HomeContent>(this)
    }

    object Events: GetEndpoint<List<Event>>(this) {
        object ReadId: GetByIdEndpoint<EventId, Event>(this)
        object CreateEvent: PostEndpoint<EventEdit, Event>(this)
        object UpdateEvent: PostEndpoint<EventEdit, Event>(this)
        object Delete: DeleteEndpoint<EventId>(this)
        object QueryMap: QueryEndpoint<MapQuery, List<EventLocation>>(this)
        object ReadUpdaterContent: GetByIdEndpoint<Slug, EventUpdaterContent>(this)
        // object UserEvents: ApiDaoEndpoint<Event, EventId, NewEvent>(this, "user")

        object ParseMultiEvents: PostEndpoint<ParseRequest, MultiEventParseResponse>(this)
        object ParseSingleEvent: PostEndpoint<ParseRequest, EventEdit>(this)
        object ParseEvent: PostEndpoint<ParseRequest, EventParseResult>(this)

        object AtLocation: GetByIdEndpoint<Slug, List<Event>>(this)
        object ReadEventLocations: PostEndpoint<List<EventId>, List<EventLocation>>(this)
        object ReadLights: GetEndpoint<List<EventId>>(this)
        object ReadSlug: GetByIdEndpoint<Slug, EventLocation>(this)
    }

    object Locations: GetByIdEndpoint<LocationId, Location>(this) {
        object ReadLocation: GetByIdEndpoint<Slug, Location>(this)
        object ReadUpdaterContent: GetByIdEndpoint<Slug, LocationUpdaterContent>(this)
        object ReadContent: GetByIdEndpoint<Slug, LocationContent>(this)

        object CreateLocation: PostEndpoint<LocationEdit, Location>(this)
        object UpdateLocation: PostEndpoint<LocationEdit, Location>(this)
        object Search: GetEndpoint<List<Location>>(this) {
            val query = stringParamOf("q")
            val city = stringOrNullParamOf("city")
            val state = stringOrNullParamOf("state")
            val limit = intParamOf("limit")
        }
        object ReadTop: GetEndpoint<List<Location>>(this) {
            val count = intParamOf("count")
        }
        object QueryPoint: QueryEndpoint<GeoPoint, List<Location>>(this)
        object ParseLocation: PostEndpoint<ParseRequest, LocationEdit>(this)
        object QueryBounds: PostEndpoint<GeoBounds, List<LocationInfo>>(this)
    }

    object Songs: GetEndpoint<List<Song>>(this) {
        object ReadId: GetByIdEndpoint<SongId, Song>(this)
        object Create: PostEndpoint<NewSong, SongId>(this)
        object Update: PostEndpoint<Song, Boolean>(this)
        object TakeNextSong: GetByIdEndpoint<EventId, EventSong>(this) {
            val since = instantParamOf("since")
        }
    }

    object RenditionFeed: GetByIdEndpoint<RenditionId, Rendition>(this) {
        object BySong: GetByIdEndpoint<SongId, List<Rendition>>(this)
        object Create: PostEndpoint<NewRendition, RenditionId>(this)
        object Update: PostEndpoint<Rendition, Boolean>(this)
        object Delete: DeleteEndpoint<RenditionId>(this)
        object ReadAllSince: GetEndpoint<List<Rendition>>(this) {
            val since = instantParamOf("since")
        }
    }

    object RequestBox: PostEndpoint<NewRequest, RequestId>(this)

    object Speech: ApiNode(this), SpeechApi {
        override val wav = PostEndpoint<SpeechRequest, ByteArray>(this, "wav")
        override val url = PostEndpoint<SpeechRequest, String>(this, "url")
    }

    object Gtfs: ApiNode(this) {
        object VehiclePosition: GetEndpoint<Unit>(this, "VehiclePosition.pb")
        object Routes: GetEndpoint<AreaTransit>(this)
        object TransitState: GetEndpoint<AreaTransitState?>(this) {
            val timestamp = longParamOf("timestamp")
        }
    }

    object Users: ApiNode(this) {
        object Files: GetEndpoint<List<Url>>(this)
        object Talents: GetEndpoint<List<Talent>>(this)
        object EditTalent: PostEndpoint<TalentEdit, Talent>(this)
        object UploadAvatar: PostEndpoint<ByteArray, String>(this)
        object UploadImage: PostEndpoint<ByteArray, Url>(this)
    }

    object Chat: ApiNode(this) { }
    object Omni: ApiNode(this) {
        object Log: ApiNode(this)
    }

    object Map: ApiNode(this) {
        object SpiritVision: ApiNode(this)
    }

    object Galaxies: ApiNode(this) {
        object CreateGalaxy: PostEndpoint<GalaxyEdit, Slug>(this)
        object UpdateGalaxy: PostEndpoint<GalaxyEdit, Slug>(this)
        object Top: GetEndpoint<List<Galaxy>>(this)
        object ReadGalaxies: PostEndpoint<List<GalaxyId>, List<Galaxy>>(this)
        object ReadUserGalaxies: GetEndpoint<List<Galaxy>>(this)
        object ReadGalaxySlug: GetByIdEndpoint<Slug, Galaxy>(this)
        object ReadGalaxyId: GetByIdEndpoint<GalaxyId, Galaxy>(this)
        object ReadContent: GetByIdEndpoint<Slug, GalaxyContent>(this)
        object CreatePost: PostEndpoint<PostEdit, Post>(this)
        object UpdatePost: PostEndpoint<PostEdit, Post>(this)
        object ReadMultiPosts: PostEndpoint<List<GalaxyId>, List<GalaxyPost>>(this)
        object ReadPosts: GetByIdEndpoint<GalaxyId, List<GalaxyPost>>(this)
        object ReadPostId: GetByIdEndpoint<PostId, GalaxyPost>(this)
        object ReadLights: GetEndpoint<List<GalaxyId>>(this)
        object RemovePost: PostEndpoint<PostId, Boolean>(this)
    }

    object Medias: ApiNode(this, "media") { // I have died a little inside but this avoids import conflicts
        object ReadMedia: GetByIdEndpoint<Slug, Media>(this)
        object CreateMedia: PostEndpoint<MediaEdit, Media>(this)
        object UpdateMedia: PostEndpoint<MediaEdit, Media>(this)
    }

    object Cities: ApiNode(this) {
        object ReadTopCities: GetEndpoint<List<City>>(this)
        object ReadCity: GetByIdEndpoint<Slug, City>(this)
        object ReadCityPosts: GetByIdEndpoint<Slug, List<Entity>>(this)

        object Search: GetEndpoint<List<City>>(this) {
            val query = stringParamOf("name")
            val country = stringParamOf("country")
            val limit = intParamOf("limit")
        }

        object SearchLocation: GetEndpoint<List<Location>>(this) {
            val query = stringParamOf("name")
            val city = stringParamOf("city")
            val state = stringParamOf("state")
            val limit = intParamOf("limit")
        }
    }

    object Stars: ApiNode(this) {
        object ValidateLogin: GetEndpoint<Star?>(this)
        object EditLight: PostEndpoint<EditLightRequest, Boolean>(this)
        object PendingEdits: GetEndpoint<List<EditLog>>(this)
        object ReadAccount: GetEndpoint<Account>(this)
        object UpdateProfile: PostEndpoint<StarEdit, Star>(this)

        object ReadStarContent: GetEndpoint<StarContent>(this) {
            val username = usernameParamOf("username")
        }
    }

    object AccountAction: ApiNode(this) {
        object ResetPassword: PostEndpoint<Email, Unit>(this) {
            object Redemption: PostEndpoint<PasswordResetRequest, String>(this)
        }
        object AccountNotOwned: PostEndpoint<Unit, Unit>(this)
        object LockdownAccount: PostEndpoint<Unit, Unit>(this)
        object RemoveEmail: PostEndpoint<Unit, Unit>(this)
        object VerifyExistingEmail: PostEndpoint<Unit, Unit>(this) {
            object CheckStatus: GetEndpoint<Boolean>(this)
        }
        object ChangePassword: PostEndpoint<PasswordChange, Unit>(this)
        object AddEmail: PostEndpoint<Email, Unit>(this)
    }

    object Docs: GetByIdEndpoint<DocId, DocContent>(this)
    object DocsTable: GetEndpoint<List<DocTableItem>>(this)

    object Talk: ApiNode(this) {
        object ReadGalaxy: GetByIdEndpoint<GalaxyId, List<Comment>>(this)
        object ReadHistory: GetEndpoint<List<Comment>>(this) {
            val spaceId = uuidParamOf("space-id")
            val spaceType = enumParamOf<SpaceType>("space-type")
        }
        object Connect: ApiNode(this)
        object CreateComment: PostEndpoint<NewComment, CommentId>(this)
        object UpdateComment: PostEndpoint<UpdatedComment, Boolean>(this)
    }

    object Tasks: ApiNode(this) {
        object ReadStarTasks: GetEndpoint<List<TaskContent>>(this)
        object ReadStarTask: GetByIdEndpoint<TaskId, TaskContent>(this)
        object CompleteTask: PostEndpoint<TaskCompletion, Boolean>(this)
    }

    object Feedback: ApiNode(this) {
        object Feed: GetEndpoint<List<FeedbackDto>>(this)
        object Create: PostEndpoint<FeedbackEdit, Boolean>(this)
    }

    object Status: ApiNode(this) {
        object Feed: GetEndpoint<List<SiteStatus>>(this) {
            val resolution = enumParamOf<MetricResolution>("resolution")
        }
        object ReadLast: GetEndpoint<SiteStatus>(this) {
            val resolution = enumParamOf<MetricResolution>("resolution")
        }
    }
}