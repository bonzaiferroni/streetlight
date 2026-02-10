package streetlight.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.contentType
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import kampfire.model.UserId
import streetlight.model.data.FileFormat
import streetlight.model.data.FileType
import streetlight.model.data.FileUse
import streetlight.model.data.UserFile
import streetlight.model.data.UserFileId
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import java.io.File

suspend fun RoutingContext.uploadUserImage(
    bytes: ByteArray,
    userId: UserId,
    fileUse: FileUse,
    app: ServerProvider = RuntimeProvider
): String? {
    if (bytes.isEmpty()) {
        call.respond(HttpStatusCode.BadRequest, "Empty body")
        return null
    }

    // Size guard (example: 10 MB)
    val maxBytes = 10 * 1024 * 1024
    if (bytes.size > maxBytes) {
        call.respond(HttpStatusCode.PayloadTooLarge, "Too large")
        return null
    }

    val fileFormat = detectFileTypeFromImage(bytes)
    if (fileFormat == null) {
        call.respond(HttpStatusCode.UnsupportedMediaType, "Not a supported image")
        return null
    }

    // Optional: compare with declared Content-Type (don’t trust it, just sanity check)
    val declared = call.request.contentType()
    if (declared.contentType.isNotBlank() && declared.contentType != "image") {
        call.respond(HttpStatusCode.UnsupportedMediaType, "Declared Content-Type not image")
        return null
    }

    val fileId = UserFileId.random()

    val name = "${fileId.value}.${fileFormat.ext}"
    val file = File(uploadFolder, name)
    val url = "${uploadFolder.name}/$name"
    file.writeBytes(bytes)

    app.dao.userFile.create(
        UserFile(
            userFileId = fileId,
            userId = userId,
            url = url,
            fileType = FileType.Image,
            fileUse = fileUse,
            fileFormat = fileFormat
        )
    )

    return url
}

private fun detectFileTypeFromImage(bytes: ByteArray): FileFormat? {
    fun has(prefix: ByteArray): Boolean =
        bytes.size >= prefix.size && prefix.indices.all { i -> bytes[i] == prefix[i] }

    // PNG: 89 50 4E 47 0D 0A 1A 0A
    if (has(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))) return FileFormat.PNG
    // JPEG: FF D8 FF
    if (bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) return FileFormat.JPEG
    // GIF: "GIF87a" or "GIF89a"
    if (bytes.size >= 6) {
        val s = bytes.copyOfRange(0, 6).decodeToString()
        if (s == "GIF87a" || s == "GIF89a") return FileFormat.GIF
    }
    // WEBP: "RIFF" .... "WEBP"
    if (bytes.size >= 12) {
        val riff = bytes.copyOfRange(0, 4).decodeToString()
        val webp = bytes.copyOfRange(8, 12).decodeToString()
        if (riff == "RIFF" && webp == "WEBP") return FileFormat.WEBP
    }
    // BMP: "BM"
    if (bytes.size >= 2 && bytes[0] == 0x42.toByte() && bytes[1] == 0x4D.toByte()) return FileFormat.BMP

    return null
}