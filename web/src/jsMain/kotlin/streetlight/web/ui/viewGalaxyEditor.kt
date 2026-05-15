package streetlight.web.ui

import kabinet.utils.format
import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.headerImage
import koala.html.heading1
import koala.html.section
import koala.html.span
import koala.model.GeoMap
import koala.model.mapDistinct
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.PostPermission
import streetlight.web.model.GalaxyEditor

fun RenderContext.viewGalaxyEditor() {
    val model = app.getCoroutineScoped<GalaxyEditor>(null, renderScope)
    val geoMap = app.get<GeoMap>()

    val nameFlow = model.galaxyFlow.mapDistinct { it.name ?: "" }
    val blobFlow = model.stateFlow.mapDistinct { it.blobUrl }
    val descriptionFlow = model.galaxyFlow.mapDistinct { it.description ?: "" }
    val pathFlow = model.galaxyFlow.mapDistinct { it.slug ?: "" }
    val permissionFlow = model.galaxyFlow.mapDistinct { it.postPermission }
    val reviewModeFlow = model.galaxyFlow.mapDistinct { it.reviewMode }
    val guideFlow = model.galaxyFlow.mapDistinct { it.postGuide ?: "" }
    val cityQueryFlow = model.stateFlow.mapDistinct { it.cityQuery }
    val isLocalFlow = model.stateFlow.mapDistinct { it.isLocal }
    val pointFlow = geoMap.stateFlow.mapDistinct { it.center to it.zoom }
    val localitiesFlow = model.stateFlow.mapDistinct { it.localities }
    val countryFlow = model.stateFlow.mapDistinct { it.country }
    val localityFlow = model.stateFlow.mapDistinct { it.locality }

    val bulletsMod = modify(OpacityMost)

    column(modify(Gap8)) {
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

        editorSection("City") {
            editorPart(
                instructions = cityInstructions,
            ) {
                row {
                    textField("city", modify(Flex1), onValue = model::setCityQuery, flow = cityQueryFlow)
                    textField("country", modify(Width24), onValue = model::setCountry, flow = countryFlow)
                }
                column(modify(Height32, OverflowYAuto, Gap0)) {
                    row(modify(Padding1, JustifyContentSpaceBetween)) {
                        textBlock("city", modify(OpacitySome, Italic))
                        textBlock("galaxies", modify(OpacitySome, Italic))
                    }
                    selectionBlock(localitiesFlow, model::setLocality, localityFlow) { locality ->
                        card(modify(ZenBg, BorderRadius1)) {
                            row(modify(JustifyContentSpaceBetween)) {
                                textBlock("${locality.city}, ${locality.state}", modify(Flex1))
                                textBlock(locality.galaxyCount.toString())
                            }
                        }
                    }
                }
            } // .flowVisibility(isLocalFlow)

            editorPart(
                instructions = "You can also choose not to focus on any particular city.",
            ) {
                checkBox("This galaxy has a city", model::setIsLocal, isLocalFlow, modify(Padding1))
            }
        }

        editorSection("Galaxy Name") {
            editorPart(
                instructions = nameInstructions1,
                examples = listOf("Denver Book Club", "Page Turners"),
            ) {
                editorTextField("name", model::setName, nameFlow, nameCharacters, GalaxyEdit.MAX_NAME_LENGTH)
            }
            editorPart(
                instructions = pathInstructions,
                info = {
                    flowBlock(pathFlow) { path ->
                        textBlock("Currently: streetlight.ing/g/$path", modify(OpacityMost))
                    }
                }
            ) {
                editorTextField("path", model::setPath, pathFlow, pathCharacters, GalaxyEdit.MAX_NAME_LENGTH)
            }
        }

        editorSection("Image") {
            editorPart(
                instructions = imageInstructions1,
                bullets = listOf(
                    "Ideally at least 1024 pixels wide and 512 pixels tall.",
                    imageRequirements,
                    canBeChangedText,
                )
            ) {
                imageDrop(blobFlow, model::setBlobUrl) {
                    box {
                        headerImage(model.stateNow.galaxy.name ?: "", it)
                    }
                }
            }
        }

        editorSection("Description") {
            editorPart(
                info = {
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
            ) {
                textEditor("description", onValue = model::setDescription, flow = descriptionFlow)
            }
        }

        editorSection("Map location") {
            editorPart(
                instructions = mapInstructions1,
                info = {
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
            ) {
                geoMapMount(
                    geoMap,
                    appScope,
                    modifiers = modify(Height48, BorderRadius2, OverflowClip, MoonShadow)
                )
            }
        }

        editorSection("Other details") {
            editorPart(
                instructions = "You can open up posting to the community or curate the content yourself.",
                bullets = listOf(canBeChangedText),
            ) {
                dropMenu(model::setPostPermission, { it.label }, flow = permissionFlow)
            }
            editorPart(
                instructions = permissionInfo1,
                bullets = listOf(
                    "You can extend the role of moderation to other community members.",
                    canBeChangedText
                ),
                info = {
                    flowBlock(permissionFlow) { permission ->
                        if (permission == PostPermission.Everyone) {
                            textBlock(anonymousInfoText)
                        }
                    }
                }
            ) {
                dropMenu(model::setReviewMode, { it.label }, flow = reviewModeFlow)
            }
            editorPart(
                instructions = "Provide guidelines or requirements for the content of community posts.",
                bullets = listOf(canBeChangedText, "Optional")
            ) {
                textEditor("Post Guide", onValue = model::setPostGuide, flow = guideFlow)
            }
        }

        row(modify(JustifyContentSpaceBetween)) {
            button("back", onClick = { portal.goBack() })
            row {
                messageBox(model.msg.flow)
                button("Found Galaxy", modify(Accent), onClick = model::foundGalaxy)
            }
        }

        appFooter("web/src/jsMain/kotlin/streetlight/web/ui/viewGalaxyEditor.kt")
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

private val cityInstructions = "What city does your galaxy focus on?"

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

private val anonymousInfoText =
    "Posts from users who are not signed in will always need review before appearing in the feed."