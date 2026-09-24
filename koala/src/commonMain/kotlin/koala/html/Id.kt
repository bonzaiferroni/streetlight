package koala.html

import koala.modifier.PositionAnchor
import kotlinx.html.*
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** The id of an element, used to render it and to select it. */
@JvmInline
@Serializable
value class Id(val identifier: String): Queryable {
    override val selector get() = "#$identifier"
    override fun toString() = selector

    /** The id quoted as a JS string. */
    val jsLiteral get() = "'$identifier'"

    /** The anchor name a popover positions itself against, derived from the id. */
    fun toPositionAnchor() = PositionAnchor("$identifier-anchor")
}

/** Anything that can select elements with a CSS [selector]. */
interface Queryable {
    val selector: String
}

/** Sets the element's id, or nothing when [id] is `null`. */
fun CoreAttributeGroupFacade.setId(id: Id?) {
    id?.let {
        this.id = id.identifier
    }
}

/** A selector made of others joined without a space, matching an element that meets them all. */
@JvmInline
value class CompoundSelector(override val selector: String) : Queryable

/** A [CompoundSelector] of both selectors. */
operator fun Queryable.plus(other: Queryable): Queryable =
    CompoundSelector(selector + other.selector)