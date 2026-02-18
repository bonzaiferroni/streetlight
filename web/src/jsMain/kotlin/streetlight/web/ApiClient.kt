package streetlight.web

import kampfire.model.GeoPoint
import kotlinx.coroutines.CoroutineScope
import streetlight.model.Api
import streetlight.model.data.*

class ApiClient(app: AppContext): AppContext by app {
    suspend fun readEvent(eventId: EventId) = get(Api.EventProfile, eventId)
    suspend fun readLocation(locationId: LocationId) = get(Api.LocationFeed, locationId)
    suspend fun readEventFeed() = get(Api.Events)
    suspend fun queryMap(request: MapQuery) = get(Api.Events.QueryMap, request.toQuery())
    suspend fun create(event: EventUpdate) = post(Api.Events.Create, event)
    suspend fun uploadFeatureImage(blobUrl: String) = uploadBlob(Api.Events.Upload.path, blobUrl)

    suspend fun readUserFiles(request: UserFileRequest) = post(Api.Users.Images, request)

    suspend fun createLocation(newLocation: NewLocation) = post(Api.LocationFeed.Create, newLocation)
    suspend fun queryLocation(point: GeoPoint) = get(Api.LocationFeed.QueryPoint, point.toQuery())

    suspend fun readStoryUrl(url: String) = get(Api.Stories.ReadUrl) {
        param(it.url, url)
    }

    fun connectChat(scope: CoroutineScope) = WebChatSocket(connectSocket(Api.Chat.path), scope)

    suspend fun readSongs() = get(Api.Songs)
    suspend fun createSong(song: NewSong) = post(Api.Songs.Create, song)
    suspend fun readSong(songId: SongId) = get(Api.SongProfile, songId)
    suspend fun updateSong(song: Song) = post(Api.SongProfile.Update, song)
}