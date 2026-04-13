package koala.html

import koala.css.PositionAnchor
import kotlinx.html.*
import kotlin.jvm.JvmInline

@JvmInline
value class Id(val identifier: String): Queryable {
    override val selector get() = "#$identifier"
    override fun toString() = selector

    val arg get() = "'$identifier'"

    fun toPositionAnchor() = PositionAnchor("$identifier-anchor")
}

interface Queryable {
    val selector: String
}

fun CoreAttributeGroupFacade.setId(id: Id?) {
    id?.let {
        this.id = id.identifier
    }
}