package kampfire.utils

fun String.obfuscate() = map { it.code.xor('s'.code).toChar() }.joinToString("")
fun String.deobfuscate() = map { it.code.xor('s'.code).toChar() }.joinToString("")

fun String.takeEllipsis(length: Int, ellipsis: String = "...") = when {
    this.length > length -> take(length) + ellipsis
    else -> this
}

fun pluralize(value: Int) = if (value == 1) "" else "s"
fun String.pluralize(value: Int) = if (value == 1) this else "${this}s"
fun String.pluralize(value: Float) = if (value == 1f) this else "${this}s"

fun toFilenameFormat(input: String) = input
    .take(64).lowercase()
    .replace(Regex("[^A-Za-z0-9]"), "_")

val pascalRegex = Regex("([a-z])([A-Z])")
fun String.pascalToKebabCase(): String = replace(pascalRegex, "$1-$2").lowercase()
fun String.pascalToSnakeCase(): String = replace(pascalRegex, "$1_$2").lowercase()