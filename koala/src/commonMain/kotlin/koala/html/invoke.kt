package koala.html

/** The JS call of [identifier] with each of [args] quoted as a string. */
fun invoke(identifier: String, vararg args: String) = buildString {
    append(identifier)
    append("('")
    args.forEachIndexed { index, arg ->
        append(arg)
        if (index < args.size - 1) {
            append("', '")
        }
    }
    append("')")
}