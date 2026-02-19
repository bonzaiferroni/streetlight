package streetlight.web

import kampfire.api.UserApi
import kampfire.model.GeoPoint
import kampfire.model.SignUpRequest
import kotlinx.coroutines.CoroutineScope
import streetlight.model.Api
import streetlight.model.data.*

class ApiClient(private val client: FetchClient) {
    suspend fun createUser(request: SignUpRequest) = client.post(UserApi.Create, request)
    suspend fun readUserInfo() = client.get(UserApi.ReadInfo)
    suspend fun readEvent(eventId: EventId) = client.get(Api.EventProfile, eventId)
    suspend fun readLocation(locationId: LocationId) = client.get(Api.LocationFeed, locationId)
    suspend fun readEventFeed() = client.get(Api.Events)
    suspend fun queryMap(request: MapQuery) = client.get(Api.Events.QueryMap, request.toQuery())
    suspend fun create(event: EventUpdate) = client.post(Api.Events.Create, event)
    suspend fun uploadFeatureImage(blobUrl: String) = client.uploadBlob(Api.Events.Upload.path, blobUrl)

    suspend fun readUserFiles(request: UserFileRequest) = client.post(Api.Users.Images, request)

    suspend fun createLocation(newLocation: NewLocation) = client.post(Api.LocationFeed.Create, newLocation)
    suspend fun queryLocation(point: GeoPoint) = client.get(Api.LocationFeed.QueryPoint, point.toQuery())

    suspend fun readStoryUrl(url: String) = client.get(Api.Stories.ReadUrl) {
        param(it.url, url)
    }

    fun connectChat(scope: CoroutineScope) = WebChatSocket(client.connectSocket(Api.Chat.path), scope)

    suspend fun readSongs() = client.get(Api.Songs)
    suspend fun createSong(song: NewSong) = client.post(Api.Songs.Create, song)
    suspend fun readSong(songId: SongId) = client.get(Api.SongProfile, songId)
    suspend fun updateSong(song: Song) = client.post(Api.SongProfile.Update, song)
}