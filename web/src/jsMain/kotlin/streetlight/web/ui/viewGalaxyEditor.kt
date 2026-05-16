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
import streetlight.model.data.GalaxyProperty
import streetlight.model.data.PostPermission
import streetlight.web.model.GalaxyEditor

fun RenderContext.viewGalaxyEditor(model: GalaxyEditor) {
    val geoMap = app.get<GeoMap>()

    val nameFlow = model.galaxyFlow.mapDistinct { it.name ?: "" }
    val blobFlow = model.stateFlow.mapDistinct { it.blobUrl }
    val descriptionFlow = model.galaxyFlow.mapDistinct { it.description ?: "" }
    val taglineFlow = model.galaxyFlow.mapDistinct { it.tagline ?: "" }
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
    val invalidPartsFlow = model.galaxyFlow.mapDistinct { it.invalidParts }

    val bulletsMod = modify(OpacityMost)

    column(modify(Gap8)) {

        editorSection("City") {
            editorPart(
                instructions = "Would you like your galaxy to focus on a city?",
            ) {
                checkBox("This galaxy has a city", model::setIsLocal, isLocalFlow, modify(Padding1))
            }

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
            }.flowVisibility(isLocalFlow, renderScope)
        }

        editorSection("Galaxy Name") {
            editorPart(
                instructions = nameInstructions1,
                examples = listOf("Denver Book Club", "Page Turners"),
            ) {
                editorTextField(
                    label = GalaxyProperty.Name,
                    onValue = model::setName,
                    flow = nameFlow,
                    modifiers = modify(Required),
                    footnote = nameCharacters,
                    maxLength = GalaxyEdit.MAX_NAME_LENGTH
                ).flowValid(GalaxyProperty.Name, invalidPartsFlow, renderScope)
            }
            editorPart(
                instructions = pathInstructions,
                info = {
                    flowBlock(pathFlow) { path ->
                        textBlock("Currently: streetlight.ing/g/$path", modify(OpacityMost))
                    }
                }
            ) {
                editorTextField(
                    label = GalaxyProperty.Path,
                    onValue = model::setPath,
                    flow = pathFlow,
                    modifiers = modify(Required),
                    footnote = pathCharacters,
                    maxLength = GalaxyEdit.MAX_NAME_LENGTH
                ).flowValid(GalaxyProperty.Path, invalidPartsFlow, renderScope)
            }
        }

        editorSection("Image") {
            editorPart(
                instructions = imageInstructions1,
                bullets = listOf(
                    "Ideally at least 1024 pixels wide and 512 pixels tall.",
                    imageRequirements,
                )
            ) {
                imageDrop(blobFlow, model::setBlobUrl) {
                    box {
                        headerImage(model.stateNow.galaxy.name ?: "", it)
                    }
                }.setBlockLabel("image")
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
                        "This information appears at the top of the feed."
                    )
                }
            ) {
                textEditor("description", onValue = model::setDescription, flow = descriptionFlow)
            }
            editorPart(
                instructions = "Give your galaxy a tagline.",
            ) {
                editorTextField("tagline", model::setTagline, taglineFlow, maxLength = 100)
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
            ) {
                dropMenu(model::setPostPermission, { it.label }, flow = permissionFlow)
            }
            editorPart(
                instructions = permissionInfo1,
                bullets = listOf(
                    "You can extend the role of moderation to other community members.",
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
            ) {
                textEditor("Post Guide", onValue = model::setPostGuide, flow = guideFlow)
            }
        }
    }
}

private val cityInstructions = "What city does your galaxy focus on?"

private val nameInstructions1 = "Let's give the galaxy a name, up to ${GalaxyEdit.MAX_NAME_LENGTH} characters."

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