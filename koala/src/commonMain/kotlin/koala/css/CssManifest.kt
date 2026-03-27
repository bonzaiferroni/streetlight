package koala.css

import koala.html.*

object ElementClass {
    // val lottie = Css("lottie")
    // val logo = Css("logo")
    // val action = Css("action")
    // val button = Css("btn")
    // val switch = Css("switch")
    // val flowBlock = Css("flow-block")
    // val itemsBlock = Css("items-block")
    // val textLabel = Css("text-label")
    // val blockLabel = Css("block-label") may need more work
    // val thumbImage = Css("thumb-image") may need more work
    // val shellBox = Css("shell-box")
    // val headerImage = Css("header-image")
    // val wireBlock = Css("wire-block")
    // val messageBox = Css("message-box")
    // val imageWithBackdrop = Css("image-with-backdrop")
    // val carousel = Css("carousel")
}

val CssManifest get() = listOf(
    // base
    ResetCss,
    StylesCss,
    TypographyCss,
    ButtonCss,
    LayoutCss,
    MagicCss,
    TabsCss,
    GeoMapCss,
    SandboxCss,
    // elements
    IconCss,
    LogoCss,
    ListingCss,
    ListItemCss,
    PopoverCss,
    SwapBlockCss,
    SectionCss,
    ActionCss,
    TextLabelCss,
    MessageBoxCss,
    SwitchCss,
    FlowBlockCss,
    ItemsBlockCss,
    ShellBoxCss,
    WireBlockCss,
    ImageWithBackdropCss,
    CarouselCss,
    HeaderImageCss,
    FilePickerCss,
    // utilities
    LayoutUtilityCss.toStylesheet(),
    DisplayUtilityCss.toStylesheet(),
    FontUtilityCss.toStylesheet(),
)
