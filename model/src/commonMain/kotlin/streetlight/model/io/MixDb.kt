package streetlight.model.io

import kampfire.model.Outcome
import streetlight.model.music.Mix
import streetlight.model.music.MixId
import streetlight.model.music.MusicId

/** The mixes of a user, kept on the device or on the server. */
interface MixDb {
    suspend fun readMixes(musicId: MusicId): Outcome<List<Mix>>
    suspend fun saveMix(mix: Mix): Outcome<Boolean>
    suspend fun removeMix(mixId: MixId): Outcome<Boolean>
}
