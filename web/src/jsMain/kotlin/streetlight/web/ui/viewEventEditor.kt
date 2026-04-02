package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.heading3
import koala.html.heading4
import koala.html.spacer
import koala.html.textBlock
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.ExtraLink
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

    viewContextOf(model) {
        column(modify(Gap4)) {
            column(modify(Gap0)) {
                heading3("What's happening?", modify(Padding1, Dim))
                card(modify(AlignItemsStretch)) {
                    primaryFields()
                    eventLinks()
                }
            }

            column(modify(Gap0)) {
                heading3("When?", modify(Padding1, Dim))
                card {
                    row {
                        row(modify(FlexItems1)) {
                            blockLabel("start time") {
                                timeInput(model.startTimeFlow, model::setStartTime)
                            }
                            blockLabel("end time") {
                                timeInput(model.endTimeFlow, model::setEndTime)
                            }
                            blockLabel("day") {
                                dateInput(model.dateFlow, model::setDate)
                            }
                        }
                        // textBlock(model.datetimeFlow.map { it.toString() })
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
}

fun ViewContext<EventEditor>.primaryFields() {
    column(modify(MediaMdRow)) {
        imageDrop(model.imageUrlFlow, model::setImageUrl, modify(Flex1, AspectRatio1))
        column(modify(Flex3)) {
            textField(
                label = "title",
                onValue = model::setEventTitle,
                flow = model.titleFlow,
                modifiers = modify(Width100P),
                placeholder = "Event Title"
            )
            row {
                flowBlock(model.isFreeFlow) { isFree ->
                    row {
                        checkBox("Free event", model::setFree, model.isFreeFlow)
                        if (!isFree) {
                            textField("cost", onValue = model::setCost, flow = model.costFlow)
                        }
                    }
                }

                textField(placeholder = "Add a tag")
            }
        }
    }
    textEditor(
        label = "description",
        placeholder = "Event description",
        onChangeValue = model::setDescription,
        bindFlow = model.descriptionFlow
    )
}

fun ViewContext<EventEditor>.eventLinks() {
    val linksFlow = model.stateFlow.mapDistinct { it.event.links ?: emptyList() }

    val editState = storeOf(LinkEditState())
    val linkEditIndexFlow = editState.flow.mapDistinct { it.index }
    val labelFlow = editState.flow.mapDistinct { it.link.label }
    val urlFlow = editState.flow.mapDistinct { it.link.url }

    fun setLink(provider: (ExtraLink) -> ExtraLink) { editState.set { it.copy(link = provider(it.link)) }}
    fun setLabel(value: String) { setLink { it.copy(label = value) } }
    fun setUrl(value: String) { setLink { it.copy(url = value) } }
    fun finalizeEdit() {
        val link = editState.now.link.takeIf { it.isValid } ?: return
        val index = editState.now.index ?: error("no edit index")
        model.editLink(index, link)
        editState.set { LinkEditState() }
    }
    fun addLink() {
        val index = model.stateNow.event.links?.size ?: 0
        model.addLink(ExtraLink.Blank)
        editState.set { it.copy(index = index) }
    }

    column() {
        textBlock(
            "Is there more information about this event somewhere out there? " +
                    "You can also try reading event details from the link.",
            modify(Dim)
        )
        textField("Link", modify(Width100P), model::setUrl, model.urlFlow)
        row {
            column(modify(Flex1)) {
                heading4("Additional Links")
                textBlock("You may provide your original source, a youtube video, or any useful link.", modify(Dim))
            }
            icon(SvgFile.Plus, ::addLink)
        }
        indexedItemsBlock(linksFlow, defaultMagic, magic = true) { (linkIndex, link) ->
            flowBlock(linkEditIndexFlow, modify(Height5, JustifyContentCenter)) { index ->
                val isEdit = index == linkIndex
                if (isEdit) {
                    row() {
                        textField("label", onValue = ::setLabel, flow = labelFlow)
                        textField("url", modify(Flex1), onValue = ::setUrl, flow = urlFlow)
                        icon(SvgFile.Check, ::finalizeEdit)
                    }
                } else {
                    row() {
                        textBlock(link.label)
                        textBlock(link.url, modify(Dim))
                        spacer(modify(Flex1))
                        icon(SvgFile.Trash, onClick = { model.removeLink(link) }, modify(Dim, Danger))
                        icon(SvgFile.Edit, onClick = { editState.set{ it.copy(index = linkIndex, link = link)} }, modify(Dim))
                    }
                }
            }
        }
    }
}

private data class LinkEditState(
    val index: Int? = null,
    val link: ExtraLink = ExtraLink("", ""),
)

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
        viewContextOf(model) {
            viewEventEditorPanel(event, callback)
        }
    }
}