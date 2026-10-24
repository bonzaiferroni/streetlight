package streetlight.compose.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

actual suspend fun readAppFile(name: String): String? = withContext(Dispatchers.IO) {
    val file = appDataDir().resolve(name)
    if (file.exists()) file.readText() else null
}

actual suspend fun writeAppFile(name: String, text: String) = withContext(Dispatchers.IO) {
    val dir = appDataDir().createDirectories()
    val file = dir.resolve(name)
    if (!file.exists()) createOwnerOnly(file)
    file.writeText(text)
}

/** The app's data directory, in the place each desktop OS keeps it. */
private fun appDataDir(): Path {
    val os = System.getProperty("os.name").lowercase()
    val home = System.getProperty("user.home")
    return when {
        "win" in os -> Path(System.getenv("APPDATA") ?: home, "Streetlight")
        "mac" in os -> Path(home, "Library", "Application Support", "Streetlight")
        else -> Path(System.getenv("XDG_DATA_HOME") ?: "$home/.local/share", "streetlight")
    }
}

/** Creates [file] readable and writable by the user alone, or plainly where there are no POSIX permissions. */
private fun createOwnerOnly(file: Path) {
    val isPosix = "posix" in file.fileSystem.supportedFileAttributeViews()
    when {
        isPosix -> Files.createFile(file, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")))
        else -> Files.createFile(file)
    }
}
