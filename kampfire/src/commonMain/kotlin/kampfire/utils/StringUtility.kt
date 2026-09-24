package kampfire.utils

import kampfire.api.Password

/** The text cut to [length] characters, with [ellipsis] appended when it was cut. */
fun String.takeEllipsis(length: Int, ellipsis: String = "...") = when {
    this.length > length -> take(length) + ellipsis
    else -> this
}

/** The plural suffix for [value]: "s", or nothing for one. */
fun pluralize(value: Int) = if (value == 1) "" else "s"
/** The word, with an "s" unless [value] is one. */
fun String.pluralize(value: Int) = if (value == 1) this else "${this}s"
/** The word, with an "s" unless [value] is one. */
fun String.pluralize(value: Float) = if (value == 1f) this else "${this}s"

/** [input] as a file name: lowercase, with every other character replaced by `_`, and cut to 64 characters. */
fun toFilenameFormat(input: String) = input
    .take(64).lowercase()
    .replace(Regex("[^A-Za-z0-9]"), "_")

private val pascalRegex = Regex("([a-z])([A-Z])")
private val nonAlphanumericRegex = Regex("[^a-z0-9 ]")
private val whitespaceRegex = Regex(" +")
fun String.pascalToSnakeCase(): String = replace(pascalRegex, "$1_$2").lowercase()
fun String.pascalToKebabCase(): String = replace(pascalRegex, "$1-$2").lowercase()
/** PascalCase text as words, as "SiteMonitor" to "Site Monitor". */
fun String.pascalToTitle(): String = replace(pascalRegex, "$1 $2")
/** snake_case text as capitalized words. */
fun String.snakeToTitleCase(): String = split("_").joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }

/** Words as snake_case, dropping any character that is not a letter or digit. */
fun String.titleToSnakeCase(): String =
    trim().lowercase().replace(nonAlphanumericRegex, "").replace(whitespaceRegex, "_")
/** Words as kebab-case, dropping any character that is not a letter or digit. */
fun String.titleToKebabCase(): String =
    trim().lowercase().replace(nonAlphanumericRegex, "").replace(whitespaceRegex, "-")