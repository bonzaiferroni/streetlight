package koala

import kampfire.model.ImageSize
import kampfire.model.ImageVariant

object SiteImage : FileSet<Image>() {
    val crossing = siteImageOf("crossing.jpg")

    val placeholderLg = siteImageUrlOf("placeholder-lg.jpg")
    val placeholderMd = siteImageUrlOf("placeholder-md.jpg")
    val placeholderSm = siteImageUrlOf("placeholder-sm.jpg")
    val placeholderTh = siteImageUrlOf("placeholder-th.jpg")

    val placeholder = siteImageOf("placeholder-lg.jpg", variants = listOf(
        ImageVariant(ImageSize.Large, placeholderLg),
        ImageVariant(ImageSize.Medium, placeholderMd),
        ImageVariant(ImageSize.Small, placeholderSm),
        ImageVariant(ImageSize.Thumb, placeholderTh),
    ))

    fun getPlaceholder(size: ImageSize) = when(size) {
        ImageSize.Thumb -> placeholderTh
        ImageSize.Small -> placeholderSm
        ImageSize.Medium -> placeholderMd
        ImageSize.Large -> placeholderLg
    }

    val FrontDesk = siteImageOf(
        "front-desk.jpg", 3 / 2f, "A woman sitting at a desk, answering the phone.",
        "Bill Branson | Wikimedia", "https://commons.wikimedia.org/wiki/File:Receptionist.jpg",
    )
    val ControlRoom = siteImageOf(
        "control-room.jpg", 4 / 3f, "A man sitting at a control panel with many lights and indicators.",
        "Denise Kinet | Wikimedia", "https://commons.wikimedia.org/wiki/File:Control_room_Johan_Neerman.jpg"
    )
    val HelixNebula = siteImageOf(
        "helix-nebula.jpg", 960 / 798f, "An eye-shaped nebula with blue clouds surrounding a red star.",
        "NASA | Wikimedia", "https://commons.wikimedia.org/wiki/File:Comets_Kick_up_Dust_in_Helix_Nebula_(PIA09178).jpg"
    )
}