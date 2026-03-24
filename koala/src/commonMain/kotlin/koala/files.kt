package koala

object JsFiles: FileSet<JsFile>() {
    val utils = add("utils.js")
    val tabs = add("tabs.js")
    val koala = add("koala/koala.js")
}

object CssFiles: FileSet<CssFile>() {
    val reset = add("reset.css")
    val styles = add("styles.css")
    val typography = add("typography.css")
    val button = add("button.css")
    val layout = add("layout.css")
    val animation = add("animation.css")
    val tabs = add("tabs.css")
    val logo = add("logo.css")
    val geoMap = add("geoMap.css")
    val elements = add("elements.css") // isGenerated = true
    val genElements = add("gen-elements.css", isGenerated = true)
    val sandbox = add("sandbox.css")
    val utilities = add("utilities.css")
}

object SvgFiles: FileSet<SvgFile>() {
    val bus = add("bus.svg")
    val food = add("food.svg")
    val guitar = add("guitar.svg")
    val meet = add("social.svg")
    val train = add("train.svg")
    val transitStop = add("transit-stop.svg")
    val focus = add("focus.svg")
    val chevronDown = add("chevron-down.svg")
    val emptyProfile = add("empty-profile.svg")
    val flame = add("flame.svg")
    val starOutline = add("star-outline.svg")
    val starFilled = add("star-filled.svg")
    val plus = add("plus.svg")
    val check = add("check.svg")
    val trash = add("trash.svg")
    val edit = add("edit.svg")
    val settings = add("settings.svg")
}

object LottieFiles: FileSet<LottieFile>() {
    val airplane = addLottie("airplane.json")
    val fox = addLottie("fox.json")
    val cassette = addLottie("cassette.json")
    val catWalk = addLottie("cat-walk.json")
    val cloudSync = addLottie("cloud-sync.json")
    val confused = addLottie("confused.json")
    val cupShuffle = addLottie("cup-shuffle.json")
    val strollingMan = addLottie("strolling-man.json")
    val dinoLoad = addLottie("dino-load.json")
    val ghost = addLottie("ghost.json")
    val friendWave = addLottie("friend-wave.json")
    val helicopter = addLottie("helicopter.json")
    val playPause = addLottie("play-pause.json")
    val cat = addLottie("cat.json")
    val astronautReading = addLottie("astronaut-reading.json")
    val astronautWalking = addLottie("astronaut-walking.json")
    val rocket = addLottie("rocket.json")
    val secureCloudSync = addLottie("secure-cloud-sync.json")
    val serverSync = addLottie("server-sync.json")
    val spinningCircles = addLottie("spinning-circles.json")
    val circleSync = addLottie("circle-sync.json")
}