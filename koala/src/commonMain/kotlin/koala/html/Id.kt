package koala.html

import kotlinx.html.*
import kotlin.jvm.JvmInline

@JvmInline
value class Id(val value: String): Queryable {
    override val selector get() = "#$value"
    override fun toString() = selector
}

interface Queryable {
    val selector: String
}

fun CoreAttributeGroupFacade.setId(id: Id?) {
    id?.let {
        this.id = id.value
    }
}