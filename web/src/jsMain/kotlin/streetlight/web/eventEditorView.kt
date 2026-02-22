package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.geoMapMount
import koala.html.heading3
import koala.html.textBlock
import koala.html.textSpan
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

fun RenderContext.eventEditorView(app: AppContext) {
    val model = EventEditor(renderScope, app.client, app.geoMap)
    val parser = EventParser(renderScope, app.client.api)

    val dialog = eventParserDialog(parser)

    renderScope.launch {
        launch {
            app.portal.routeFlowOf<EditEventRoute>().collect { route ->
                route.eventId?.let {
                    model.initEvent(it)
                }
            }
        }
        launch {
            parser.selectionFlow.collect(model::setEventParse)
        }
    }

    val element = column(modify(Gap4)) {
        column(modify(Gap0)) {
            heading3("What's happening?", modify(Padding1, Dim))
            card(modify(AlignItemsStretch)) {
                blockLabel("feature image") {
                    imageChoice(
                        modifiers = modify(MinHeight8),
                        onUpload = { app.client.api.uploadFeatureImage(it) },
                        onValueChanged = model::setImageUrl,
                        urlFlow = model.imageUrlFlow,
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
                            values = model.titleFlow,
                            modifiers = modify(Flex1),
                            textModifiers = modify(Heading2),
                            placeholder = "Event Title"
                        )
                    }
                    row(modify(WidthAuto)) {
                        this.textBlock("Event category:", modify(MarginLeft1, Dim))
                        dropMenu(model::setEventType, provideLabel = { it.label })
                    }
                }
                row {
                    textField(placeholder = "Add a tag")
                }
                textEditor(
                    label = "description",
                    placeholder = "Event description",
                    onChangeValue = model::setDescription,
                    binding = model.descriptionFlow
                )

                column() {
                    textBlock(
                        "Is there more information about this event somewhere out there? " +
                                "You can also try reading event details from the link.",
                        modify(Dim)
                    )
                    row {
                        textField("Link", modify(Flex1), model::setUrl, model.urlFlow)
                        button("read", onClick = { parser.readUrl(model.eventNow.url) })
                    }
                }
            }
        }

        column(modify(Gap0)) {
            column(modify(Gap0, Padding1, Dim)) {
                heading3("Where?")
                textBlock("You can choose from existing locations or provide a new one.")
            }
            card {
                locationEditor(app, model)
            }
        }

        column(modify(Gap0)) {
            heading3("When?", modify(Padding1, Dim))
            card {
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
            }
        }

        column(modify(Gap0)) {
            heading3("Who?", modify(Padding1, Dim))
            card {
                textBlock("yer who")
            }
        }

        card {
            messageBox(model.messageFlow, modify(Flex1))
            row {
                button("cancel", onClickEvent = {
                    app.portal.goBack()
                })
                button("create", modify(Accent), onClickEvent = {
                    renderScope.launch {
                        val eventId = model.saveEvent() ?: return@launch
                        app.portal.go(EventIdRoute(eventId))
                    }
                })
            }
        }
    }

    element.onView(model::setVisibility)
}

fun RenderContext.locationEditor(app: AppContext, model: EventEditor) {

    val element = column(modify(QueryRow, AlignItemsStretch)) {
        geoMapMount(modify(Flex1, Square))
        column(modify(Flex2, AlignItemsStretch)) {
            row {
                textField(
                    label = "location name",
                    placeholder = "Location name",
                    modifiers = modify(Flex1),
                    onChangeValue = model::setLocationName,
                    values = model.locationFlow,
                )
                button("check map", onClickEvent = {
                    model.queryLocation()
                })
            }
            textField(
                label = "address",
                placeholder = "Address",
                modifiers = modify(Width100),
                onChangeValue = model::setAddress,
                values = model.addressFlow,
            )
            flowBlock(model.pointFlow, animate = true, modifiers = modify(MagicBlur)) { point ->
                this.textBlock {
                    textSpan("latitude: ", modify(Dim))
                    textSpan(point.lat.toString())
                    textSpan(" longitude: ", modify(Dim))
                    textSpan(point.lng.toString())
                }
            }
        }
    }

    wireGeoMap(app.geoMap, app.appScope, element)
}