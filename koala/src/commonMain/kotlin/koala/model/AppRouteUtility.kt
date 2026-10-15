package koala.model

import io.ktor.http.Parameters
import kampfire.api.PathBuilder
import io.ktor.http.formUrlEncode

/** The route's [AppRoute.basePath] followed by [nodes], then [extra] as query [Parameters] or a last segment. */
fun AppRoute.buildRelativePath(nodes: List<String> = emptyList(), extra: Any? = null) = buildString {
    append(basePath)
    nodes.forEach {
        if (!endsWith('/')) append('/')
        append(it)
    }
    if (extra == null) return@buildString

    when (extra is Parameters) {
        true -> {
            if (extra.isEmpty()) return@buildString
            append('?')
            append(extra.formUrlEncode())
        }
        else -> {
            if (!endsWith('/')) append('/')
            append(extra)
        }
    }
}

/** The route's [AppRoute.basePath] followed by [nodes], with the query parameters [block] writes. */
fun AppRoute.buildRelativePath(nodes: List<String> = emptyList(), block: PathBuilder.() -> Unit) =
    PathBuilder(buildRelativePath(nodes)).apply(block).build()

fun <T> Parameters.getAll(name: String, toValue: (String) -> T?) = getAll(name)
    ?.mapNotNull(toValue)
    ?.takeIf { it.isNotEmpty() }