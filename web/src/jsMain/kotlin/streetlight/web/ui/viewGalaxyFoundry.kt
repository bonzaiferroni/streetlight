package streetlight.web.ui

import koala.LottieFile
import koala.css.Accent
import koala.css.AlignItemsCenter
import koala.css.AntiShadow
import koala.css.Flex1
import koala.css.Flex4
import koala.css.FlexMd2
import koala.css.JustifyContentCenter
import koala.css.JustifyContentSpaceBetween
import koala.css.MaxHeight32
import koala.css.PaddingLeft3
import koala.css.Shrinkable
import koala.css.modify
import koala.dom.*
import koala.html.filigree
import koala.html.heading1
import koala.html.section
import streetlight.web.model.GalaxyEditor

fun RenderContext.viewGalaxyFoundry() {
    val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)

    column {
        section {
            filigree {
                heading1("Galaxy Foundry", modify(Shrinkable, AntiShadow))
            }
            row(modify(AlignItemsCenter)) {
                column(modify(Flex4, FlexMd2, PaddingLeft3)) {
                    textBlock(introText1)
                    textBlock(introText2)
                }
                row(modify(Flex1, JustifyContentCenter)) {
                    lottie(LottieFile.AstronautReading, modify(MaxHeight32))
                }
            }
        }

        viewGalaxyEditor(model)

        row(modify(JustifyContentSpaceBetween)) {
            button("back", onClick = { portal.goBack() })
            row {
                messageBox(model.msg.flow)
                button("Found Galaxy", modify(Accent), onClick = model::foundGalaxy)
            }
        }

        appFooter("web/src/jsMain/kotlin/streetlight/web/ui/viewGalaxyFoundry.kt")
    }
}

private val introText1 = """
A galaxy is a streetlight community where events, locations, and other posts can be shared on a map. 
As a galaxy founder, you may curate the content yourself or open it up to the community. 
"""

private val introText2 = """
Streetlight is in an early stage of development. It's current focus is our hometown, Denver.
Theoretically, your map can focus on any part of the world, but features like transit updates may not be available.
"""