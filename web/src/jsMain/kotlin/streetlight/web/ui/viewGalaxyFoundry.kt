package streetlight.web.ui

import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.headerImage
import koala.html.heading1
import koala.html.heading3
import koala.html.heading4
import koala.html.heading5
import koala.html.textProperty
import koala.html.section
import koala.html.spacer
import koala.model.mapDistinct
import streetlight.model.data.GalaxyEdit
import streetlight.web.model.Streetlight
import streetlight.web.model.GalaxyFoundry

fun ViewContext<Streetlight>.viewGalaxyFoundry() {
    val app = model
    val model = GalaxyFoundry(app, renderScope)

    val geoMap = app.geoMap
    val nameFlow = model.galaxyFlow.mapDistinct { it.name ?: "" }
    val blobFlow = model.stateFlow.mapDistinct { it.blobUrl }
    val descriptionFlow = model.galaxyFlow.mapDistinct { it.description ?: "" }
    val pathFlow = model.galaxyFlow.mapDistinct { it.path ?: "" }
    val textMod = modify()
    val sectionMod = modify(QueryContainer, Gap0)
    val queryColumnMod = modify(ContainerMdRow)
    val instructionsColumnMod = modify(Flex1, JustifyContentCenter)
    val contentColumnMod = modify(Flex1)
    val cardMod = modify(ZenCardBg)
    val footnoteMod = modify(OpacityMost, Italic, TextAlignCenter)

    column(modify(Gap4)) {
        section(sectionMod) {
            filigree {
                heading1("Galaxy Foundry", modify(Shrinkable))
            }
            row(modify(AlignItemsCenter, QueryContainer)) {
                column(modify(Flex4, FlexMd2, PaddingLeft3)) {
                    textBlock(introText1)
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
                column {
                    column(queryColumnMod) {
                        column(instructionsColumnMod) {
                            textBlock(nameInstructions1, textMod)
                            column(modify(Gap0, OpacityMost)) {
                                textBlock("Examples:")
                                bulletsOf("Denver Book Club", "Page Turners")
                            }
                        }
                        column(contentColumnMod + Gap0) {
                            textField("name", onChangeValue = model::setName, bindFlow = nameFlow)
                            textBlock(nameCharacters, footnoteMod)
                        }
                    }
                }
                column(modify(MarginTop2)) {
                    column(queryColumnMod) {
                        column(instructionsColumnMod) {
                            textBlock(pathInstructions)
                            column(modify(Gap0, OpacityMost)) {
                                textBlock("Currently:")
                                flowBlock(pathFlow) { path ->
                                    box {
                                        bulletsOf("streetlight.ing/g/$path")
                                    }
                                }
                            }
                        }
                        column(contentColumnMod) {
                            column(modify(Gap0)) {
                                textField("path", onChangeValue = model::setPath, bindFlow = pathFlow)
                                textBlock(pathCharacters, footnoteMod)
                            }
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
                        bulletsOf(
                            modify(OpacityMost),
                            "Ideally at least 1024 pixels wide and 512 pixels tall.",
                            canBeChangedText,
                            imageRequirements
                        )
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
                            val galaxy = name.takeIf { it.isNotEmpty() } ?: "the galaxy"
                            textBlock("Describe ${galaxy} to newcomers.", textMod)
                        }
                        bulletsOf(
                            modify(OpacityMost),
                            canBeChangedText
                        )
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
                        bulletsOf(
                            modify(OpacityMost),
                            canBeChangedText
                        )
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
A galaxy is a streetlight community where events, locations, and other posts can be shared on a map. 
As a galaxy founder, you may curate the content yourself or open it up to the community. 
"""

private val nameInstructions1 = "Let's give the galaxy a name, up to ${GalaxyEdit.MAX_NAME_LENGTH} characters."

private val canBeChangedText = "Can be changed later on."

private val nameCharacters = "Available: letters, numbers, spaces, and ${GalaxyEdit.NameCharacters.joinToString(" ")}"

private val pathInstructions = "The path determines the web address of the galaxy."
private val pathCharacters = "Available: letters, numbers, and ${GalaxyEdit.PathCharacters.joinToString(" ")}"

private val imageInstructions1 = """
This image will appear at the top of the galaxy page.
"""
private val imageRequirements = "Suitable for all audiences."

private val mapInstructions1 = """
Choose the point on the map and zoom level that people will see first. They can move around from there.
"""