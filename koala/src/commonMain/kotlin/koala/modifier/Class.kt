package koala.modifier

import koala.html.Queryable
import kotlin.jvm.JvmInline

@JvmInline
value class Class(override val identifier: String): ClassModifier, Queryable {
    override val selector get() = ".$identifier"

    val jsLiteral get() = "'$identifier'"

    override fun toString() = selector

    fun withBemElement(name: String) = Class("${identifier}__$name")
    fun withBemModifier(name: String) = Class("${identifier}--$name")
}

interface ClassModifier: Modifier {
    val selector: String
    val identifier: String
}