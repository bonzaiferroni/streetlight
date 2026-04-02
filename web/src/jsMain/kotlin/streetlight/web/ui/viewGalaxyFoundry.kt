package streetlight.web.ui

import kabinet.utils.format
import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.headerImage
import koala.html.heading1
import koala.html.heading3
import koala.html.section
import koala.html.spacer
import koala.html.span
import koala.model.mapDistinct
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.PostPermission
import streetlight.web.model.Streetlight
import streetlight.web.model.GalaxyEditor

fun ViewContext<Streetlight>.viewGalaxyFoundry() {
    val app = model
    val model = GalaxyEditor(app, renderScope)

    val geoMap = app.geoMap
    val nameFlow = model.galaxyFlow.mapDistinct { it.name ?: "" }
    val blobFlow = model.stateFlow.mapDistinct { it.blobUrl }
    val descriptionFlow = model.galaxyFlow.mapDistinct { it.description ?: "" }
    val pathFlow = model.galaxyFlow.mapDistinct { it.path ?: "" }
    val permissionFlow = model.galaxyFlow.mapDistinct { it.postPermission }
    val reviewModeFlow = model.galaxyFlow.mapDistinct { it.reviewMode }
    val guideFlow = model.galaxyFlow.mapDistinct { it.postGuide ?: "" }
    val pointFlow = app.geoMap.stateFlow.mapDistinct { it.center to it.zoom }

    val textMod = modify()
    val sectionMod = modify(QueryContainer)
    val queryColumnMod = modify(ContainerMdRow)
    val querySubColumnMod = queryColumnMod + MarginTop2
    val instructionsColumnMod = modify(Flex1, JustifyContentCenter, Margin1)
    val contentColumnMod = modify(Flex1)
    val cardMod = modify(ZenCardBg)
    val footnoteMod = modify(OpacityMost, Italic, JustifyContentSpaceBetween, WhiteSpaceNoWrap)
    val bulletsMod = modify(OpacityMost)

    column(modify(Gap8)) {
        section(sectionMod) {
            filigree {
                heading1("Galaxy Foundry", modify(Shrinkable))
            }
            row(modify(AlignItemsCenter, QueryContainer)) {
                column(modify(Flex4, FlexMd2, PaddingLeft3)) {
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
                        column(modify(Gap0, OpacityMost)) {
                            textBlock("Examples:")
                            bulletsOf("Denver Book Club", "Page Turners")
                        }
                    }
                    column(contentColumnMod + Gap0) {
                        textField("name", onValue = model::setName, flow = nameFlow)
                        row(modify(footnoteMod)) {
                            textBlock(nameCharacters)
                            flowBlock(nameFlow) { name ->
                                textBlock("${name.length}/${GalaxyEdit.MAX_NAME_LENGTH}")
                            }
                        }
                    }
                }
                column(querySubColumnMod) {
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
                            textField("path", onValue = model::setPath, flow = pathFlow)
                            row(modify(footnoteMod)) {
                                textBlock(pathCharacters)
                                flowBlock(pathFlow) { path ->
                                    textBlock("${path.length}/${GalaxyEdit.MAX_NAME_LENGTH}")
                                }
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
                            bulletsMod,
                            "Ideally at least 1024 pixels wide and 512 pixels tall.",
                            imageRequirements,
                            canBeChangedText,
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
                            val mod = when (name.isEmpty()) {
                                true -> null
                                else -> modify(Italic, Bold)
                            }
                            textBlock {
                                span("Describe ")
                                span(galaxy, mod)
                                span(" to newcomers.")
                            }
                        }
                        bulletsOf(
                            bulletsMod,
                            "Can be brief or detailed.",
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
                        flowBlock(pointFlow) { (point, zoom) ->
                            box {
                                bulletsOf(
                                    bulletsMod,
                                    "latitude: ${point.lat.toFloat().format(4)}",
                                    "longitude: ${point.lng.toFloat().format(4)}",
                                    "zoom: ${zoom.format(1)}",
                                    canBeChangedText,
                                )
                            }
                        }
                    }
                    column(contentColumnMod) {
                        viewGeoMap(geoMap, app.appScope)
                    }
                }
            }
        }

        section(sectionMod) {
            filigree {
                heading3("Other details")
            }
            card(cardMod) {
                column(queryColumnMod) {
                    column(instructionsColumnMod) {
                        textBlock("You can open up posting to the community or curate the content yourself.")
                        bulletsOf(
                            bulletsMod,
                            canBeChangedText,
                        )
                    }
                    column(contentColumnMod) {
                        dropMenu(model::setPostPermission, { it.label }, flow = permissionFlow)
                    }
                }
                flowBlock(permissionFlow) { permission ->
                    if (permission == PostPermission.Founder) return@flowBlock
                    column(modify(QueryContainer)) {
                        column(querySubColumnMod) {
                            column(instructionsColumnMod) {
                                textBlock(permissionInfo1)
                                bulletsOf(
                                    bulletsMod,
                                    "You can extend the role of moderation to other community members.",
                                    canBeChangedText
                                )
                                if (permission == PostPermission.Everyone) {
                                    textBlock(anonymousInfoText)
                                }
                            }
                            column(contentColumnMod) {
                                dropMenu(model::setReviewMode, { it.label }, flow = reviewModeFlow)
                            }
                        }
                    }
                }
                column(querySubColumnMod) {
                    column(instructionsColumnMod) {
                        textBlock("Provide guidelines or requirements for the content of community posts.")
                        bulletsOf(
                            modify(OpacityMost),
                            canBeChangedText,
                            "Optional"
                        )
                    }
                    column(contentColumnMod) {
                        textEditor("Post Guide", onChangeValue = model::setPostGuide, bindFlow = guideFlow)
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

private val introText2 = """
Streetlight is in an early stage of development. It's current focus is our hometown, Denver.
Theoretically, your map can focus on any part of the world, but features like transit updates may not be available.
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

private val permissionInfo1 = "You can choose to review posts before they appear in the feed."

private val anonymousInfoText = "Posts from users who are not signed in will always need review before appearing in the feed."