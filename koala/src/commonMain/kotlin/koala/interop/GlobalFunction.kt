package koala.interop

import kampfire.api.TableId
import koala.modifier.Class
import koala.modifier.PositionAnchor
import koala.html.Id
import kotlin.uuid.Uuid

/** A JS function on `globalThis` that markup calls by [name]. */
interface GlobalFunction {
    val name: String

    /**
     * The JS call of this function with [args], as markup writes it in an attribute such as `onclick`.
     *
     * A string is quoted, an [Id] or [Class] is written as its literal, and [ThisElement] is written as `this`.
     */
    fun invokeJs(vararg args: Any) = args.joinToString(", ", "$name(", ")") { arg ->
        when (arg) {
            is ThisElement -> "this"
            is String, is Uuid -> "'$arg'"
            is Id -> arg.jsLiteral
            is PositionAnchor -> "'${arg.identifier}'"
            is TableId<*> -> "'${arg.value}'"
            is Class -> arg.jsLiteral
            else -> arg.toString()
        }
    }
}

/** Stands for the element whose attribute holds the call, written as `this`. */
data object ThisElement