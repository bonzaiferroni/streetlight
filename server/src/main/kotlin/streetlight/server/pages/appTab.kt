package streetlight.server.pages

import klutch.html.*
import kotlinx.html.*

fun FlowContent.appTab() {
    column {
        row {
            lottie("cup_stack", Flex1)
            val introText = "Streetlight is a street performance community and app. " +
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
                paragraph("Consider downloading Streetlight to see what it can offer.", Bold)
            }
            lottie("dancing_man", Flex1)
        }
        row {
            lottie("playful_cat", Flex1)
            column(Flex2) {
                paragraph {
                    githubLink("Streetlight", "bonzaiferroni")
                    +" is 100% free and open-source. Free as in speech, free as in beer. "
                }
                paragraph(welcomeText)
                column(NoGap) {
                    heading5("Tech stack:")
                    ul {
                        li {
                            propertyValue("app") {
                                githubLink("Compose Multiplatform", "jetbrains", "compose-multiplatform")
                            }
                        }
                        li {
                            propertyValue("server") {
                                githubLink("Ktor", "jetbrains")
                            }
                        }
                        li {
                            propertyValue("backend database") {
                                githubLink("Postgres", "postgres")
                            }
                        }
                        li {
                            propertyValue("backend ORM") {
                                githubLink("Exposed", "jetbrains")
                            }
                        }
                        li {
                            propertyValue("frontend database") {
                                githubLink("Sqlite", "sqlite")
                            }
                        }
                        li {
                            propertyValue("frontend ORM") {
                                githubLink("Room", "room")
                            }
                        }
                        li {
                            propertyValue("animations") {
                                githubLink("Lottie", "lottie")
                            }
                        }
                        li {
                            propertyValue("lottie content") {
                                githubLink("Open Animation", "lottie")
                            }
                        }
                    }
                }
            }
        }
    }
}

const val welcomeText = "Do you like working with Kotlin and/or people? Consider becoming a contributor. " +
        "As a software development community, we welcome people at any stage in their career. " +
        "Are you interested in working on open-source software full time? " +
        "We are based in Aurora, CO, and we have support opportunities. " +
        "Work on Streetlight or your own open-source idea."