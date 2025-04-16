package streetlight.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform