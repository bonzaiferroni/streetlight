package koala

interface PageResource {
    val bundle: JsBundle
    val styles: String
}

interface JsBundle {
    val web: Js
    val passwordReset: Js
}

object JsFile : FileSet<Js>() {
    val Utils = addJs("utils.js")
    val Koala = addJs("koala/koala.js")
}

object LottieFile : FileSet<Lottie>() {
    val Cat = addLottie("cat.json")
    val CatProto = addLottie("cat-proto.json")
    val Ghost = addLottie("ghost.json")
    val GhostProto = addLottie("ghost-proto.json")
    val GhostGirl = addLottie("ghost-girl.json")
    val StrollingMan = addLottie("strolling-man.json")
    val StrollingManProto = addLottie("strolling-man-proto.json")
    val DinoLoad = addLottie("dino-load.json")
    val DinoLoadProto = addLottie("dino-load-proto.json")
    val CharLoad = addLottie("char-load.json")
    val CupShuffle = addLottie("cup-shuffle.json")
    val CupShuffleProto = addLottie("cup-shuffle-proto.json")
    val Math = addLottie("math.json")
    val Airplane = addLottie("airplane.json") // airplane circling earth
    val Fox = addLottie("fox.json") // replace
    val Cassette = addLottie("cassette.json")
    val Wombat = addLottie("wombat.json") // wombat pond
    val cloudSync = addLottie("cloud-sync.json")
    val confused = addLottie("confused.json")
    val FriendWave = addLottie("friend-wave.json") // great motion, too cute
    val helicopter = addLottie("helicopter.json")
    val playPause = addLottie("play-pause.json")
    val AstronautReading = addLottie("astronaut-reading.json")
    val Astronaut404 = addLottie("astronaut-404.json") // replace
    val Rocket = addLottie("rocket.json") // replace
    val secureCloudSync = addLottie("secure-cloud-sync.json")
    val ServerSync = addLottie("server-sync.json")
    val SpinningCircles = addLottie("spinning-circles.json")
    val CircleSync = addLottie("circle-sync.json")
    val StreetlightNight = addLottie("streetlight-night.json")
}

