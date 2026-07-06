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
    val ArrowLeft = add("arrow-left.svg")
    val ArrowRight = add("arrow-right.svg")
    val Backspace = add("backspace.svg")
    val Boost = add("boost.svg")
    val BoostFilled = add("boost-filled.svg")
    val Bus = add("bus.svg")
    val Calendar = add("calendar.svg")
    val CalendarMinus = add("calendar-minus.svg")
    val CalendarPlus = add("calendar-plus.svg")
    val CaretLeft = add("caret-left.svg")
    val CaretUp = add("caret-up.svg")
    val Check = add("check.svg")
    val ChevronDown = add("chevron-down.svg")
    val City = add("city.svg")
    val Clock = add("clock.svg")
    val Crosshairs = add("crosshairs.svg")
    val Dashboard = add("dashboard.svg")
    val Directions = add("directions.svg")
    val Dots = add("dots.svg")
    val DotsVertical = add("dots-vertical.svg")
    val Edit = add("edit.svg")
    val ExpandBelow = add("expand-below.svg")
    val Eye = add("eye.svg")
    val EyeClosed = add("eye-closed.svg")
    val EyeMinus = add("eye-minus.svg")
    val EyeOff = add("eye-off.svg")
    val EyePlus = add("eye-plus.svg")
    val Flame = add("flame.svg")
    val Focus = add("focus.svg")
    val Food = add("food.svg")
    val FrameEye = add("frame-eye.svg")
    val GearLarge = add("gear-large.svg")
    val GearSmall = add("gear-small.svg")
    val Github = add("github.svg")
    val Guitar = add("guitar.svg")
    val Helm = add("helm.svg")
    val Home = add("home.svg")
    val Info = add("info.svg")
    val Light = add("light.svg")
    val LightFilled = add("light-filled.svg")
    val Link = add("link.svg")
    val LoaderSmall = add("loader-small.svg")
    val LogoFlame = add("logo-flame.svg")
    val LogoText = add("logo-text.svg")
    val Magic = add("magic.svg")
    val MapPinOutline = add("map-pin-outline.svg")
    val MapWithPin = add("map-pin.svg")
    val Meet = add("social.svg")
    val Menu = add("menu.svg")
    val MessageMinus = add("message-minus.svg")
    val MessagePlus = add("message-plus.svg")
    val Minus = add("minus.svg")
    val Moon = add("moon.svg")
    val PanelLeft = add("panel-left.svg")
    val PanelRight = add("panel-right.svg")
    val Pin = add("pin.svg")
    val Plus = add("plus.svg")
    val Rays = add("rays.svg")
    val Search = add("search.svg")
    val SignOut = add("sign-out.svg")
    val Someone = add("someone.svg")
    val SomeoneSmall = add("someone-small.svg")
    val StarFilled = add("star-filled.svg")
    val StarOff = add("star-off.svg")
    val StarOutline = add("star-outline.svg")
    val Sun = add("sun.svg")
    val TicketSmall = add("ticket-small.svg")
    val Train = add("train.svg")
    val TransitStop = add("transit-stop.svg")
    val Trash = add("trash.svg")

    // filigree
    val CircularFiligree = add("circular-filigree.svg")
    val CircularFiligreeAnimated = add("circular-filigree-animated.svg")
}

object LottieFile : FileSet<Lottie>() {
    val Airplane = addLottie("airplane.json") // airplane circling earth
    val Fox = addLottie("fox.json") // replace
    val Cassette = addLottie("cassette.json")
    val Wombat = addLottie("wombat.json") // wombat pond
    val cloudSync = addLottie("cloud-sync.json")
    val confused = addLottie("confused.json")
    val cupShuffle = addLottie("cup-shuffle.json")
    val StrollingMan = addLottie("strolling-man.json")
    val dinoLoad = addLottie("dino-load.json")
    val Ghost = addLottie("ghost.json")
    val FriendWave = addLottie("friend-wave.json") // great motion, too cute
    val helicopter = addLottie("helicopter.json")
    val playPause = addLottie("play-pause.json")
    val Cat = addLottie("cat.json")
    val AstronautReading = addLottie("astronaut-reading.json")
    val Astronaut404 = addLottie("astronaut-404.json") // replace
    val Rocket = addLottie("rocket.json") // replace
    val secureCloudSync = addLottie("secure-cloud-sync.json")
    val ServerSync = addLottie("server-sync.json")
    val SpinningCircles = addLottie("spinning-circles.json")
    val CircleSync = addLottie("circle-sync.json")
    val StreetlightNight = addLottie("streetlight-night.json")
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