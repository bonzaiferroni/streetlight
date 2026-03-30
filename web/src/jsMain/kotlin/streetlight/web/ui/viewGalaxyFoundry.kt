package streetlight.web.ui

import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.html.filigree
import koala.html.headerImage
import koala.html.heading1
import koala.html.heading3
import koala.html.textProperty
import koala.html.section
import koala.html.spacer
import koala.model.mapDistinct
import streetlight.web.model.Streetlight
import streetlight.web.model.GalaxyFoundry

fun ViewContext<Streetlight>.viewGalaxyFoundry() {
    val app = model
    val model = GalaxyFoundry(app, renderScope)

    val geoMap = app.geoMap
    val nameFlow = model.galaxyFlow.mapDistinct { it.name ?: "" }
    val blobFlow = model.stateFlow.mapDistinct { it.blobUrl }
    val descriptionFlow = model.galaxyFlow.mapDistinct { it.description ?: "" }
    val textMod = modify()
    val sectionMod = modify(QueryContainer, Gap0)
    val queryColumnMod = modify(ContainerMdRow)
    val instructionsColumnMod = modify(Flex1, JustifyContentCenter)
    val contentColumnMod = modify(Flex1)
    val cardMod = modify(ZenCardBg)

    column(modify(Gap4)) {
        section(sectionMod) {
            filigree {
                heading1("Galaxy Foundry", modify(Shrinkable))
            }
            row(modify(AlignItemsCenter, QueryContainer)) {
                column(modify(Flex4, FlexMd2)) {
                    textBlock(introText1)
                    textBlock(introText2)
                }
                row(modify(Flex1, JustifyContentCenter)) {
                    lottie(LottieFile.AstronautReading, modify(MaxHeight32))
                }
            }
        }

        section(sectionMod) {
            filigree {
                heading3("Galaxy Name")
            }
            card(cardMod) {
                column(queryColumnMod) {
                    column(instructionsColumnMod) {
                        textBlock(nameInstructions1, textMod)
                        textBlock(nameInstructions2, textMod)
                    }
                    column(contentColumnMod) {
                        textField("name", onChangeValue = model::setName, bindFlow = nameFlow)
                        column(modify(Gap0)) {
                            textProperty("Requirements", nameRequirements)
                            textProperty("Characters", nameCharacters)
                        }
                    }
                }
            }
        }

        section(sectionMod) {
            filigree {
                heading3("Image")
            }
            card(cardMod) {
                column(queryColumnMod) {
                    column(instructionsColumnMod) {
                        textBlock(imageInstructions1, textMod)
                        textBlock(canBeChangedText, textMod)
                        textProperty("Requirements", imageRequirements)
                    }
                    column(contentColumnMod) {
                        imageDrop(blobFlow, model::setBlobUrl) {
                            box {
                                headerImage(model.stateNow.galaxy.name ?: "", it)
                            }
                        }
                    }
                }
            }
        }

        section(sectionMod) {
            filigree {
                heading3("Description")
            }
            card(cardMod) {
                column(queryColumnMod) {
                    column(instructionsColumnMod) {
                        flowBlock(nameFlow) { name ->
                            textBlock("Describe $name to newcomers.", textMod)
                        }
                    }
                    column(contentColumnMod) {
                        textEditor("description", onChangeValue = model::setDescription, bindFlow = descriptionFlow)
                    }
                }
            }
        }

        section(sectionMod) {
            filigree {
                heading3("Map point")
            }
            card(cardMod) {
                column(queryColumnMod) {
                    column(instructionsColumnMod) {
                        textBlock(mapInstructions1, textMod)
                        textBlock(canBeChangedText, textMod)
                    }
                    column(contentColumnMod) {
                        viewGeoMap(geoMap, app.appScope)
                    }
                }
            }
        }

        row {
            spacer(modify(Flex1))
            button("Found Galaxy", modify(Accent), onClick = model::foundGalaxy)
        }

        appFooter("web/src/jsMain/kotlin/streetlight/web/ui/viewGalaxyFoundry.kt")
    }
}

private val introText1 = """
A galaxy is a streetlight community where events, resources, and stories can be posted on a map. 
As a galaxy founder, you may curate the content yourself or open it up to the community.
"""

private val introText2 = """
If you haven't yet, take a moment to find out what Streetlight is all about. 
As a galaxy leader, you have a special role in shaping the experience of Streetlight, and this is a role you can extend to others.
"""


private val nameInstructions1 = "Let's give the galaxy a name. It should describe what people will find there."
private val nameInstructions2 = """
It can be something direct like "Book Clubs of Denver" or something catchy like "Page Turners", 
whatever fits the galaxy mood. 
"""
private val canBeChangedText = "This can be changed later on."

private val nameRequirements = "Suitable for all audiences, under 28 characters."
private val nameCharacters = "Letters, numbers, and ,?!:"

private val imageInstructions1 = """
This image will appear at the top of the galaxy page. 
For best quality, use an image at least 1024 pixels wide and 512 pixels tall.
"""
private val imageRequirements = "Suitable for all audiences."

private val mapInstructions1 = """
Choose the point on the map and zoom level that people will see first. They can move around from there.
"""