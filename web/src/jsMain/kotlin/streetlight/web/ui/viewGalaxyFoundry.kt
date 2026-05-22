package streetlight.web.ui

import koala.LottieFile
import koala.css.Accent
import koala.css.JustifyContentSpaceBetween
import koala.css.modify
import koala.dom.*
import streetlight.model.data.GalaxyEdit
import streetlight.web.model.GalaxyEditor

fun RenderContext.viewGalaxyFoundry() {
    val model = app.getGalaxyEditor(GalaxyEdit(), renderScope)

    column {
        introSection("Galaxy Foundry", lottie = LottieFile.AstronautReading) {
            textBlock(introText1)
            textBlock(introText2)
        }

        formBody {
            galaxyCityForm(model)
            galaxyNameForm(model)
            galaxyImageForm(model)
            galaxyDescriptionForm(model)
            galaxyLocationForm(model)
            galaxyAccessForm(model)
        }

        row(modify(JustifyContentSpaceBetween)) {
            button("back", onClick = { portal.goBack() })
            button("Found Galaxy", modify(Accent), onClick = model::submit)
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