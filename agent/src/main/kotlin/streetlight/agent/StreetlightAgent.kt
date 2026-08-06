package streetlight.agent

object StreetlightAgent {
    val AgentToken = "Streetlight"
    val AgentVersion = "1.0"
    val AgentEmail = "lucas@streetlight.ing"

    val UserAgent = "${AgentToken}/${AgentVersion} ($AgentEmail)"
    val Accept = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    val AcceptLanguage = "en-US,en;q=0.5"
    val Connection = "keep-alive"
    val Locale = "en-US"

    val Timeout = 60_000
}

