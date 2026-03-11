package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.textBlock
import koala.model.storeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.toEdit
import streetlight.web.EditEventCallbackRoute
import streetlight.web.EditEventIdRoute
import streetlight.web.EditEventRoute
import streetlight.web.EventIdRoute
import streetlight.web.model.Streetlight
import streetlight.web.model.EventEditor

fun RenderContext.viewEventEditor(
    event: EventEdit?,
    app: Streetlight,
    bindFlow: Flow<EventEdit>? = null,
    onEdit: ((EventEdit) -> Unit)? = null,
) {
    val model = EventEditor(event, renderScope, app.client)

    onEdit?.let {
        renderScope.launch {
            model.editFlow.collect {
                onEdit(it)
            }
        }
    }

    bindFlow?.let {
        renderScope.launch {
            it.collect { edit ->
                model.setEdit(edit)
            }
        }
    }

    column(modify(Gap4)) {
        column(modify(Gap0)) {
            heading3("What's happening?", modify(Padding1, Dim))
            card(modify(AlignItemsStretch)) {
                column(modify(QueryRow)) {
                    imageDrop(model.imageUrlFlow, model::setImageUrl, modify(Flex1, Square))
                    column(modify(Flex3)) {
                        textField(
                            label = "title",
                            onChangeValue = model::setEventTitle,
                            bindFlow = model.titleFlow,
                            modifiers = modify(Width100),
                            textModifiers = modify(Heading2),
                            placeholder = "Event Title"
                        )
                        textField(placeholder = "Add a tag")
                    }
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
    }
}

fun ViewContext<Streetlight>.viewEventEditorPanel(
    event: EventEdit,
    callback: ((Event?) -> Unit)?
) {
    val msg = storeOf(UIMessage())
    val editStore = storeOf(event)

    column {
        viewEventEditor(event, model, editStore.flow, editStore::setValue)

        card {
            messageBox(msg.flow, modify(Flex1))
            row {
                button("cancel", onClickEvent = {
                    model.portal.goBack()
                })
                button("create", modify(Accent), onClickEvent = {
                    renderScope.launch {
                        val response = api.createOrEditEvent(editStore.now)
                        val savedEvent = response?.payload
                        if (savedEvent == null) {
                            msg.set("Unable to create event: ${response?.reason}")
                            return@launch
                        }
                        if (callback != null) {
                            model.portal.goBack()
                            callback.invoke(savedEvent)
                        } else {
                            model.portal.go(EventIdRoute(savedEvent.eventId))
                        }
                    }
                })
            }
        }
    }
}

fun ViewContext<Streetlight>.viewEventEditorRoute() {
    var callback: ((Event?) -> Unit)? = null

    routeBlock<EditEventRoute, EventEdit>({ route ->
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
        viewOf(model) {
            viewEventEditorPanel(event, callback)
        }
    }
}