package streetlight.web.ui

import kabinet.utils.format
import kampfire.api.Slug
import kampfire.model.mutableTapFirstBy
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import koala.html.bulletsOf
import streetlight.model.data.GalaxyEdit
import streetlight.model.data.GalaxyProperty
import streetlight.web.model.GalaxyEditor

fun ViewScope.galaxyNameFormRow(model: GalaxyEditor) = formRow {
    formSection("Name") {
        formTextField(
            field = model.nameField,
            label = GalaxyProperty.Name,
            footnote = nameCharacters,
            maxLength = Slug.MAX_LENGTH
        ).flowValid(GalaxyProperty.Name, model.validityField, contentScope)
    }
    formSection("Url") {
        formField {
            formTextField(
                field = model.slugField,
                label = GalaxyProperty.Path,
                footnote = pathCharacters,
                maxLength = Slug.MAX_LENGTH
            ).flowValid(GalaxyProperty.Path, model.validityField, contentScope)

            flowBlock(model.slugField) { path ->
                textBlock("Currently: streetlight.ing/g/$path", OpacityHigh)
            }
        }
    }
}

fun ViewScope.galaxyDescriptionFormRow(model: GalaxyEditor) = formRow {
    formSection("Description") {
        textEditor(model.descriptionField, "description")
        bulletsOf(
            FormMod.Bullets,
            "Can be brief or detailed.",
            "This information appears at the top of the feed."
        )
    }

    galaxyImageSection(model)

    formSection("Tagline") {
        formTextField(model.taglineField, "tagline", maxLength = 50)
    }

    formSection("Post Guide") {
        formField {
            textEditor(model.postGuideField, "Post Guide")
            centeredText("Provide guidelines or requirements for the content of community posts.")
        }
    }

    formSection("Marks") {
        column {
            val newMark = storeOf("")
            row {
                textField(newMark, "Mark", Flex1)
                button("add mark", {
                    launchEffect {
                        if (model.addMark(newMark.now)) { return@launchEffect }
                        newMark.set { "" }
                    }
                })
            }
            itemsBlock(model.markIdsState) { markId ->
                val markState = model.marksState.mutableTapFirstBy { it.markId == markId }
                val leanState = markState.mutableTapOf({ it.lean }) { copy(lean = it) }
                val nameState = markState.mutableTapOf({ it.name }) { copy(name = it) }

                row(modify(AlignItemsCenter, ZenBg, BorderRadius2, Padding(1))) {
                    textField(nameState, "name", Flex1)
                    dropMenu(leanState)
                    button(SvgFile.X, {
                        model.removeMark(markId)
                    })
                }
            }
        }
    }
}

fun ViewScope.galaxyMapFormRow(model: GalaxyEditor) = formRow {
    formSection("Map Location") {
        formField {
            geoMapMount(mod = FormMod.GeoMap)
            centeredText("Choose the point on the map and zoom level.")

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
    }

    formSection("City") {
        checkBox(model.isLocalField, "This galaxy has a city", Padding(1))

        formField {
            row {
                textField(model.cityQueryField, "city search", Flex1)
                textField(model.countryField, "country", Width(24))
            }
            column(modify(Height(32), OverflowYAuto, Gap0)) {
                row(modify(Padding(1), JustifyContentSpaceBetween)) {
                    textBlock("city", modify(OpacityLow, Italic))
                    textBlock("galaxies", modify(OpacityLow, Italic))
                }
                selectionBlock(model.citiesField, model.cityField) { city ->
                    card(BorderRadius1) {
                        row(JustifyContentSpaceBetween) {
                            textBlock("${city.name}, ${city.state}", Flex1)
                            textBlock(city.galaxyCount.toString())
                        }
                    }
                }
            }
        }.flowIsDisplayed(model.isLocalField.flow, contentScope)
    }
}

fun ViewScope.galaxyImageSection(model: GalaxyEditor) = imageFormSection(imageInstructions1, model.imageEditor)

fun ViewScope.galaxyAccessFormRow(model: GalaxyEditor) = formRow {
    formSection("Permissions") {
        formField {
            dropMenu(model.permissionField)
            centeredText("You can open up posting to the community or curate the content yourself.")
        }

        // formField {
        //     textField(model.reviewCountField, "review count", modify(AlignSelfStart))
        // }
    }

    column { }
}

private val imageInstructions1 = "This image will appear at the top of the galaxy page."

private val nameCharacters = "Available: letters, numbers, spaces, and ${GalaxyEdit.NameCharacters.joinToString(" ")}"

private val pathInstructions = "The path determines the web address of the galaxy."
private val pathCharacters = "Available: letters, numbers, and ${GalaxyEdit.PathCharacters.joinToString(" ")}"



private val permissionInfo1 = "You can choose to review posts before they appear in the feed."

private val anonymousInfoText =
    "Posts from users who are not signed in will always need review before appearing in the feed."