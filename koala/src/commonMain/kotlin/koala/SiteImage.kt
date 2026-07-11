package koala

import kampfire.model.ImageSize
import kampfire.model.ScaledImage
import kampfire.model.ScaledImageArray

object SiteImage : FileSet<Image>() {
    val crossing = siteImageOf("crossing.jpg")
    val placeholderLg = siteImageOf("placeholder-lg.jpg")
    val placeholderMd = siteImageOf("placeholder-md.jpg")
    val placeholderSm = siteImageOf("placeholder-sm.jpg")
    val placeholderTh = siteImageOf("placeholder-th.jpg")
    val placeholder: ScaledImageArray = listOf(
        ScaledImage(ImageSize.Large, placeholderLg.url),
        ScaledImage(ImageSize.Medium, placeholderMd.url),
        ScaledImage(ImageSize.Small, placeholderSm.url),
        ScaledImage(ImageSize.Thumb, placeholderTh.url),
    )

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