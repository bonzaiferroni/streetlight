package streetlight.compose.io

import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.fillDefaults
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.parseServerSetCookieHeader
import io.ktor.http.renderSetCookieHeader
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The app's cookies, kept in an app-data file so a session outlives the app.
 *
 * With [relaxSecure], a cookie is kept without its `secure` flag, so a server on plain `http` gets back the
 * session it set, as a browser allows for `localhost`.
 */
class AppCookieStorage(private val relaxSecure: Boolean) : CookiesStorage {
    private val storage = AcceptAllCookiesStorage()
    private val mutex = Mutex()
    private var savedCookies: MutableList<SavedCookie>? = null

    override suspend fun get(requestUrl: Url): List<Cookie> {
        loadCookies()
        return storage.get(requestUrl)
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        val savedCookies = loadCookies()
        val kept = cookie.copy(secure = cookie.secure && !relaxSecure).withExpiry(GMTDate().timestamp)
        storage.addCookie(requestUrl, kept)

        mutex.withLock {
            val filled = kept.fillDefaults(requestUrl)
            savedCookies.removeAll { it.matches(filled) }
            if (!filled.isExpired()) savedCookies.add(SavedCookie(requestUrl.toString(), renderSetCookieHeader(filled)))
            writeAppFile(COOKIE_FILE, Json.encodeToString(savedCookies.toList()))
        }
    }

    override fun close() = storage.close()

    /** The saved cookies, read from the file and added to [storage] on first use. */
    private suspend fun loadCookies(): MutableList<SavedCookie> = mutex.withLock {
        savedCookies ?: run {
            val text = readAppFile(COOKIE_FILE)
            val read = text?.let { runCatching { Json.decodeFromString<List<SavedCookie>>(it) }.getOrNull() }
            val kept = read.orEmpty().filter { !it.cookie.isExpired() }.toMutableList()
            kept.forEach { storage.addCookie(Url(it.requestUrl), it.cookie) }
            kept.also { savedCookies = it }
        }
    }
}

/** A cookie as saved: the URL that set it, and its `Set-Cookie` header. */
@Serializable
private data class SavedCookie(
    val requestUrl: String,
    val header: String,
) {
    val cookie get() = parseServerSetCookieHeader(header)

    /** Whether [other] takes this cookie's place, by name, domain and path. */
    fun matches(other: Cookie) = cookie.let { it.name == other.name && it.domain == other.domain && it.path == other.path }
}

/** The cookie with its `Max-Age` turned into an `Expires` counted from [now], so a reload does not restart it. */
private fun Cookie.withExpiry(now: Long): Cookie {
    val maxAge = maxAge ?: return this
    return copy(maxAge = null, expires = GMTDate(now + maxAge * 1000L))
}

private fun Cookie.isExpired() = expires?.let { it.timestamp <= GMTDate().timestamp } ?: false

private const val COOKIE_FILE = "cookies.json"
