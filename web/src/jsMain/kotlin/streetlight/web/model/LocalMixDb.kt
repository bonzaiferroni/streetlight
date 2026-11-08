package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import streetlight.model.io.MixDb
import streetlight.model.music.Mix
import streetlight.model.music.MixId
import streetlight.model.music.MusicId
import streetlight.web.utility.IdbDatabase
import streetlight.web.utility.IdbStore

/** The mixes kept on the device, for a user without an account. */
class LocalMixDb(db: IdbDatabase): MixDb {
    private val store = IdbStore(db, MIX_STORE, Mix.serializer()) { it.mixId.key }

    override suspend fun readMixes(musicId: MusicId) = outcomeOf { store.readAll().filter { it.musicId == musicId } }
    override suspend fun saveMix(mix: Mix) = outcomeOf { store.write(mix); true }
    override suspend fun removeMix(mixId: MixId) = outcomeOf { store.remove(mixId.key); true }
}

/** [Ok] with what [block] gives, or a [Problem] when the browser fails it. */
private inline fun <T> outcomeOf(block: () -> T): Outcome<T> = try {
    Ok(block())
} catch (error: Throwable) {
    Problem(error.message ?: "The device storage failed")
}

private val MixId.key get() = value.toString()
