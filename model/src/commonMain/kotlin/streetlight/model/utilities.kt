package streetlight.model

/** Scrambles the text so it does not travel plainly. [deobfuscate] reverses it. */
fun String.obfuscate(): String {
    return this.map { it.code.xor('s'.code).toChar() }.joinToString("")
}

/** Reads text written by [obfuscate]. */
fun String.deobfuscate(): String {
    return this.map { it.code.xor('s'.code).toChar() }.joinToString("")
}