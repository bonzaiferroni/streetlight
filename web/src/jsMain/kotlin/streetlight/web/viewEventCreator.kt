package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.paragraph
import koala.html.textSpan
import kotlinx.coroutines.flow.map

fun RenderContext.viewEventCreator(app: AppContext) {
    val model = app.home.eventCreator

    column {
        card(modify(Width100, AlignItemsStretch)) {
            blockLabel("feature image") {
                imageChoice(
                    modifiers = modify(MinHeight8),
                    onUpload = { app.client.event.uploadFeatureImage(it) },
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
                    paragraph("Event category:", modify(MarginLeft1, Dim))
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
            textField(5, "description", placeholder = "Event description")
        }
        message(model.stateFlow.mapDistinct { it.message })
        row {
            button("cancel", onClick = {
                app.portal.goBack()
            })
            button("create", modify(Accent), onClick = {
                model.createEvent()
            })
        }
    }
}

fun RenderContext.locationEditor(app: AppContext) {
    val eventCreator = app.home.eventCreator

    column(modify(QueryRow, AlignItemsStretch)) {
        viewGeoMap(app.home.geoMap, modify(Flex1, Square))
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
                textBlock {
                    textSpan("latitude: ", modify(Dim))
                    textSpan(point?.lat?.toString() ?: "--" )
                    textSpan(" longitude: ", modify(Dim))
                    textSpan(point?.lng?.toString() ?: "--" )
                }
            }
        }
    }
}