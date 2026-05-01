package koala.css

import koala.html.Id

class Fun(val identifier: String, vararg val params: String) {
    override fun toString() = signature

    val paramsExpression get() = params.joinToString(", ")
    val signature get() = "function $identifier($paramsExpression)"

    fun invoke(vararg args: Any) = args.joinToString(", ", "$identifier(", ")") { arg ->
        when (arg) {
            is This -> "this"
            is String -> "'$arg'"
            is Id -> "'${arg.identifier}'"
            else -> arg.toString()
        }
    }
}

//data class Fun1(val identifier: String, val param1: Any) {
//    override fun toString() = "$identifier()"
//
//    val invocation get() = "$identifier($param1)"
//}

// args.joinToString(", ").let {
//        "$identifier($it)"
//    }