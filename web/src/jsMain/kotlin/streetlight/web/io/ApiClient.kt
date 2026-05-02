package streetlight.web.io

import kampfire.api.StringId
import kampfire.api.UserApi
import kampfire.model.ApiResponse
import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.SignUpRequest
import kampfire.model.Url
import koala.model.DocId
import kotlinx.coroutines.CoroutineScope
import streetlight.model.Api
import streetlight.model.data.*

class ApiClient(private val client: FetchClient) {

    // content
    suspend fun readHomeContent() = client.getApi(Api.Content.Home)

    suspend fun createUser(request: SignUpRequest) = client.post(UserApi.Create, request)
    // suspend fun readStarInfo() = client.get(UserApi.ReadInfo)

    // events
    suspend fun readEvent(eventId: EventId) = client.get(Api.Events.ReadById, eventId)
    suspend fun readEventBySlug(slug: Slug) = client.get(Api.Events.ReadBySlug, slug)
    suspend fun readEventLocationBySlug(slug: Slug) = client.get(Api.Events.ReadEventLocationBySlug, slug)
    suspend fun readEventFeed() = client.get(Api.Events)
    suspend fun createOrEditEvent(event: EventEdit) = client.postAndReadStatus(Api.Events.Edit, event)
    suspend fun parseMultiEvent(request: ParseRequest) = client.post(Api.Events.ParseMultiEvents, request)
    suspend fun parseSingleEvent(request: ParseRequest) = client.postApi(Api.Events.ParseSingleEvent, request)
    suspend fun readLocationEvents(locationId: LocationId) = client.get(Api.Events.AtLocation, locationId)
    suspend fun readEventLocations(eventIds: List<EventId>) = client.post(Api.Events.ReadEventLocations, eventIds)
    suspend fun readEventLights() = client.get(Api.Events.ReadLights)
    suspend fun editEventLight(edit: LightRequest) = client.post(Api.Events.EditLight, edit)

    // locations
    suspend fun readLocation(locationId: LocationId) = client.get(Api.Locations, locationId)
    suspend fun parseLocation(request: ParseRequest) = client.postApi(Api.Locations.ParseLocation, request)
    suspend fun readLocationsInBounds(bounds: GeoBounds) = client.post(Api.Locations.QueryBounds, bounds)
    suspend fun searchLocations(query: String) = client.get(Api.Locations.Search) {
        param(it.query, query)
    }
    suspend fun createOrEditLocation(location: LocationEdit) = client.post(Api.Locations.CreateOrEdit, location)
    suspend fun queryMap(request: MapQuery) = client.get(Api.Events.QueryMap, request.toQuery())

    // stars
    // suspend fun readUserFiles() = client.get(Api.Users.Files)
    // suspend fun updateUser(user: BasicUserInfo) = client.post(UserApi.Update, user)
    suspend fun checkUsername(username: String) = client.post(UserApi.CheckUsername, username)
    // suspend fun uploadAvatar(blobUrl: Url) = client.uploadBlob(Api.Users.UploadAvatar.path, blobUrl)
    suspend fun uploadImage(blobUrl: Url) = client.uploadBlob(Api.Users.UploadImage, blobUrl)
    suspend fun queryLocation(point: GeoPoint) = client.get(Api.Locations.QueryPoint, point.toQuery())
    suspend fun validateLogin() = client.get(Api.Stars.ValidateLogin)
    suspend fun updateStar(edit: StarEdit) = client.post(Api.Stars.EditStar, edit)

    suspend fun readStoryUrl(url: String) = client.get(Api.Stories.ReadUrl) {
        param(it.url, url)
    }

    // websockets
    fun connectChat(scope: CoroutineScope) = WebChatSocket(client.connectSocket(Api.Chat), scope)
    fun connectSpiritVision() = client.connectSocket(Api.Map.SpiritVision)
    fun connectOmniLog() = client.connectSocket(Api.Omni.Log)
    fun connectTalkLog(stringId: StringId, space: SpaceType) = client.connectSocket(
        Api.Talk.Connect,
        "id" to stringId,
        "space" to space.paramValue
        )

    suspend fun readSongs() = client.get(Api.Songs)
    suspend fun createSong(song: NewSong) = client.post(Api.Songs.Create, song)
    suspend fun readSong(songId: SongId) = client.get(Api.SongProfile, songId)
    suspend fun updateSong(song: Song) = client.post(Api.SongProfile.Update, song)

    // talents
    suspend fun readTalents() = client.get(Api.Users.Talents)
    suspend fun editTalent(talent: TalentEdit) = client.post(Api.Users.EditTalent, talent)

    // galaxies
    suspend fun foundGalaxy(galaxy: GalaxyEdit) = client.post(Api.Galaxies.Found, galaxy)
    suspend fun readTopGalaxies() = client.get(Api.Galaxies.Top)
    suspend fun readGalaxies(galaxyIds: List<GalaxyId>) = client.post(Api.Galaxies.ReadGalaxies, galaxyIds)
    suspend fun readGalaxy(path: String) = client.get(Api.Galaxies.Path, path)
    suspend fun createPost(post: EventPostEdit) = client.postApi(Api.Galaxies.PostEvent, post)
    suspend fun createPost(post: ContentEdit) = client.postApi(Api.Galaxies.PostContent, post)
    suspend fun postLocation(location: LocationPostEdit) = client.postApi(Api.Galaxies.PostLocation, location)
    suspend fun readPosts(galaxyIds: List<GalaxyId>) = client.postApi(Api.Galaxies.ReadMultiPosts, galaxyIds)
    suspend fun readPosts(galaxyId: GalaxyId) = client.getApi(Api.Galaxies.ReadPosts, galaxyId)
    suspend fun readPost(postId: PostId) = client.getApi(Api.Galaxies.ReadPost, postId)
    suspend fun readGalaxyLights() = client.get(Api.Galaxies.ReadLights)
    suspend fun editGalaxyLight(edit: LightRequest) = client.post(Api.Galaxies.EditLight, edit)
    suspend fun removePost(postId: PostId) = client.postApi(Api.Galaxies.RemovePost, postId)

    suspend fun readStarByUsername(username: String) = client.get(Api.Stars.ReadByUsername) {
        param(it.username, username)
    }

    // docs
    suspend fun readSiteDoc(docId: DocId) = client.get(Api.Docs, docId)
    suspend fun readSiteDocTable() = client.get(Api.SiteDocTable)

    // talk
    suspend fun createComment(comment: NewComment) = client.postApi(Api.Talk.CreateComment, comment)
    suspend fun updateComment(comment: UpdatedComment) = client.postApi(Api.Talk.UpdateComment, comment)
}

fun <T> ApiResponse<T>?.getDataOrNull() = when (this) {
    is Ok -> this.data
    is Problem -> null.also { console.log("Problem: ${this.message}") }
    null -> null.also { console.log("Response was null") }
}

fun <T> ApiResponse<T>?.getDataOrNull(onMessage: (String) -> Unit) = when (this) {
    is Ok -> data
    is Problem -> onMessage(message)
    null -> onMessage("No response.")
}

fun <T> ApiResponse<T>?.handleResponse(onMessage: (String) -> Unit, block: (T) -> Unit) {
    when (this) {
        is Ok -> block(data)
        is Problem -> onMessage(message)
        null -> onMessage("No response.")
    }
}