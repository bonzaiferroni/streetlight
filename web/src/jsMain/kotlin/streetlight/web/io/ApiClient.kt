package streetlight.web.io

import kampfire.api.UserApi
import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.SignUpRequest
import kotlinx.coroutines.CoroutineScope
import streetlight.model.Api
import streetlight.model.data.*

class ApiClient(private val client: FetchClient) {
    suspend fun createUser(request: SignUpRequest) = client.post(UserApi.Create, request)
    suspend fun readUserInfo() = client.get(UserApi.ReadInfo)

    // events
    suspend fun readEvent(eventId: EventId) = client.get(Api.EventProfile, eventId)
    suspend fun readEventFeed() = client.get(Api.Events)
    suspend fun createOrEditEvent(event: EventEdit) = client.postAndReadStatus(Api.Events.Edit, event)
    suspend fun parseMultiEventFromUrl(request: ParseRequest) = client.post(Api.Events.ParseEvents, request)
    suspend fun readLocationEvents(locationId: LocationId) = client.get(Api.Events.AtLocation, locationId)

    // locations
    suspend fun readLocation(locationId: LocationId) = client.get(Api.Locations, locationId)
    suspend fun parseLocation(request: ParseRequest) = client.post(Api.Locations.ParseLocation, request)
    suspend fun readLocationsInBounds(bounds: GeoBounds) = client.post(Api.Locations.QueryBounds, bounds)

    suspend fun queryMap(request: MapQuery) = client.get(Api.Events.QueryMap, request.toQuery())
    suspend fun uploadFile(blobUrl: String) = client.uploadBlob(Api.Events.Upload.path, blobUrl)

    suspend fun readUserFiles() = client.get(Api.Users.Files)

    suspend fun createLocation(place: Place) = client.post(Api.Locations.Create, place)
    suspend fun createOrEditLocation(location: LocationEdit) = client.post(Api.Locations.Edit, location)
    suspend fun queryLocation(point: GeoPoint) = client.get(Api.Locations.QueryPoint, point.toQuery())

    suspend fun readStoryUrl(url: String) = client.get(Api.Stories.ReadUrl) {
        param(it.url, url)
    }

    // websockets
    fun connectChat(scope: CoroutineScope) = WebChatSocket(client.connectSocket(Api.Chat.path), scope)
    fun connectSpiritVision() = client.connectSocket(Api.Map.SpiritVision.path)

    suspend fun readSongs() = client.get(Api.Songs)
    suspend fun createSong(song: NewSong) = client.post(Api.Songs.Create, song)
    suspend fun readSong(songId: SongId) = client.get(Api.SongProfile, songId)
    suspend fun updateSong(song: Song) = client.post(Api.SongProfile.Update, song)

    // talents
    suspend fun readTalents() = client.get(Api.Users.Talents)
    suspend fun editTalent(talent: TalentEdit) = client.post(Api.Users.EditTalent, talent)
}