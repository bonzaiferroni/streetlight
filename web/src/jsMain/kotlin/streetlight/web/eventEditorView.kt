package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.textBlock
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.toEdit

fun RenderContext.eventEditorView(
    model: EventEditor,
    app: AppContext,
    callback: ((Event?) -> Unit)?
) {
    val imagesFlow = app.userCache.files.flow

    column(modify(Gap4)) {
        column(modify(Gap0)) {
            heading3("What's happening?", modify(Padding1, Dim))
            card(modify(AlignItemsStretch)) {
                blockLabel("feature image") {
                    imageChoice(
                        modifiers = modify(MinHeight8),
                        onUpload = { app.client.api.uploadFile(it) },
                        onValueChanged = model::setImageUrl,
                        urlFlow = model.imageUrlFlow,
                        choicesFlow = imagesFlow
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
                    textField("Link", modify(Width100), model::setUrl, model.urlFlow)
                }
            }
        }

        column(modify(Gap0)) {
            column(modify(Gap0, Padding1, Dim)) {
                heading3("Where?")
                textBlock("You can choose from existing locations or provide a new one.")
            }
            card {
                placeEditor(null, app, model)
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
            messageBox(model.message.flow, modify(Flex1))
            row {
                button("cancel", onClickEvent = {
                    app.portal.goBack()
                })
                button("create", modify(Accent), onClickEvent = {
                    renderScope.launch {
                        val event = model.saveEvent() ?: return@launch
                        if (callback != null) {
                            app.portal.goBack()
                            callback.invoke(event)
                        } else {
                            app.portal.go(EventIdRoute(event.eventId))
                        }
                    }
                })
            }
        }
    }
}

fun RenderContext.eventEditorRouteView(app: AppContext) {
    val api = app.client.api
    var callback: ((Event?) -> Unit)? = null

    routeBlock<EditEventRoute, EventEdit>(app.portal, { route ->
        when (route) {
            is EditEventIdRoute -> route.eventId?.let {
                api.readEvent(it)?.toEdit()
            } ?: EventEdit()
            is EditEventCallbackRoute -> {
                callback = route.callback
                route.event
            }
        }
    }) { event ->
        val model = EventEditor(event, renderScope, app.client)
        eventEditorView(model, app, callback)
    }
}