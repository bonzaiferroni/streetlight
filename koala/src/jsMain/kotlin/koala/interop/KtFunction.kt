package koala.interop

import koala.core.globalThis
import kotlin.reflect.KFunction

/** A Kotlin [function] exposed to markup under the name of its [signature]. */
data class KtFunction(val signature: JsSignature, val function: KFunction<*>)

/** Adds each function to `globalThis`, so markup can call it by name. */
fun addGlobalFunctions(vararg functions: KtFunction) = addGlobalFunctions(functions.asList())

/** Adds each function to `globalThis`, so markup can call it by name. */
fun addGlobalFunctions(functions: List<KtFunction>) {
    functions.forEach {
        println("adding ${it.signature.name}")
        globalThis[it.signature.name] = it.function
    }
}