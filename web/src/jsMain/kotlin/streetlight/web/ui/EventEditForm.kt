package streetlight.web.ui

import kabinet.utils.toRelativeDayFormat
import koala.css.*
import koala.dom.*
import koala.html.ButtonPopover
import koala.html.bulletsOf
import koala.html.buttonPopover
import koala.html.heading3
import koala.html.heading4
import koala.html.markdown
import koala.html.textProperty
import streetlight.web.model.EventEditor
import kotlin.time.Clock

fun RenderContext.eventEditFormBody(model: EventEditor) = formBody {
    eventDetailsForm(model)
    eventImageForm(model)
    eventLinksForm(model)
}

fun RenderContext.eventDetailsForm(model: EventEditor) = formCardSection("Event Details") {
    formPart("What is the name of the event?") {
        formTextField("title", model::setTitle, model.titleFlow, maxLength = 50)
    }
    formPart("How much does it cost?") {
        row {
            checkBox("Free event", model::setFree, model.isFreeFlow)
            textField("cost", modify(Width12), onValue = model::setCost, flow = model.costFlow)
                .flowVisibility(model.isFreeFlow, renderScope)
        }
    }
    formPart("What is the day and time?") {
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
    formPart("Tell us all about the event.") {
        textEditor(
            label = "description",
            placeholder = "Event description",
            rows = 8,
            onValue = model::setDescription,
            flow = model.descriptionFlow
        )
        row(modify(JustifyContentSpaceBetween)) {
            buttonPopover("Markdown Hints", flair = "💡") {
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
}

fun RenderContext.eventImageForm(model: EventEditor) =
    imageFormSection(
        instructions = "This image will appear in the feed and at the top of the event page.",
        onValue = model::setImageUrl,
        imageFlow = model.imageUrlFlow
    )

fun RenderContext.eventLinksForm(model: EventEditor) = formCardSection("Links") {
    eventLinks(model)
}