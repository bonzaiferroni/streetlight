package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Url
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun Url.normalize(): Url {
    val noFragment = value.substringBefore('#')
    val schemeEnd = noFragment.indexOf("://")
    if (schemeEnd < 0) return Url(noFragment)

    val scheme = noFragment.take(schemeEnd).lowercase()
    val rest = noFragment.substring(schemeEnd + 3)

    val authorityEnd = rest.indexOfFirst { it == '/' || it == '?' }
    val authority = if (authorityEnd < 0) rest else rest.take(authorityEnd)
    val tail = if (authorityEnd < 0) "" else rest.substring(authorityEnd)

    val path = normalizePath(tail.substringBefore('?'))
    val query = normalizeQuery(tail.substringAfter('?', ""))

    return Url(buildString {
        append(scheme).append("://").append(normalizeAuthority(authority, scheme))
        append(path)
        if (query.isNotEmpty()) append('?').append(query)
    })
}

private fun normalizeAuthority(authority: String, scheme: String): String {
    // userinfo is case-sensitive; only the host half gets lowered
    val at = authority.lastIndexOf('@')
    val userInfo = if (at < 0) "" else authority.take(at + 1)
    val hostPort = authority.substring(at + 1).lowercase().trimEnd('.')

    val bare = when {
        scheme == "http" && hostPort.endsWith(":80") -> hostPort.dropLast(3)
        scheme == "https" && hostPort.endsWith(":443") -> hostPort.dropLast(4)
        else -> hostPort
    }
    return userInfo + bare
}

private fun normalizePath(raw: String): String {
    if (raw.isEmpty()) return "/"

    val segments = mutableListOf<String>()
    for (segment in raw.split('/')) {
        when (segment) {
            "", "." -> {}
            ".." -> segments.removeLastOrNull()
            else -> segments.add(normalizeEncoding(segment))
        }
    }
    return "/" + segments.joinToString("/")
}

private fun normalizeQuery(raw: String): String {
    if (raw.isEmpty()) return ""

    return raw.split('&')
        .filter { it.isNotEmpty() }
        .map { it.substringBefore('=') to it.substringAfter('=', "") }
        .filterNot { (name, _) -> isFluff(name) }
        .map { (name, value) -> normalizeEncoding(name) to normalizeEncoding(value) }
        .sortedWith(compareBy({ it.first }, { it.second }))
        .joinToString("&") { (name, value) ->
            if (value.isEmpty()) name else "$name=$value"
        }
}

private const val UNRESERVED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"

private fun normalizeEncoding(raw: String): String = buildString {
    var i = 0
    while (i < raw.length) {
        val c = raw[i]
        if (c == '%' && i + 2 < raw.length) {
            val hex = raw.substring(i + 1, i + 3)
            val code = hex.toIntOrNull(16)
            val decoded = code?.toChar()
            if (decoded != null && decoded in UNRESERVED) append(decoded)
            else append('%').append(hex.uppercase())
            i += 3
        } else {
            append(c)
            i++
        }
    }
}

private val fluffPrefixes = listOf(
    "utm_", "ga_", "_ga", "_gl", "pk_", "piwik_", "matomo_",
    "mc_", "mkt_", "hsa_", "_hs", "vero_", "oly_", "wt_", "at_",
)

private val fluffParams = setOf(
    "fbclid", "gclid", "gbraid", "wbraid", "dclid", "msclkid",
    "yclid", "twclid", "ttclid", "igshid", "igsh", "mkt_tok",
    "s_kwcid", "cmpid", "campaignid", "spm", "scm",
    "ref", "ref_src", "ref_url", "referrer", "source",
    "ei", "ved", "usg", "sca_esv",
)

private fun isFluff(name: String): Boolean {
    val lower = name.lowercase()
    return lower in fluffParams || fluffPrefixes.any { lower.startsWith(it) }
}

