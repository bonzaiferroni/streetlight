package kampfire.utils

import kampfire.api.Password

fun String.obfuscate() = map { it.code.xor('s'.code).toChar() }.joinToString("")
fun String.deobfuscate() = Password(map { it.code.xor('s'.code).toChar() }.joinToString(""))

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

private val pascalRegex = Regex("([a-z])([A-Z])")
private val nonAlphanumericRegex = Regex("[^a-z0-9 ]")
private val whitespaceRegex = Regex(" +")
fun String.pascalToSnakeCase(): String = replace(pascalRegex, "$1_$2").lowercase()
fun String.pascalToKebabCase(): String = replace(pascalRegex, "$1-$2").lowercase()
fun String.pascalToTitle(): String = replace(pascalRegex, "$1 $2")
fun String.snakeToTitleCase(): String = split("_").joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }

fun String.titleToSnakeCase(): String =
    trim().lowercase().replace(nonAlphanumericRegex, "").replace(whitespaceRegex, "_")
fun String.titleToKebabCase(): String =
    trim().lowercase().replace(nonAlphanumericRegex, "").replace(whitespaceRegex, "-")