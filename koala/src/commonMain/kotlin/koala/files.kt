package koala

object JsFile : FileSet<Js>() {
    val Utils = add("utils.js")
    val Tabs = add("tabs.js")
    val Koala = add("koala/koala.js")

    // not part of FileSet collection
    val Web = jsFileOf("web.js", basePath = "/js/streetlight/")
    val PasswordReset = jsFileOf("passwordReset.js", basePath = "/js/streetlight/")
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

