package streetlight.web.model

import streetlight.model.music.Clip
import streetlight.model.music.ClipId
import streetlight.web.utility.IdbDatabase
import streetlight.web.utility.IdbStore

/**
 * The clips a user plays, kept on the device whether or not they have an account, beside their audio in
 * [ClipLibrary]. A shared clip's [Clip.url] is its public home.
 */
class ClipDb(db: IdbDatabase) {
    private val store = IdbStore(db, CLIP_STORE, Clip.serializer()) { it.clipId.key }

    suspend fun readClip(clipId: ClipId) = store.read(clipId.key)
    suspend fun readClips() = store.readAll()
    suspend fun saveClip(clip: Clip) = store.write(clip)
    suspend fun removeClip(clipId: ClipId) = store.remove(clipId.key)
}

private val ClipId.key get() = value.toString()
