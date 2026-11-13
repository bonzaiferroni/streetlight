package streetlight.model.io

import kampfire.api.ApiCall
import kampfire.api.ApiTransport
import kampfire.api.getApi
import kampfire.api.postApi
import kampfire.model.Outcome
import kotlinx.serialization.serializer
import streetlight.model.Api
import streetlight.model.music.Mix
import streetlight.model.music.MixId
import streetlight.model.music.MusicId

/** The calls of `Api.Mixes`, the mixes of the signed-in user. */
class MixClient(private val transport: ApiTransport) {
    suspend fun readMixes(musicId: MusicId): Outcome<List<Mix>> = transport.getApi(Api.Mixes.ByMusic, musicId)
    suspend fun saveMix(mix: Mix): Outcome<Boolean> = transport.postApi(Api.Mixes.Save, mix)
    suspend fun removeMix(mixId: MixId): Outcome<Boolean> =
        transport.send(ApiCall(Api.Mixes.Delete, serializer(), sent = mixId, sentSerializer = serializer()))
}
