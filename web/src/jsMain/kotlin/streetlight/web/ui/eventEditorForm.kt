package streetlight.web.ui

import kabinet.utils.toRelativeDayFormat
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.ButtonPopover
import koala.html.bulletsOf
import koala.html.buttonPopover
import koala.html.filigree
import koala.html.heading3
import koala.html.heading4
import koala.html.markdown
import koala.html.spacer
import koala.html.textBlock
import koala.html.textProperty
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import streetlight.model.data.EventEdit
import streetlight.model.data.ExtraLink
import streetlight.web.model.EventEditor
import streetlight.web.model.Streetlight
import kotlin.time.Clock

fun RenderContext.eventEditorForm(
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

        column(modify(AlignItemsStretch, QueryContainer, Gap4)) {

            column(modify(ContainerMdRow)) {

                // provide event image
                column(modify(Flex1)) {
                    filigree {
                        heading3("Image")
                    }
                    imageDrop(model.imageUrlFlow, model::setImageRef, modify(AspectRatio1, BorderRadius1))
                }

                column(modify(Flex2, Gap4)) {
                    column {
                        filigree {
                            heading3("Event title and cost")
                        }
                        // provide event title
                        textField(
                            label = "title",
                            onValue = model::setEventTitle,
                            flow = model.titleFlow,
                            modifiers = modify(Width100P),
                            placeholder = "Event Title"
                        )
                        row {

                            // provide event cost
                            row {
                                textField("cost", modify(Width12), onValue = model::setCost, flow = model.costFlow)
                                checkBox("Free event", model::setFree, model.isFreeFlow)
                            }

                            // td: implement event tagging
                            // textField(placeholder = "Add a tag")
                        }
                    }

                    column {
                        filigree {
                            heading3("When though?")
                        }
                        row(modify(AlignItemsCenter, JustifyContentCenter)) {
                            blockLabel("start time") {
                                timeInput(model.startTimeFlow, model::setStartTime)
                            }
                            // end time is optional, not every event has a fixed end time
                            blockLabel("end time") {
                                timeInput(model.endTimeFlow, model::setEndTime)
                            }
                            blockLabel("day") {
                                dateInput(model.dateFlow, model::setDate)
                            }
                        }
                        flowBlock(model.startsAtFlow, modify(FlexColumn, AlignItemsCenter)) {
                            val startsAt = it ?: return@flowBlock
                            // must be boxed due to FlowContent context
                            val dayDescription = when (startsAt < Clock.System.now()) {
                                true -> "in the past"
                                else -> startsAt.toRelativeDayFormat()
                            }
                            box {
                                heading4("That's $dayDescription.")
                            }
                        }
                    }
                }
            }

            // provide event description
            // td: render markdown hints in editor
            column {
                filigree {
                    heading3("Description")
                }
                column(modify(OpacityMost, AlignItemsCenter)) {
                    textBlock(
                        "Tell us all about the event. Markdown features currently supported: Headings, paragraphs.",
                    )
                    textBlock("If you use content from the source, it is respectful to get permission. You may even gain a contact.")
                }
                textEditor(
                    label = "description",
                    placeholder = "Event description",
                    // 16 rows are default as roughly the desired content length, field can be resized
                    rows = 16,
                    onValue = model::setDescription,
                    flow = model.descriptionFlow
                )
                row(modify(JustifyContentSpaceBetween)) {
                    buttonPopover("Markdown Hints", emoji = "💡") {
                        card(modify(ButtonPopover.CardMod, Padding2)) {
                            bulletsOf(
                                "Add a blank line in between paragraphs.",
                                "Use # symbols at the beginning of a line to provide a heading.",
                                "One # provides the largest heading, two provides the next largest, etc.",
                            )
                            textProperty("Example", "### My Fancy Heading")
                            textProperty("Becomes") {
                                heading3("My Fancy Heading")
                            }
                        }
                    }
                    buttonDialog("Preview", emoji = "👀") {
                        flowBlock(model.descriptionFlow) {
                            box(modify(Padding2)) {
                                markdown(it)
                            }
                        }
                    }
                }
            }

            // provide additional links
            column {
                filigree {
                    heading3("Event Links")
                }
                eventLinks()
            }

            // td: provide a way to change the event location, in case it changes or the initial location was mistaken
            // td: provide info if the event is limited by age or otherwise
            // column(modify(Gap0)) {
            //     heading3("Who?", modify(Padding1, Dim))
            //     card {
            //         textBlock("yer who")
            //     }
            // }
        }
    }
}

private fun ViewContext<EventEditor>.eventLinks() {
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

    column(modify(Gap2)) {
        column(modify(Gap1)) {
            textBlock(
                "Is there more information about this event somewhere out there?",
                modify(OpacityMost)
            )
            textField("Link", modify(Width100P), model::setUrl, model.urlFlow)
        }
        row(modify(AlignItemsEnd)) {
            column(modify(Flex1)) {
                heading4("Additional Links")
                textBlock("You may provide your original source, a youtube video, or any useful link.", modify(Dim))
            }
            button("➕ Add link", onClick = ::addLink)
        }
        indexedItemsBlock(linksFlow, defaultMagic) { (linkIndex, link) ->
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
