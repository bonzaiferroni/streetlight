package streetlight.web.utility

import js.typedarrays.toUint8Array
import web.blob.Blob
import web.blob.BlobPropertyBag
import web.dom.document
import web.html.HTMLAnchorElement
import web.url.URL

/** Hands the viewer [bytes] to save as a file named [fileName], of the media [type]. */
fun downloadFile(bytes: ByteArray, fileName: String, type: String) {
    val url = URL.createObjectURL(Blob(arrayOf(bytes.toUint8Array()), BlobPropertyBag(type = type)))
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = url
    anchor.download = fileName
    anchor.click()
    URL.revokeObjectURL(url)
}
