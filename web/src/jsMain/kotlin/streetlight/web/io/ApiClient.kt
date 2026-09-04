package streetlight.web.io

import kampfire.api.EmailAddress
import kampfire.api.Slug
import kampfire.api.UserApi
import kampfire.api.Username
import kampfire.model.AccountUpgradeRequest
import kampfire.model.EmailChange
import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.LoginRequest
import kampfire.model.PasswordChange
import kampfire.model.PasswordVerification
import kampfire.model.SignUpRequest
import kampfire.model.Url
import koala.Image
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
    suspend fun readLocationConfigContent(locationId: LocationId) = client.getApi(Api.Locations.ReadConfigContent, locationId)
    suspend fun parseEventSchema(url: Url) = client.postApi(Api.Locations.ParseEventSchema, url)
    suspend fun updateLocationConfig(edit: LocationConfig) = client.postApi(Api.Locations.UpdateConfig, edit)
    suspend fun uploadSchemas(schemas: UrlSchemas) = client.postApi(Api.Locations.UploadSchemas, schemas)

    suspend fun createLocation(location: LocationEdit) = client.postApi(Api.Locations.CreateLocation, location)
    suspend fun updateLocation(location: LocationEdit) = client.postApi(Api.Locations.UpdateLocation, location)
    suspend fun queryMap(request: MapQuery) = client.getApi(Api.Events.QueryMap, request.toQuery())
    suspend fun configSubdomain(config: SubdomainConfig) = client.postApi(Api.Locations.UpdateSubdomain, config)

    // stars
    // suspend fun readUserFiles() = client.get(Api.Users.Files)
    suspend fun readPendingEdits() = client.getApi(Api.Stars.PendingEdits)
    suspend fun readUserTasks() = client.getApi(Api.Tasks.ReadStarTasks)
    suspend fun readReview(taskId: TaskId) = client.getApi(Api.Tasks.ReadStarTask, taskId)
    suspend fun readStarContent(username: Username) = client.getApi(Api.Stars.ReadStarContent) {
        writeParam(it.username, username)
    }
    suspend fun readAccount() = client.getApi(Api.Stars.ReadAccount)
    suspend fun uploadImageBlob(blobImage: Image) = client.uploadBlob(Api.Users.UploadImage.path, blobImage)
    suspend fun queryLocation(point: GeoPoint) = client.getApi(Api.Locations.QueryPoint, point.toQuery())
    suspend fun validateLogin() = client.getApi(Api.Stars.ValidateLogin)
    suspend fun login(request: LoginRequest) = client.postApi(UserApi.Login, request)
    suspend fun updateProfile(edit: StarEdit) = client.postApi(Api.Stars.UpdateProfile, edit)
    suspend fun editLight(edit: EditLightRequest) = client.postApi(Api.Stars.EditLight, edit)
    suspend fun readProfileDesign() = client.getApi(Api.Stars.ReadProfileConfig)

    // messages
    suspend fun sendMessage(message: NewMessage) = client.postApi(Api.Messages.SendNew, message)
    suspend fun sendMessage(message: ReplyMessage) = client.postApi(Api.Messages.SendReply, message)
    suspend fun readInbox() = client.getApi(Api.Messages.Inbox)
    suspend fun readChatMessages(request: ChatMessageRequest) = client.postApi(Api.Messages.ReadChatMessages, request)
    suspend fun readChats(request: ChatRequest) = client.postApi(Api.Messages.ReadChats, request)
    suspend fun archiveChat(chatId: ChatId) = client.postApi(Api.Messages.ArchiveChat, chatId)
    suspend fun unarchiveChat(chatId: ChatId) = client.postApi(Api.Messages.UnarchiveChat, chatId)
    suspend fun readChatPreview(chatId: ChatId) = client.getApi(Api.Messages.ReadChatPreview, chatId)

    // account actions
    suspend fun verifyExistingEmail() = client.postApi(Api.AccountAction.VerifyExistingEmail)
    suspend fun readEmailVerificationIsSent() = client.getApi(Api.AccountAction.VerifyExistingEmail.CheckStatus)
    suspend fun removeEmail(password: PasswordVerification) = client.postApi(Api.AccountAction.RemoveEmail, password)
    suspend fun resetPassword(email: EmailAddress) = client.postApi(Api.AccountAction.ResetPassword, email)
    suspend fun addEmail(value: EmailChange) = client.postApi(Api.AccountAction.ChangeEmail, value)
    suspend fun changePassword(value: PasswordChange) = client.postApi(Api.AccountAction.ChangePassword, value)

    // account
    suspend fun checkGuest() = client.getApi(UserApi.Login.CheckGuest)
    suspend fun upgradeAccount(request: AccountUpgradeRequest) = client.postApi(UserApi.AccountUpgrade, request)
    suspend fun logout() = client.postApi(UserApi.Logout, Unit)
    suspend fun checkUsernameExists(username: Username) = client.postApi(UserApi.CheckUsernameExists, username)
    suspend fun generateUsername() = client.getApi(UserApi.GenerateUsername)

    // websockets
    fun connectChat(scope: CoroutineScope) = WebChatSocket(client.connectSocket(Api.GroupChat), scope)
    fun connectSpiritVision() = client.connectSocket(Api.Map.SpiritVision)
    fun connectOmniLog() = client.connectSSE(Api.Omni.Log)
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
    suspend fun createGalaxy(galaxy: GalaxyEdit) = client.postApi(Api.Galaxies.CreateGalaxy, galaxy)
    suspend fun updateGalaxy(galaxy: GalaxyEdit) = client.postApi(Api.Galaxies.UpdateGalaxy, galaxy)
    suspend fun readTopGalaxies() = client.getApi(Api.Galaxies.Top)
    suspend fun readGalaxies(galaxyIds: List<GalaxyId>) = client.postApi(Api.Galaxies.ReadGalaxies, galaxyIds)
    suspend fun readUserGalaxies() = client.getApi(Api.Galaxies.ReadUserGalaxies)
    suspend fun readGalaxy(slug: Slug) = client.getApi(Api.Galaxies.ReadGalaxySlug, slug)
    suspend fun readGalaxyContent(slug: Slug) = client.getApi(Api.Galaxies.ReadContent, slug)
    suspend fun readGalaxy(galaxyId: GalaxyId) = client.getApi(Api.Galaxies.ReadGalaxyId, galaxyId)
    suspend fun createPost(post: PostEdit) = client.postApi(Api.Galaxies.CreatePost, post)
    suspend fun editPost(post: PostEdit) = client.postApi(Api.Galaxies.UpdatePost, post)
    suspend fun readPosts(galaxyIds: List<GalaxyId>) = client.postApi(Api.Galaxies.ReadMultiPosts, galaxyIds)
    suspend fun readPosts(galaxyId: GalaxyId) = client.getApi(Api.Galaxies.ReadPosts, galaxyId)
    suspend fun readPost(postId: PostId) = client.getApi(Api.Galaxies.ReadPostId, postId)
    suspend fun readGalaxyLights() = client.getApi(Api.Galaxies.ReadLights)
    suspend fun removePost(postId: PostId) = client.postApi(Api.Galaxies.RemovePost, postId)

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

    // feedback
    suspend fun feedFeedback() = client.getApi(Api.Feedback.Feed)
    suspend fun createFeedback(edit: FeedbackEdit) = client.postApi(Api.Feedback.Create, edit)

    // site status
    suspend fun feedSiteStatus(resolution: MetricResolution) = client.getApi(Api.Status.Feed) {
        writeParam(it.resolution, resolution)
    }
    suspend fun readLastSiteStatus(resolution: MetricResolution) = client.getApi(Api.Status.ReadLast) {
        writeParam(it.resolution, resolution)
    }
}

