package koala.html

import kotlinx.html.*
import kotlin.jvm.JvmInline

@JvmInline
value class Id(val value: String) {
    val selector get() = "#$value"
}

fun CoreAttributeGroupFacade.applyId(id: Id?) {
    id?.let {
        this.id = id.value
    }
}