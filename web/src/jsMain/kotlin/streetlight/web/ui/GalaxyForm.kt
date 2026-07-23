package streetlight.web.ui

import kabinet.utils.format
import kampfire.api.Slug
import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.span
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.GalaxyProperty
import streetlight.model.data.PostPermission
import streetlight.web.model.GalaxyEditor

fun ViewScope.galaxyCityForm(model: GalaxyEditor) {
    // val cityQueryFlow = model.stateFlow.dedup { it.cityQuery }
    // val isLocalFlow = model.stateFlow.dedup { it.isLocal }
    // val localitiesFlow = model.stateFlow.dedup { it.cities }
    // val countryFlow = model.stateFlow.dedup { it.country }
    // val localityFlow = model.stateFlow.dedup { it.city }

    formCardSection("City") {
        formPart(
            instructions = "Would you like your galaxy to focus on a city?",
        ) {
            checkBox(model.isLocalField, "This galaxy has a city", modify(Padding1))
        }

        formPart(
            instructions = cityInstructions,
        ) {
            row {
                textField(model.cityQueryField, "city search", modify(Flex1))
                textField(model.countryField, "country", modify(Width24))
            }
            column(modify(Height32, OverflowYAuto, Gap0)) {
                row(modify(Padding1, JustifyContentSpaceBetween)) {
                    textBlock("city", modify(OpacityLow, Italic))
                    textBlock("galaxies", modify(OpacityLow, Italic))
                }
                selectionBlock(model.citiesField, model.cityField) { city ->
                    card(modify(BorderRadius1)) {
                        row(modify(JustifyContentSpaceBetween)) {
                            textBlock("${city.name}, ${city.state}", modify(Flex1))
                            textBlock(city.galaxyCount.toString())
                        }
                    }
                }
            }
        }.flowDisplay(model.isLocalField.flow, contentScope)
    }
}

fun ViewScope.galaxyNameForm(model: GalaxyEditor) {
    // val nameFlow = model.galaxyFlow.dedup { it.name ?: "" }
    // val slugFlow = model.galaxyFlow.dedup { it.slug?.value ?: "" }

    formCardSection("Galaxy Name") {
        formPart(
            instructions = nameInstructions1,
            bullets = listOf("Denver Book Club", "Page Turners"),
            bulletsHeading = "Examples"
        ) {
            formTextField(
                field = model.nameField,
                label = GalaxyProperty.Name,
                footnote = nameCharacters,
                maxLength = Slug.MAX_LENGTH
            ).flowValid(GalaxyProperty.Name, model.validityField, contentScope)
        }
        formPart(
            instructions = pathInstructions,
            info = {
                flowBlock(model.slugField) { path ->
                    textBlock("Currently: streetlight.ing/g/$path", modify(OpacityHigh))
                }
            }
        ) {
            formTextField(
                field = model.slugField,
                label = GalaxyProperty.Path,
                footnote = pathCharacters,
                maxLength = Slug.MAX_LENGTH
            ).flowValid(GalaxyProperty.Path, model.validityField, contentScope)
        }
    }
}

fun ViewScope.galaxyImageForm(model: GalaxyEditor) = imageFormSection(imageInstructions1, model.imageEditor)

private val imageInstructions1 = "This image will appear at the top of the galaxy page."

fun ViewScope.galaxyDescriptionForm(model: GalaxyEditor) {
    // val nameFlow = model.galaxyFlow.dedup { it.name ?: "" }
    // val descriptionFlow = model.galaxyFlow.dedup { it.description ?: "".toMarkdown() }
    // val taglineFlow = model.galaxyFlow.dedup { it.tagline ?: "" }
    // val guideFlow = model.galaxyFlow.dedup { it.postGuide ?: "".toMarkdown() }

    formCardSection("Description") {
        formPart(
            info = {
                flowBlock(model.nameField) { name ->
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
            textEditor(model.descriptionField, "description")
        }
        formPart(
            instructions = "Give your galaxy a tagline.",
        ) {
            formTextField(model.taglineField, "tagline", maxLength = 50)
        }
        formPart(
            instructions = "Provide guidelines or requirements for the content of community posts.",
        ) {
            textEditor(model.postGuideField, "Post Guide")
        }
    }
}

fun ViewScope.galaxyLocationForm(model: GalaxyEditor) {

    formCardSection("Map location") {
        formPart(
            instructions = mapInstructions1,
            info = {
                flowBlock(geoCamera.pointAndZoom) { (point, zoom) ->
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
    // val permissionFlow = model.galaxyFlow.dedup { it.postPermission }
    // val reviewCountFlow = model.galaxyFlow.dedup { it.reviewCount?.toString() ?: "" }

    formCardSection("Permissions") {
        formPart(
            instructions = "You can open up posting to the community or curate the content yourself.",
        ) {
            dropMenu(model.permissionField)
        }
        formPart(
            instructions = permissionInfo1,
            bullets = listOf(
                "You can extend the role of moderation to other community members. (Feature in progress)",
            ),
            info = {
                flowBlock(model.permissionField) { permission ->
                    if (permission == PostPermission.Everyone) {
                        textBlock(anonymousInfoText)
                    }
                }
            }
        ) {
            textField(model.reviewCountField, "review count", modify(AlignSelfStart))
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