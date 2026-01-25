package kampfire.utils

fun String.obfuscate(): String {
    return this.map { it.code.xor('s'.code).toChar() }.joinToString("")
}

fun String.deobfuscate(): String {
    return this.map { it.code.xor('s'.code).toChar() }.joinToString("")
}

fun String.takeEllipsis(length: Int, ellipsis: String = "..."): String =
    if (this.length > length) take(length) + ellipsis else this

fun pluralize(value: Int) = if (value == 1) "" else "s"

fun String.pluralize(value: Int) = if (value == 1) this else "${this}s"

fun String.pluralize(value: Float) = if (value == 1f) this else "${this}s"

fun String.toSnakeCase(): String = this.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()

fun toFilenameFormat(input: String): String =
    input
        .take(64).lowercase()
        .replace(Regex("[^A-Za-z0-9]"), "_")