package koala.html

import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.http.formUrlEncode

fun buildRelativePath(nodes: List<String>, extra: Any? = null) = buildString {
    nodes.forEach {
        append('/')
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
            append("/")
            append(extra)
        }
    }
}

fun buildRelativePath(nodes: List<String>, block: ParametersBuilder.() -> Unit) =
    buildRelativePath(nodes, Parameters.build(block))

fun <T> Parameters.getAll(name: String, toValue: (String) -> T?) = getAll(name)
    ?.mapNotNull(toValue)
    ?.takeIf { it.isNotEmpty() }