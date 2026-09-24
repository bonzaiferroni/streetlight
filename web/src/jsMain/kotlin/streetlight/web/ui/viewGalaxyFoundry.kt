package streetlight.web.ui

import koala.LottieFile
import koala.modifier.*
import koala.dom.*
import streetlight.model.data.GalaxyEdit

fun ViewScope.viewGalaxyFoundry() {
    val model = app.getGalaxyEditor(GalaxyEdit(marks = emptyList()), contentScope)

    mainBody("viewGalaxyFoundry.kt") {
        introSection("Galaxy Foundry", lottie = LottieFile.AstronautReading) {
            textBlock(introText1)
            textBlock(introText2)
        }

        column(BodyStyle.MainColumn) {
            formCard("Galaxy Name") {
                galaxyNameFormRow(model)
            }
            formCard("Description") {
                galaxyDescriptionFormRow(model)
            }
            formCard("Map Location") {
                galaxyMapFormRow(model)
            }
            formCard("Access") {
                galaxyAccessFormRow(model)
            }
        }

        formSubmit(
            label = "Found Galaxy",
            onClick = model::submit,
            messenger = model.editMessage,
            buttonMod = Accent,
            back = MenuAction("back", onClick = portal::goBack),
        )
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

