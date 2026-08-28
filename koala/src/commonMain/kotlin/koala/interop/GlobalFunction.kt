package koala.interop

import kampfire.api.TableId
import koala.css.Modifier
import koala.css.PositionAnchor
import koala.html.Id
import kotlin.uuid.Uuid

interface GlobalFunction {
    val name: String

    fun invokeJs(vararg args: Any) = args.joinToString(", ", "$name(", ")") { arg ->
        when (arg) {
            is ThisElement -> "this"
            is String, is Uuid -> "'$arg'"
            is Id -> arg.jsLiteral
            is PositionAnchor -> "'${arg.identifier}'"
            is TableId<*> -> "'${arg.value}'"
            is Modifier -> arg.jsLiteral
            else -> arg.toString()
        }
    }
}

data object ThisElement