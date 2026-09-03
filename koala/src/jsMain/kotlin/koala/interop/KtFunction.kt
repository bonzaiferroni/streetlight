package koala.interop

import koala.core.globalThis
import kotlin.reflect.KFunction

data class KtFunction(val signature: JsSignature, val function: KFunction<*>)

fun addGlobalFunctions(vararg functions: KtFunction) = addGlobalFunctions(functions.asList())

fun addGlobalFunctions(functions: List<KtFunction>) {
    functions.forEach {
        println("adding ${it.signature.name}")
        globalThis[it.signature.name] = it.function
    }
}