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
import kotlinx.coroutines.flow.map
import streetlight.model.data.EventProperty
import streetlight.web.model.EventEditor
import kotlin.time.Clock

fun ScopedDOM.eventEditFormBody(model: EventEditor) = formBody {
    eventWebsiteForm(model)
    eventDetailsForm(model)
    eventImageForm(model)
    eventLinksForm(model)
}

fun ScopedDOM.eventWebsiteForm(model: EventEditor) = formCardSection("Web page") {
    formPart(
        instructions = "Does this event have a web page? We can read it to find certain details.",
        bullets = listOf("Some websites cannot be read automatically, but you can fill in the details yourself.")
    ) {
        textField("website", modify(), model::setUrl, model.urlFlow)
        row(modify(JustifyContentEnd)) {
            messageBox(model.urlMessage, modify(Magic))
            button("🤖 read page", onClick = model::readUrl)
        }
    }
}

fun ScopedDOM.eventDetailsForm(model: EventEditor) = formCardSection("Event Details") {
    formPart("What is the name of the event?") {
        formTextField("title", model::setTitle, model.titleFlow, maxLength = 50)
            .flowValid(EventProperty.Title, model.validityFlow, renderScope)
    }
    formPart("How much does it cost?") {
        row(modify(AlignItemsCenter)) {
            checkBox("Free event", model::setFree, model.isFreeFlow)
            textField("cost", modify(Width12), onValue = model::setCost, flow = model.costFlow)
                .flowVisibility(model.isFreeFlow.map { !it }, renderScope)
                .flowValid(EventProperty.Cost, model.validityFlow, renderScope)
        }
    }
    formPart("What is the day and time?") {
        row(modify(AlignItemsCenter, JustifyContentCenter)) {
            blockLabel("start time") {
                timeInput(model.startTimeFlow, model::setStartTime)
            }.flowValid(EventProperty.StartTime, model.validityFlow, renderScope)
            // end time is optional, not every event has a fixed end time
            blockLabel("end time") {
                timeInput(model.endTimeFlow, model::setEndTime)
            }
            blockLabel("date") {
                dateInput(model.dateFlow, model::setDate)
            }.flowValid(EventProperty.Date, model.validityFlow, renderScope)
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

fun ScopedDOM.eventImageForm(model: EventEditor) =
    imageFormSection(
        instructions = "This image will appear in the feed and at the top of the event page.",
        onValue = model::setImageUrl,
        imageFlow = model.imageUrlFlow
    )

fun ScopedDOM.eventLinksForm(model: EventEditor) = formCardSection("Links") {
    eventLinks(model)
}