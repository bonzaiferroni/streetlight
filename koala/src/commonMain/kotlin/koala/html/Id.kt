package koala.html

import kotlinx.html.*
import kotlin.jvm.JvmInline

@JvmInline
value class Id(val value: String)

fun CoreAttributeGroupFacade.set(id: Id) {
    this.id = id.value
}