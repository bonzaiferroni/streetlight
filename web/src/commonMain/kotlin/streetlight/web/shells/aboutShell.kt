package streetlight.web.shells

import koala.LottieFile
import koala.html.heading5
import koala.html.textBlock
import koala.html.row
import kotlinx.html.*
import koala.css.*
import koala.html.Id
import koala.html.column
import koala.html.heading1
import koala.html.lottie
import streetlight.web.pages.appFooter

fun FlowContent.aboutShell() {
    column(AboutKey.id, modify(AlignItemsCenter)) {
        heading1("About Streetlight")
        column(modify(MediaMdRow, AlignItemsCenter)) {
            lottie(LottieFile.cupShuffle, modify(Flex1, MaxWidth50P))
            val introText = "Streetlight is your front page for community events. " +
                    "It is cross-platform, open-source, and 100% Kotlin. "
            textBlock(introText, modify(Flex3, LargeText))
        }
        column(modify(MediaMdRow, AlignItemsCenter)) {
            lottie(LottieFile.strollingMan, modify(Flex1, MaxWidth50P))
            column(modify(Flex3)) {
                column(modify(Gap0)) {
                    textBlock("Do you have a talent to share with passersby?")
                    textBlock("Do you have a business, venue, or message you would like to promote?")
                    textBlock("Are you walking down the street somewhere and looking for something to experience?")
                }
                textBlock("Consider downloading Streetlight to see what it can offer.", modify(Bold))
            }
        }
        column(modify(MediaMdRow, AlignItemsCenter)) {
            lottie(LottieFile.Cat, modify(Flex1, MaxWidth50P))
            column(modify(Flex3)) {
                textBlock {
                    externalLink("https://github.com/bonzaiferroni/streetlight", "Streetlight")
                    +" is 100% free and open-source. Free as in speech, free as in beer. "
                }
                textBlock(
                    "Do you like working with Kotlin and/or people? Consider becoming a contributor. " +
                            "As a software development community, we welcome people at any stage in their career. "
                    // pending resources to follow through
//                            "Are you interested in working on open-source software full time? " +
//                            "We are based in Aurora, CO, and we have support opportunities. "
                )
//                paragraph("Work on Streetlight or your own open-source idea.", Large)
                textBlock {
                    +"For better or for worse, apps are evermore present in our lives. "
                    +"As software engineers, we hold influence. "
                    +"The nature of our work supports a level of collaboration as yet unrealized in human history. "
                    +"We are like giants who stand on the shoulders of other giants, each one reaching higher. "
                }
                textBlock("It's giants all the way down.", modify(LargeText))
            }
        }
        column(modify(Gap0, AlignItemsStretch, Width100P, MarginTop4)) {
            heading5("Our Giants", modify(TextAlignCenter))
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
            githubLink("icons", "Tabler", "tabler")
        }
        appFooter("web/src/commonMain/kotlin/streetlight/web/shells/homeShell.kt")
    }
}

object AboutKey {
    val id = Id("about-app-shell")
}

fun FlowContent.githubLink(
    role: String,
    name: String,
    user: String,
    repo: String = name,
) {
    row {
        textBlock("$role:", modify(OpacityMost, TextAlignRight, Flex1))
        a("https://github.com/$user/$repo") {
            addModifiers(Flex1)
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