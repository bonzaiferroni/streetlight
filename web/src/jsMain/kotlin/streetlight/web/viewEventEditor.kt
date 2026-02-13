package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.geoMapMount
import koala.html.textBlock
import koala.html.textSpan
import koala.model.mapDistinct
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

fun RenderContext.viewEventEditor(app: AppContext) {
    val model = app.eventEditor

    val element = column {
        card(modify(Width100, AlignItemsStretch)) {
            blockLabel("feature image") {
                imageChoice(
                    modifiers = modify(MinHeight8),
                    onUpload = { app.client.api.uploadFeatureImage(it) },
                    onValueChanged = model::setImageUrl,
                    urlFlow = model.urlFlow,
                    choicesFlow = model.userImagesFlow
                )
            }
            column(modify(QueryRow, AlignItemsStretch)) {
                row(modify(Flex1)) {
//                    blockLabel("icon", modify(Center, Square, Height100)) {
//                        imageChoice(Api.Events.Upload.path, modify(Size100))
//                    }
                    textField(
                        label = "title",
                        onChangeValue = model::setEventTitle,
                        modifiers = modify(Flex1),
                        textModifiers = modify(Heading2),
                        placeholder = "Event Title"
                    )
                }
                row {
                    this.textBlock("Event category:", modify(MarginLeft1, Dim))
                    dropMenu(model::setEventType, provideLabel = { it.label })
                }
            }
            row {
                textField(placeholder = "Add a tag")
            }
            row {
                row(modify(FlexItems1)) {
                    blockLabel("time") {
                        timeInput(model.timeFlow, model::setTime, modify(Width24))
                    }
                    blockLabel("day") {
                        dateInput(model.dateFlow, model::setDate, modify(Width24))
                    }
                }
                textBlock(model.datetimeFlow.map { it.toString() })
            }
            locationEditor(app)
            textEditor(
                label = "description",
                placeholder = "Event description",
                onChangeValue = model::setDescription,
                binding = model.descriptionFlow
            )
        }
        message(model.stateFlow.mapDistinct { it.message })
        row {
            button("cancel", onClick = {
                app.portal.goBack()
            })
            button("create", modify(Accent), onClick = {
                renderScope.launch {
                    val eventId = model.createEvent() ?: return@launch
                    app.portal.go(EventIdRoute(eventId))
                }
            })
        }
    }

    element.onView(model::setVisibility)
}

fun RenderContext.locationEditor(app: AppContext) {
    val eventCreator = app.eventEditor

    val element = column(modify(QueryRow, AlignItemsStretch)) {
        geoMapMount(modify(Flex1, Square))
        column(modify(Flex2, AlignItemsStretch)) {
            row {
                textField(
                    label = "location name",
                    placeholder = "Location name",
                    modifiers = modify(Flex1),
                    onChangeValue = eventCreator::setLocationName,
                    binding = eventCreator.locationFlow,
                )
                button("look up", onClick = {
                    eventCreator.queryLocation()
                })
            }
            textField(
                label = "address",
                placeholder = "Address",
                modifiers = modify(Width100),
                onChangeValue = eventCreator::setAddress,
                binding = eventCreator.addressFlow,
            )
            flowBlock(eventCreator.pointFlow, animate = true, modifiers = modify(MagicBlur)) { point ->
                this.textBlock {
                    textSpan("latitude: ", modify(Dim))
                    textSpan(point?.lat?.toString() ?: "--" )
                    textSpan(" longitude: ", modify(Dim))
                    textSpan(point?.lng?.toString() ?: "--" )
                }
            }
        }
    }

    wireGeoMap(app.geoMap, app.appScope, element)
}