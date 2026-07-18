package streetlight.web.ui

import kabinet.utils.format
import kampfire.api.Slug
import kampfire.api.toMarkdown
import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.span
import koala.model.tap
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.GalaxyProperty
import streetlight.model.data.PostPermission
import streetlight.web.model.GalaxyEditor

fun ViewScope.galaxyCityForm(model: GalaxyEditor) {
    val cityQueryFlow = model.stateFlow.tap { it.cityQuery }
    val isLocalFlow = model.stateFlow.tap { it.isLocal }
    val localitiesFlow = model.stateFlow.tap { it.cities }
    val countryFlow = model.stateFlow.tap { it.country }
    val localityFlow = model.stateFlow.tap { it.city }

    formCardSection("City") {
        formPart(
            instructions = "Would you like your galaxy to focus on a city?",
        ) {
            checkBox("This galaxy has a city", model::setIsLocal, isLocalFlow, modify(Padding1))
        }

        formPart(
            instructions = cityInstructions,
        ) {
            row {
                textField("city search", model::setCityQuery, cityQueryFlow, modify(Flex1))
                textField("country", model::setCountry, countryFlow, modify(Width24))
            }
            column(modify(Height32, OverflowYAuto, Gap0)) {
                row(modify(Padding1, JustifyContentSpaceBetween)) {
                    textBlock("city", modify(OpacityLow, Italic))
                    textBlock("galaxies", modify(OpacityLow, Italic))
                }
                selectionBlock(localitiesFlow, model::setCity, localityFlow) { city ->
                    card(modify(BorderRadius1)) {
                        row(modify(JustifyContentSpaceBetween)) {
                            textBlock("${city.name}, ${city.state}", modify(Flex1))
                            textBlock(city.galaxyCount.toString())
                        }
                    }
                }
            }
        }.flowDisplay(isLocalFlow, scope)
    }
}

fun ViewScope.galaxyNameForm(model: GalaxyEditor) {
    val nameFlow = model.galaxyFlow.tap { it.name ?: "" }
    val slugFlow = model.galaxyFlow.tap { it.slug?.value ?: "" }

    formCardSection("Galaxy Name") {
        formPart(
            instructions = nameInstructions1,
            bullets = listOf("Denver Book Club", "Page Turners"),
            bulletsHeading = "Examples"
        ) {
            formTextField(
                label = GalaxyProperty.Name,
                onValue = model::setName,
                flow = nameFlow,
                footnote = nameCharacters,
                maxLength = Slug.MAX_LENGTH
            ).flowValid(GalaxyProperty.Name, model.validityFlow, scope)
        }
        formPart(
            instructions = pathInstructions,
            info = {
                flowBlock(slugFlow) { path ->
                    textBlock("Currently: streetlight.ing/g/$path", modify(OpacityHigh))
                }
            }
        ) {
            formTextField(
                label = GalaxyProperty.Path,
                onValue = model::setSlug,
                flow = slugFlow,
                footnote = pathCharacters,
                maxLength = Slug.MAX_LENGTH
            ).flowValid(GalaxyProperty.Path, model.validityFlow, scope)
        }
    }
}

fun ViewScope.galaxyImageForm(model: GalaxyEditor) = imageFormSection(imageInstructions1, model.imageEditor)

private val imageInstructions1 = "This image will appear at the top of the galaxy page."

fun ViewScope.galaxyDescriptionForm(model: GalaxyEditor) {
    val nameFlow = model.galaxyFlow.tap { it.name ?: "" }
    val descriptionFlow = model.galaxyFlow.tap { it.description ?: "".toMarkdown() }
    val taglineFlow = model.galaxyFlow.tap { it.tagline ?: "" }
    val guideFlow = model.galaxyFlow.tap { it.postGuide ?: "".toMarkdown() }

    formCardSection("Description") {
        formPart(
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
                box {
                    bulletsOf(
                        FormMod.Bullets,
                        "Can be brief or detailed.",
                        "This information appears at the top of the feed."
                    )
                }
            }
        ) {
            textEditor("description", onValue = model::setDescription, flow = descriptionFlow)
        }
        formPart(
            instructions = "Give your galaxy a tagline.",
        ) {
            formTextField("tagline", model::setTagline, taglineFlow, maxLength = 50)
        }
        formPart(
            instructions = "Provide guidelines or requirements for the content of community posts.",
        ) {
            textEditor("Post Guide", onValue = model::setPostGuide, flow = guideFlow)
        }
    }
}

fun ViewScope.galaxyLocationForm(model: GalaxyEditor) {
    val pointFlow = geoMap.stateFlow.tap { it.center to it.zoom }

    formCardSection("Map location") {
        formPart(
            instructions = mapInstructions1,
            info = {
                flowBlock(pointFlow) { (point, zoom) ->
                    box {
                        bulletsOf(
                            FormMod.Bullets,
                            "latitude: ${point.lat.toFloat().format(4)}",
                            "longitude: ${point.lng.toFloat().format(4)}",
                            "zoom: ${zoom.format(1)}",
                        )
                    }
                }
            }
        ) {
            geoMapMount(mod = FormMod.GeoMap)
        }
    }
}

fun ViewScope.galaxyAccessForm(model: GalaxyEditor) {
    val permissionFlow = model.galaxyFlow.tap { it.postPermission }
    val reviewCountFlow = model.galaxyFlow.tap { it.reviewCount?.toString() ?: "" }

    formCardSection("Permissions") {
        formPart(
            instructions = "You can open up posting to the community or curate the content yourself.",
        ) {
            dropMenu(model::setPostPermission, { it.label }, flow = permissionFlow)
        }
        formPart(
            instructions = permissionInfo1,
            bullets = listOf(
                "You can extend the role of moderation to other community members. (Feature in progress)",
            ),
            info = {
                flowBlock(permissionFlow) { permission ->
                    if (permission == PostPermission.Everyone) {
                        textBlock(anonymousInfoText)
                    }
                }
            }
        ) {
            textField("review count", { model.setReviewCount(it.toIntOrNull()) }, reviewCountFlow, modify(AlignSelfStart))
        }
    }
}

private val cityInstructions = "What city does your galaxy focus on?"

private val nameInstructions1 = "Let's give the galaxy a name, up to ${Slug.MAX_LENGTH} characters."

private val nameCharacters = "Available: letters, numbers, spaces, and ${GalaxyEdit.NameCharacters.joinToString(" ")}"

private val pathInstructions = "The path determines the web address of the galaxy."
private val pathCharacters = "Available: letters, numbers, and ${GalaxyEdit.PathCharacters.joinToString(" ")}"

private val mapInstructions1 = """
Choose the point on the map and zoom level that people will see first. They can move around from there.
"""

private val permissionInfo1 = "You can choose to review posts before they appear in the feed."

private val anonymousInfoText =
    "Posts from users who are not signed in will always need review before appearing in the feed."