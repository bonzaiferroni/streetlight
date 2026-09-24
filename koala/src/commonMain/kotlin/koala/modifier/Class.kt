package koala.modifier

import koala.html.Queryable
import kotlin.jvm.JvmInline

/** A CSS class, applied to an element and used as a selector. */
@JvmInline
value class Class(val identifier: String): Modifier, Queryable {
    override val selector get() = ".$identifier"

    /** The class name quoted as a JS string. */
    val jsLiteral get() = "'$identifier'"

    override fun toString() = selector

    /** The BEM element class `block__name` of this block class. */
    fun withBemElement(name: String) = Class("${identifier}__$name")
    /** The BEM modifier class `block--name` of this class. */
    fun withBemModifier(name: String) = Class("${identifier}--$name")
}
