package streetlight.server.pages

import klutch.html.*
import kotlinx.html.*

fun FlowContent.appTab() {
    column {
        row {
            lottie("cup_stack", Flex1)
            val introText = "Streetlight is an app for the discovery and production of street performances. " +
                    "It is cross-platform, open-source, and 100% Kotlin. "
            paragraph(introText, Flex2, Large)
        }
        row {
            column(Flex2) {
                column(NoGap) {
                    paragraph("Do you have a talent to share with passersby?")
                    paragraph("Do you have a business, venue, or message you would like to promote?")
                    paragraph("Are you walking down the street somewhere and looking for something to experience?")
                }
                paragraph("Consider downloading Streetlight to see what it can offer.")
            }
            lottie("dancing_man", Flex1)
        }
        row {
            lottie("playful_cat", Flex1)
            column(Flex2) {
                paragraph {
                    span {
                        modify(Bold)
                        +"Streetlight is 100% free and open-source. "
                    }
                    +"Free as in speech, free as in beer."
                }
                paragraph(welcomeText)
            }
        }
    }
}

const val welcomeText = "Do you know Kotlin or do you know people? Consider becoming a contributor. " +
        "As a software development community, we welcome people at any stage in their career. " +
        "Are you interested in focusing on open-source software full time? " +
        "We are based in Aurora, CO, and we have support opportunities. " +
        "Work on Streetlight or your own open-source idea."