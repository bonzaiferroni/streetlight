package koala.modifier

import kotlinx.css.Align
import kotlinx.css.Flex
import kotlinx.css.JustifyContent
import kotlinx.css.px

object Css {
    val JustifyContent = Property<JustifyContent>("justify-content")
    val AlignItems = Property<Align>("align-items")
    val Flex = Property<Flex>("flex")
}

val JustifyContentCenter = Css.JustifyContent.of(JustifyContent.center)
val AlignItemsCenter = Css.AlignItems.of(Align.center)

val Height2Px = Height.of(2.px)

val Flex1 = Css.Flex.of(Flex(1))