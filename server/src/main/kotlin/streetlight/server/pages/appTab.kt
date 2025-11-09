package streetlight.server.pages

import klutch.html.*
import kotlinx.html.*

fun FlowContent.appTab() {
    column {
        paragraph(introText)
        paragraph(userText)
        paragraph(welcomeText)
    }
}

const val introText = "Streetlight is an app for the production, appreciation, and support of street performances. " +
        "It is cross-platform, open-source, and 100% Kotlin. "

const val userText = "Do you have a talent to share with passersby? " +
        "Do you have a business, venue, or message you would like to promote? " +
        "Are you walking down the street somewhere and looking for something to experience? " +
        "Consider downloading Streetlight to see what it can offer."

const val welcomeText = "Streetlight is 100% free and open-source. Free as in speech, free as in beer. " +
        "Do you know Kotlin or do you know people? Consider becoming a contributor. " +
        "As a development community, we welcome people at any stage in their career. " +
        "Are you interested in focusing on open-source software full time? " +
        "We are based in Aurora, CO, and we have support opportunities. " +
        "Work on Streetlight or your own open-source idea."