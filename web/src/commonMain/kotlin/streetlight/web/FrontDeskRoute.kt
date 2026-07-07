package streetlight.web

object FrontDeskRoute: StreetlightRoute {
    override val screen get() = Screen.Feedback
    override val title get() = "Feedback"
}