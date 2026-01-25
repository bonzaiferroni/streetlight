package streetlight.server.pages

import koala.html.AlignItemsCenter
import koala.html.AlignItemsStretch
import koala.html.Bold
import koala.html.Dim
import koala.html.Flex1
import koala.html.Flex2
import koala.html.Large
import koala.html.NoGap
import koala.html.TextAlignCenter
import koala.html.TextAlignRight
import koala.html.column
import koala.html.heading5
import koala.html.lottie
import koala.html.modify
import koala.html.paragraph
import koala.html.row
import kotlinx.html.*

fun FlowContent.appTab() {
    column(AlignItemsCenter) {
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
                    externalLink("https://github.com/bonzaiferroni/streetlight", "Streetlight")
                    +" is 100% free and open-source. Free as in speech, free as in beer. "
                }
                paragraph(
                    "Do you like working with Kotlin and/or people? Consider becoming a contributor. " +
                            "As a software development community, we welcome people at any stage in their career. " +
                            "Are you interested in working on open-source software full time? " +
                            "We are based in Aurora, CO, and we have support opportunities. "
                )
                paragraph("Work on Streetlight or your own open-source idea.", Large)
            }
        }
        row {
            column(Flex1) {
                paragraph {
                    +"For better or for worse, apps are evermore present in our lives. "
                    +"As software engineers, we hold influence. "
                    +"The nature of our work supports a level of collaboration as yet unrealized in human history. "
                    +"We are like giants who stand on the shoulders of other giants, each one reaching higher. "
                }
                paragraph("It's giants all the way down.", Large)
            }
            column(NoGap, Flex1, AlignItemsStretch) {
                heading5("Our Giants", TextAlignCenter)
                githubLink("web", "kotlinx.html", "Kotlin")
                githubLink("app client", "Compose Multiplatform", "jetbrains", "compose-multiplatform")
                githubLink("app database", "SQLite", "sqlite")
                githubLink("app ORM", "Room", "androidx-releases")
                githubLink("server", "Ktor", "jetbrains")
                githubLink("server database", "Postgres", "postgres")
                githubLink("server ORM", "Exposed", "jetbrains")
                githubLink("animation", "Lottie", "airbnb")
                githubLink("animation content", "Open Animation", "orispok", "OpenAnimationApp")
                githubLink("map", "MapLibre", "maplibre-gl-js")
                githubLink("map data", "OpenFreeMap", "hyper-knot")
            }
        }
        homeFooter()
    }
}

fun FlowContent.githubLink(
    role: String,
    name: String,
    user: String,
    repo: String = name,
) {
    row {
        paragraph("$role:", Dim, TextAlignRight, Flex1)
        a("https://github.com/$user/$repo") {
            modify(Flex1)
            target = "_blank"
            rel = "noopener noreferrer"
            +name
        }
    }
}

fun FlowContent.externalLink(
    url: String,
    text: String
) {
    a(url) {
        target = "_blank"
        rel = "noopener noreferrer"
        +text
    }
}