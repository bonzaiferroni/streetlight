package koala.modifier

import kampfire.model.Url
import koala.Asset
import kotlinx.css.Align
import kotlinx.css.ContainerType
import kotlinx.css.Display
import kotlinx.css.Flex
import kotlinx.css.FlexDirection
import kotlinx.css.FlexWrap
import kotlinx.css.GridTemplateColumns
import kotlinx.css.JustifyContent
import kotlinx.css.JustifySelf
import kotlinx.css.LinearDimension
import kotlinx.css.Position
import kotlinx.css.ObjectFit
import kotlinx.css.TextAlign
import kotlinx.css.Visibility

object Css {
    val Height = Property<LinearDimension>("height")
    val MinHeight = Property<LinearDimension>("min-height")
    val MaxHeight = Property<LinearDimension>("max-height")

    val Width = Property<LinearDimension>("width")
    val MinWidth = Property<LinearDimension>("min-width")
    val MaxWidth = Property<LinearDimension>("max-width")

    val JustifyContent = Property<JustifyContent>("justify-content")
    val TextAlign = Property<TextAlign>("text-align")
    val FlexDirection = Property<FlexDirection>("flex-direction")
    val FlexWrap = Property<FlexWrap>("flex-wrap")
    val ObjectFit = Property<ObjectFit>("object-fit")
    val ObjectPosition = Property<String>("object-position")
    val ContainerType = Property<ContainerType>("container-type")
    val Position = Property<Position>("position")
    val Flex = Property<Flex>("flex")
    val FlexBasis = Property<LinearDimension>("flex-basis")
    val FlexShrink = Property<Number>("flex-shrink")
    val PositionAnchor = Property<PositionAnchor>("position-anchor")
    val Display = Property<Display>("display")
    val Visibility = Property<Visibility>("visibility")
    val Padding = Property<LinearDimension>("padding")
    val PaddingTop = Property<LinearDimension>("padding-top")
    val PaddingRight = Property<LinearDimension>("padding-right")
    val PaddingBottom = Property<LinearDimension>("padding-bottom")
    val PaddingLeft = Property<LinearDimension>("padding-left")
    val Margin = Property<LinearDimension>("margin")
    val MarginRight = Property<LinearDimension>("margin-right")
    val MarginBottom = Property<LinearDimension>("margin-bottom")
    val MarginLeft = Property<LinearDimension>("margin-left")
    val MarginTop = Property<LinearDimension>("margin-top")
    val Gap = Property<LinearDimension>("gap")
    val BorderRadius = Property<LinearDimension>("border-radius")
    val AnchorName = Property<PositionAnchor>("anchor-name")
    val ZIndex = Property<Int>("z-index")
    val GridTemplateColumns = Property<GridTemplateColumns>("grid-template-columns")
    val ViewTransitionName = Property<String>("view-transition-name")
    val AspectRatio = Property<Number>("aspect-ratio")
    val BackgroundColor = Property<String>("background-color")
    val Top = Property<LinearDimension>("top")
    val Left = Property<LinearDimension>("left")
    val Bottom = Property<LinearDimension>("bottom")
    val Right = Property<LinearDimension>("right")
    val AlignItems = Property<Align>("align-items")
    val AlignSelf = Property<Align>("align-self")
    val JustifySelf = Property<JustifySelf>("justify-self")
    val PlaceSelf = Property<Align>("place-self")
    val PlaceItems = Property<Align>("place-items")
    val PlaceContent = Property<JustifyContent>("place-content")
    val Opacity = Property<Number>("opacity")

    val MaskUrl = Property<Asset>("mask-url", true)
    val ColorScheme = Property<String>("color-scheme", true)
    val BackgroundUrl = Property<Url>("background-url", true)
    val AnchorId = Property<PositionAnchor>("anchor-id", true)
    val ContainerAnchorId = Property<PositionAnchor>("anchor-container-id", true)
    val ColumnCount = Property<Int>("column-count", true)
    val InlineImage = Property<Url>("inline-image", true)
    val RandomSeed = Property<Number>("random-seed", true)
}
