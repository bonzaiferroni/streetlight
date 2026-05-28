package koala

import kampfire.model.ImageSize
import kampfire.model.ScaledImage
import kampfire.model.ScaledImageArray

object JsFile : FileSet<Js>() {
    val Utils = add("utils.js")
    val Tabs = add("tabs.js")
    val Koala = add("koala/koala.js")

    val Web = jsFileOf("web.js", basePath = "/js/streetlight/")
}

object CssFile : FileSet<Css>() {
    val Reset = add("reset.css")

    // val Styles = add("styles.css")
    val Typography = add("typography.css")
    val Button = add("button.css")
    val Layout = add("layout.css")
    val Magic = add("magic.css")
    val Tabs = add("tabs.css")
    val GeoMap = add("geo-map.css")
    // val Sandbox = add("sandbox.css")
}

object SvgFile : FileSet<Svg>() {
    val Clock = add("clock.svg")
    val Bus = add("bus.svg")
    val Food = add("food.svg")
    val Guitar = add("guitar.svg")
    val Meet = add("social.svg")
    val Train = add("train.svg")
    val TransitStop = add("transit-stop.svg")
    val Focus = add("focus.svg")
    val ChevronDown = add("chevron-down.svg")
    val Someone = add("someone.svg")
    val SomeoneSmall = add("someone-small.svg")
    val Flame = add("flame.svg")
    val StarOutline = add("star-outline.svg")
    val StarFilled = add("star-filled.svg")
    val Plus = add("plus.svg")
    val Check = add("check.svg")
    val Trash = add("trash.svg")
    val Edit = add("edit.svg")
    val Settings = add("settings.svg")
    val Menu = add("menu.svg")
    val Search = add("search.svg")
    val Magic = add("magic.svg")
    val Eye = add("eye.svg")
    val EyeOff = add("eye-off.svg")
    val EyeClosed = add("eye-closed.svg")
    val Crosshairs = add("crosshairs.svg")
    val LoaderSmall = add("loader-small.svg")
    val StarOff = add("star-off.svg")
    val Minus = add("minus.svg")
    val Helm = add("helm.svg")
    val Github = add("github.svg")
    val Calendar = add("calendar.svg")
    val TicketSmall = add("ticket-small.svg")
    val Backspace = add("backspace.svg")
    val Light = add("light.svg")
    val LightFilled = add("light-filled.svg")
    val SignOut = add("sign-out.svg")
    val Info = add("info.svg")
    val Dashboard = add("dashboard.svg")
    val Rays = add("rays.svg")
    val Sun = add("sun.svg")
    val Moon = add("moon.svg")
    val PanelRight = add("panel-right.svg")
    val PanelLeft = add("panel-left.svg")
    val ArrowLeft = add("arrow-left.svg")
    val ArrowRight = add("arrow-right.svg")
    val MessagePlus = add("message-plus.svg")
    val MessageMinus = add("message-minus.svg")
    val EyePlus = add("eye-plus.svg")
    val EyeMinus = add("eye-minus.svg")
    val MapPin = add("map-pin.svg")
    val Pin = add("pin.svg")
    val DotsVertical = add("dots-vertical.svg")
    val LogoText = add("logo-text.svg")
    val LogoFlame = add("logo-flame.svg")
    val Boost = add("boost.svg")
    val Dots = add("dots.svg")
    val Link = add("link.svg")
}

object LottieFile : FileSet<Lottie>() {
    val airplane = addLottie("airplane.json")
    val fox = addLottie("fox.json")
    val cassette = addLottie("cassette.json")
    val Wombat = addLottie("wombat.json")
    val cloudSync = addLottie("cloud-sync.json")
    val confused = addLottie("confused.json")
    val cupShuffle = addLottie("cup-shuffle.json")
    val StrollingMan = addLottie("strolling-man.json")
    val dinoLoad = addLottie("dino-load.json")
    val ghost = addLottie("ghost.json")
    val friendWave = addLottie("friend-wave.json")
    val helicopter = addLottie("helicopter.json")
    val playPause = addLottie("play-pause.json")
    val Cat = addLottie("cat.json")
    val AstronautReading = addLottie("astronaut-reading.json")
    val Astronaut404 = addLottie("astronaut-404.json")
    val rocket = addLottie("rocket.json")
    val secureCloudSync = addLottie("secure-cloud-sync.json")
    val ServerSync = addLottie("server-sync.json")
    val spinningCircles = addLottie("spinning-circles.json")
    val circleSync = addLottie("circle-sync.json")
}

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
}