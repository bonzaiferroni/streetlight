package streetlight.app.io

import pondui.io.ApiStore
import streetlight.model.Api
import streetlight.model.data.NewSong

class SongStore: ApiStore() {
    suspend fun readSongs() = client.get(Api.Songs)
    suspend fun createSong(newSong: NewSong) = client.post(Api.Songs.Create, newSong)
}