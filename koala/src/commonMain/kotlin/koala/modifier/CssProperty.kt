package koala.modifier

import kampfire.model.Url
import koala.Asset
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.Flex
import kotlinx.css.GridTemplateColumns
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension

object Css {
    val JustifyContent = Property<JustifyContent>("justify-content")
    val Flex = Property<Flex>("flex")
    val PositionAnchor = Property<PositionAnchor>("position-anchor")
    val Display = Property<Display>("display")
    val MinHeight = Property<LinearDimension>("min-height")
    val Height = Property<LinearDimension>("height")
    val Padding = Property<LinearDimension>("padding")
    val Margin = Property<LinearDimension>("margin")
    val Gap = Property<LinearDimension>("gap")
    val MaxWidth = Property<LinearDimension>("max-width")
    val BorderRadius = Property<LinearDimension>("border-radius")
}


val AnchorName = Property<PositionAnchor>("anchor-name")
val Width = Property<LinearDimension>("width")
val ZIndex = Property<Int>("z-index")
val GridTemplateColumns = Property<GridTemplateColumns>("grid-template-columns")
val ViewTransitionName = Property<String>("view-transition-name")
val AspectRatio = Property<Float>("aspect-ratio")
val BackgroundColor = Property<String>("background-color")
val Top = Property<LinearDimension>("top")
val Left = Property<LinearDimension>("left")
val AlignItems = Property<Align>("align-items")

val MaskUrl = Property<Asset>("mask-url", true)
val ColorScheme = Property<String>("color-scheme", true)
val BackgroundUrl = Property<Url>("background-url", true)
val AnchorId = Property<PositionAnchor>("anchor-id", true)
val ContainerAnchorId = Property<PositionAnchor>("anchor-container-id", true)
val ColumnCount = Property<Int>("column-count", true)
val InlineImage = Property<Url>("inline-image", true)
val RandomSeed = Property<Number>("random-seed", true)