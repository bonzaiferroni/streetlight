package koala.css

import koala.html.IconStyles
import koala.html.LISTING_STYLES
import koala.html.LIST_ITEM_STYLES
import koala.html.LOGO_STYLES
import koala.html.PopoverStyle
import koala.html.SectionStyles
import koala.html.SwapBlockStyle

object ElementClass {
    val lottie = Css("lottie")
    val logo = Css("logo")
    val action = Css("action")
    val button = Css("btn")
    val switch = Css("switch")
    val flowBlock = Css("flow-block")
    val itemsBlock = Css("items-block")
    val textLabel = Css("text-label")
    val blockLabel = Css("block-label")
    val thumbImage = Css("thumb-image")
    val shellBox = Css("shell-box")
    val headerImage = Css("header-image")
    val wireBlock = Css("wire-block")
    val messageBox = Css("message-box")
    val imageWithBackdrop = Css("image-with-backdrop")
    val carousel = Css("carousel")
}

val kotlinElementStyles get() = listOf(
    IconStyles,
    LOGO_STYLES,
    LISTING_STYLES,
    LIST_ITEM_STYLES,
    PopoverStyle,
    SwapBlockStyle,
    SectionStyles,
    layoutUtilities.mapNotNull{ it.definition }.joinToString("\n"),
    displayUtilities.mapNotNull{ it.definition }.joinToString("\n")
)
