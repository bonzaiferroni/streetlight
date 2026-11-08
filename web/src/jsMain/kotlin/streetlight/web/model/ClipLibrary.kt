package streetlight.web.model

import js.typedarrays.Int8Array
import js.typedarrays.toByteArray
import js.typedarrays.toUint8Array
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Tap
import kampfire.model.toDataOr
import kampfire.model.storeOf
import streetlight.model.music.ClipId
import web.blob.arrayBuffer
import web.fs.FileSystemDirectoryHandle
import web.fs.FileSystemGetDirectoryOptions
import web.fs.FileSystemGetFileOptions
import web.fs.createWritable
import web.fs.getDirectoryHandle
import web.fs.getFile
import web.fs.getFileHandle
import web.fs.removeEntry
import web.fs.write
import web.navigator.navigator
import web.storage.getDirectory
import web.storage.persist
import web.streams.close

/**
 * The audio of each clip, kept as a mono Ogg Opus file in the browser's private file system under its [ClipId], and
 * decoded at the sample rate of [audio].
 */
class ClipLibrary(
    private val audio: StudioAudio,
) {
    private var directory: FileSystemDirectoryHandle? = null

    private val persistedStore = storeOf<Boolean?>(null)

    /** Whether the browser keeps the clips when space runs low, or `null` before the first clip is stored. */
    val isPersistedState: Tap<Boolean?> = persistedStore

    /** Encodes [pcm] and stores it as the clip [clipId], in place of any clip it held, or gives the problem. */
    suspend fun write(clipId: ClipId, pcm: PcmSample): Outcome<Unit> {
        val bytes = encodeOggOpus(pcm).toDataOr { return it }
        if (persistedStore.now == null) persistedStore.set(navigator.storage.persist())
        val handle = readDirectory().getFileHandle(clipId.fileName, FileSystemGetFileOptions(create = true))
        val stream = handle.createWritable()
        stream.write(bytes.toUint8Array())
        stream.close()
        return Ok(Unit)
    }

    /** The clip [clipId], or `null` when none is stored or its file cannot be read. */
    suspend fun read(clipId: ClipId): PcmSample? {
        val handle = readFileHandle(clipId.fileName) ?: return null
        val buffer = handle.getFile().arrayBuffer()
        return decodeOggOpus(audio.readContext(), Int8Array(buffer).toByteArray())
    }

    /** Removes the clip [clipId], if one is stored. */
    suspend fun remove(clipId: ClipId) {
        if (readFileHandle(clipId.fileName) == null) return
        readDirectory().removeEntry(clipId.fileName)
    }

    private suspend fun readFileHandle(name: String) = try {
        readDirectory().getFileHandle(name)
    } catch (error: Throwable) {
        if (error.asDynamic().name != NOT_FOUND) throw error
        null
    }

    private suspend fun readDirectory() = directory
        ?: navigator.storage.getDirectory().getDirectoryHandle(CLIP_DIRECTORY, FileSystemGetDirectoryOptions(create = true))
            .also { directory = it }
}

/** The name of the file holding the clip. */
private val ClipId.fileName get() = "$value.opus"

private const val CLIP_DIRECTORY = "clips"
private const val NOT_FOUND = "NotFoundError"
