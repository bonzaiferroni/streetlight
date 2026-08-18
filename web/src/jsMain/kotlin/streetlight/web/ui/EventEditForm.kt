package streetlight.web.ui

import kabinet.utils.toLocalDate
import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.ButtonPopover
import koala.html.bulletsOf
import koala.html.buttonPopover
import koala.html.heading3
import koala.html.heading4
import koala.html.markdown
import koala.html.textProperty
import koala.model.MutableTap
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import streetlight.model.data.EventProperty
import streetlight.web.model.EventEditor
import kotlin.time.Clock

fun ViewScope.eventEditFormBody(model: EventEditor, isAdmin: Boolean = false) = formColumn {
    if (isAdmin) eventWebsiteForm(model)
    eventDetailsForm(model)
    eventImageForm(model)
    eventLinksForm(model)
}

fun ViewScope.eventWebsiteForm(model: EventEditor) = formCard("Parse Event") {
    formRow {
        formSection("Website") {
            formText("Does this event have a web page? We can read it to find certain details.")
            textField(model.urlField, "website")
            formSubmit("🤖 read page", model::parseFromUrl, model.parseMessage)
        }
        column { }
    }
}

fun ViewScope.eventDetailsForm(model: EventEditor) = formCard("Event Details") {
    formRow {
        formSection("Title") {
            textField(model.title, "title", maxLength = 50)
                .flowValid(EventProperty.Title, model.validityCheckField, contentScope)
        }
        formSection("Cost") {
            row(modify(AlignItemsCenter, JustifyContentCenter)) {
                checkBox(model.isFree, "Free event")
                textField(model.costField, "cost", modify(Width12))
                    .flowVisibility(model.isFree.flow.map { !it }, contentScope)
                    .flowValid(EventProperty.Cost, model.validityCheckField, contentScope)
            }
        }
        formSection("Time") {
            row(modify(AlignItemsCenter, JustifyContentCenter)) {
                blockLabel("start time") {
                    timeInput(model.startTimeField)
                }.flowValid(EventProperty.StartTime, model.validityCheckField, contentScope)
                // end time is optional, not every event has a fixed end time
                blockLabel("end time") {
                    timeInput(model.endTimeField)
                }
                blockLabel("date") {
                    dateInput(model.dateField)
                }.flowValid(EventProperty.Date, model.validityCheckField, contentScope)
            }
            column(modify(Gap0, MarginTop2)) {
                textBlock("that's", modify(AlignSelfCenter, TextSmall, OpacityHigh))
                dayIndicator(model.dateField)
                textBlock("at", modify(AlignSelfCenter, TextSmall, OpacityHigh))
                timeIndicator(model.startTimeField, "some time")
            }
            // flowBlock(model.startsAt, modify(FlexColumn, AlignItemsCenter)) {
            //     val startsAt = it ?: return@flowBlock
            //     // must be boxed due to FlowContent context
            //     val dayDescription = when (startsAt < Clock.System.now()) {
            //         true -> "in the past"
            //         else -> startsAt.toRelativeDayFormat()
            //     }
            //     box {
            //         heading4("That's $dayDescription.")
            //     }
            // }
        }
        formSection("Description") {
            textEditor(
                state = model.description,
                label = "description",
                placeholder = "Event description",
                rows = 8,
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
                    flowBlock(model.description) {
                        box(modify(Padding2)) {
                            markdown(it)
                        }
                    }
                }
            }
        }
    }
}

fun ViewScope.eventImageForm(model: EventEditor) =
    imageFormSection(
        instructions = "This image will appear in the feed and at the top of the event page.",
        imageEditor = model.imageEditor
    )

fun ViewScope.eventLinksForm(model: EventEditor) = formCardSection("Links") {
    eventLinks(model)
}

fun ViewScope.dayIndicator(field: MutableTap<LocalDate?>) {
    fun changeDate(delta: Int) {
        val date = field.now ?: Clock.System.now().toLocalDate()
        field.set(date.plus(delta, DateTimeUnit.DAY))
    }

    row(modify(AlignItemsCenter, JustifyContentCenter)) {
        button(SvgFile.ArrowLeft, { changeDate(-1) }, modify(Height4, OpacityHigh))
        flowBlock(field, modify(Magic, Blur, Width24)) { date ->
            column(modify(Gap0, AlignItemsCenter, JustifyContentCenter, Height100P)) {
                when (date) {
                    null -> heading4("Someday", modify(OpacityHalf))
                    else -> {
                        heading4(date.toRelativeDayFormat())
                    }
                }
            }
        }
        button(SvgFile.ArrowRight, { changeDate(1) }, modify(Height4, OpacityHigh))
    }
}

fun ViewScope.timeIndicator(field: MutableTap<LocalTime?>, defaultLabel: String) {
    fun changeTime(delta: Int) {
        val time = field.now ?: LocalTime(12, 0)
        val seconds = (time.toSecondOfDay() + delta * 60).mod(86400)
        field.set(LocalTime.fromSecondOfDay(seconds))
    }

    row(modify(AlignItemsCenter, JustifyContentCenter)) {
        button(SvgFile.ArrowLeft, { changeTime(-30) }, modify(Height4, OpacityHigh))
        flowBlock(field, modify(Magic, Blur, Width24)) { time ->
            column(modify(Gap0, AlignItemsCenter, JustifyContentCenter, Height100P)) {
                when (time) {
                    null -> heading4(defaultLabel, modify(OpacityHalf))
                    else -> heading4(time.toTimeFormat())
                }
            }
        }
        button(SvgFile.ArrowRight, { changeTime(30) }, modify(Height4, OpacityHigh))
    }
}