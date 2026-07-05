package streetlight.web.io

import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.SignUpRequest
import kampfire.model.Url
import koala.model.DocId
import kotlinx.coroutines.CoroutineScope
import streetlight.model.Api
import streetlight.model.data.*
import kotlin.uuid.Uuid

class ApiClient(private val client: FetchClient) {

    // content
    suspend fun readHomeContent() = client.getApi(Api.Content.Home)

    suspend fun createUser(request: SignUpRequest) = client.postApi(UserApi.Create, request)
    // suspend fun readStarInfo() = client.get(UserApi.ReadInfo)

    // events
    suspend fun readEventId(eventId: EventId) = client.getApi(Api.Events.ReadId, eventId)
    suspend fun readEventSlug(slug: Slug) = client.getApi(Api.Events.ReadSlug, slug)
    suspend fun readEventFeed() = client.getApi(Api.Events)
    suspend fun createEvent(event: EventEdit) = client.postApi(Api.Events.CreateEvent, event)
    suspend fun updateEvent(event: EventEdit) = client.postApi(Api.Events.UpdateEvent, event)
    suspend fun readEventUpdaterContent(slug: Slug) = client.getApi(Api.Events.ReadUpdaterContent, slug)
    suspend fun parseMultiEvent(request: ParseRequest) = client.postApi(Api.Events.ParseMultiEvents, request)
    suspend fun parseSingleEvent(request: ParseRequest) = client.postApi(Api.Events.ParseSingleEvent, request)
    suspend fun readLocationEvents(slug: Slug) = client.getApi(Api.Events.AtLocation, slug)
    suspend fun readEventLocations(eventIds: List<EventId>) = client.postApi(Api.Events.ReadEventLocations, eventIds)
    suspend fun readEventLights() = client.getApi(Api.Events.ReadLights)

    // locations
    suspend fun readLocation(locationId: LocationId) = client.getApi(Api.Locations, locationId)
    suspend fun readLocation(slug: Slug) = client.getApi(Api.Locations.ReadLocation, slug)
    suspend fun readLocationContent(slug: Slug) = client.getApi(Api.Locations.ReadContent, slug)
    suspend fun readLocationUpdaterContent(slug: Slug) = client.getApi(Api.Locations.ReadUpdaterContent, slug)
    suspend fun parseLocation(request: ParseRequest) = client.postApi(Api.Locations.ParseLocation, request)
    suspend fun readLocationsInBounds(bounds: GeoBounds) = client.postApi(Api.Locations.QueryBounds, bounds)
    suspend fun searchLocations(query: String, city: String? = null, state: String? = null, limit: Int = 10) =
        client.getApi(Api.Locations.Search) {
            writeParam(it.query, query)
            writeParam(it.city, city)
            writeParam(it.state, state)
            writeParam(it.limit, limit)
        }

    suspend fun createLocation(location: LocationEdit) = client.postApi(Api.Locations.CreateLocation, location)
    suspend fun updateLocation(location: LocationEdit) = client.postApi(Api.Locations.UpdateLocation, location)
    suspend fun queryMap(request: MapQuery) = client.getApi(Api.Events.QueryMap, request.toQuery())

    // stars
    // suspend fun readUserFiles() = client.get(Api.Users.Files)
    // suspend fun updateUser(user: BasicUserInfo) = client.post(UserApi.Update, user)
    suspend fun checkUsername(username: Username) = client.postApi(UserApi.CheckUsername, username)
    suspend fun generateUsername() = client.getApi(UserApi.GenerateUsername)
    suspend fun readPendingEdits() = client.getApi(Api.Stars.PendingEdits)
    suspend fun readUserTasks() = client.getApi(Api.Tasks.ReadStarTasks)
    suspend fun readReview(taskId: TaskId) = client.getApi(Api.Tasks.ReadStarTask, taskId)

    // suspend fun uploadAvatar(blobUrl: Url) = client.uploadBlob(Api.Users.UploadAvatar.path, blobUrl)
    suspend fun uploadImage(blobUrl: Url) = client.uploadBlob(Api.Users.UploadImage.path, blobUrl)
    suspend fun queryLocation(point: GeoPoint) = client.getApi(Api.Locations.QueryPoint, point.toQuery())
    suspend fun validateLogin() = client.getApi(Api.Stars.ValidateLogin)
    suspend fun logout() = client.postApi(UserApi.Logout, Unit)
    suspend fun updateStar(edit: StarEdit) = client.postApi(Api.Stars.EditStar, edit)
    suspend fun editLight(edit: EditLightRequest) = client.postApi(Api.Stars.EditLight, edit)

    // websockets
    fun connectChat(scope: CoroutineScope) = WebChatSocket(client.connectSocket(Api.Chat), scope)
    fun connectSpiritVision() = client.connectSocket(Api.Map.SpiritVision)
    fun connectOmniLog() = client.connectSocket(Api.Omni.Log)
    fun connectTalkLog(id: Uuid, space: SpaceType) = client.connectSSE(
        Api.Talk.Connect,
        "id" to id.toString(),
        "space" to space.paramValue
    )

    suspend fun readSongs() = client.getApi(Api.Songs)
    suspend fun createSong(song: NewSong) = client.postApi(Api.Songs.Create, song)
    suspend fun readSong(songId: SongId) = client.getApi(Api.Songs.ReadId, songId)
    suspend fun updateSong(song: Song) = client.postApi(Api.Songs.Update, song)

    // talents
    suspend fun readTalents() = client.getApi(Api.Users.Talents)
    suspend fun editTalent(talent: TalentEdit) = client.postApi(Api.Users.EditTalent, talent)

    // galaxies
    suspend fun createOrUpdateGalaxy(galaxy: GalaxyEdit) = client.postApi(Api.Galaxies.CreateGalaxy, galaxy)
    suspend fun readTopGalaxies() = client.getApi(Api.Galaxies.Top)
    suspend fun readGalaxies(galaxyIds: List<GalaxyId>) = client.postApi(Api.Galaxies.ReadGalaxies, galaxyIds)
    suspend fun readGalaxies() = client.getApi(Api.Galaxies.ReadUserGalaxies)
    suspend fun readGalaxy(slug: Slug) = client.getApi(Api.Galaxies.ReadGalaxySlug, slug)
    suspend fun readGalaxyContent(slug: Slug) = client.getApi(Api.Galaxies.ReadContent, slug)
    suspend fun readGalaxy(galaxyId: GalaxyId) = client.getApi(Api.Galaxies.ReadGalaxyId, galaxyId)
    suspend fun createPost(post: PostEdit) = client.postApi(Api.Galaxies.CreatePost, post)
    suspend fun editPost(post: PostEdit) = client.postApi(Api.Galaxies.EditPost, post)
    suspend fun readPosts(galaxyIds: List<GalaxyId>) = client.postApi(Api.Galaxies.ReadMultiPosts, galaxyIds)
    suspend fun readPosts(galaxyId: GalaxyId) = client.getApi(Api.Galaxies.ReadPosts, galaxyId)
    suspend fun readPost(postId: PostId) = client.getApi(Api.Galaxies.ReadPostId, postId)
    suspend fun readGalaxyLights() = client.getApi(Api.Galaxies.ReadLights)
    suspend fun removePost(postId: PostId) = client.postApi(Api.Galaxies.RemovePost, postId)

    suspend fun readStarByUsername(username: String) = client.getApi(Api.Stars.ReadByUsername) {
        writeParam(it.username, username)
    }

    // docs
    suspend fun readSiteDoc(docId: DocId) = client.getApi(Api.Docs, docId)
    suspend fun readSiteDocTable() = client.getApi(Api.DocsTable)

    // talk
    suspend fun readHistory(spaceId: Uuid, spaceType: SpaceType) = client.getApi(Api.Talk.ReadHistory) {
        writeParam(it.spaceId, spaceId)
        writeParam(it.spaceType, spaceType)
    }

    suspend fun createComment(comment: NewComment) = client.postApi(Api.Talk.CreateComment, comment)
    suspend fun updateComment(comment: UpdatedComment) = client.postApi(Api.Talk.UpdateComment, comment)

    // city
    suspend fun readCity(slug: Slug) = client.getApi(Api.Cities.ReadCity, slug)
    suspend fun readTopCities() = client.getApi(Api.Cities.ReadTopCities)
    suspend fun readCityPosts(slug: Slug) = client.getApi(Api.Cities.ReadCityPosts, slug)
    suspend fun searchCity(query: String, country: String) = client.getApi(Api.Cities.Search) {
        writeParam(it.query, query)
        writeParam(it.country, country)
    }

    // media
    suspend fun readMedia(slug: Slug) = client.getApi(Api.Medias.ReadMedia, slug)
    suspend fun createMedia(edit: MediaEdit) = client.postApi(Api.Medias.CreateMedia, edit)
    suspend fun updateMedia(edit: MediaEdit) = client.postApi(Api.Medias.UpdateMedia, edit)

}

