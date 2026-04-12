package koala.css

class Fun(val identifier: String, vararg val params: String) {
    override fun toString() = "$identifier($paramsExpression)"

    val paramsExpression get() = params.joinToString(", ")

    fun invoke(vararg args: String) = args.joinToString(", ").let {
        "$identifier($it)"
    }
}

//data class Fun1(val identifier: String, val param1: Any) {
//    override fun toString() = "$identifier()"
//
//    val invocation get() = "$identifier($param1)"
//}