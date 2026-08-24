package streetlight.web.ui

import kabinet.utils.toLocalDate
import kabinet.utils.toRelativeDayFormat
import kabinet.utils.toTimeFormat
import koala.LottieFile
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.ButtonPopover
import koala.html.bulletsOf
import koala.html.buttonPopover
import koala.html.heading3
import koala.html.heading5
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
            textField(model.urlState, "website")
            formSubmit("🤖 read page", model::parseFromUrl, model.parseMessage)
        }
        formFiller(LottieFile.Ghost)
    }
}

fun ViewScope.eventDetailsForm(model: EventEditor) = formCard("Event Details") {
    formRow {
        formSection("Title") {
            textField(model.titleState, "title", maxLength = 50)
                .flowValid(EventProperty.Title, model.validityState, contentScope)
        }
        formSection("Cost") {
            row(modify(AlignItemsCenter, JustifyContentCenter)) {
                checkBox(model.isFreeState, "Free event")
                textField(model.costState, "cost", modify(Width12))
                    .flowVisibility(model.isFreeState.flow.map { !it }, contentScope)
                    .flowValid(EventProperty.Cost, model.validityState, contentScope)
            }
        }
        formSection("Time") {
            row(modify(AlignItemsCenter, JustifyContentCenter)) {
                blockLabel("start time") {
                    timeInput(model.startTimeState)
                }.flowValid(EventProperty.StartTime, model.validityState, contentScope)
                // end time is optional, not every event has a fixed end time
                blockLabel("end time") {
                    timeInput(model.endTimeState)
                }
                blockLabel("date") {
                    dateInput(model.dateState)
                }.flowValid(EventProperty.Date, model.validityState, contentScope)
            }
            column(modify(Gap0, MarginTop2)) {
                textBlock("that's", modify(AlignSelfCenter, TextSmall, OpacityHigh))
                dayIndicator(model.dateState)
                textBlock("at", modify(AlignSelfCenter, TextSmall, OpacityHigh))
                timeIndicator(model.startTimeState, "some time")
            }
        }
    }
    formSection("Description") {
        styledMarkdownEditor(
            state = model.descriptionState,
            label = "description",
            mod = modify(MinHeight48)
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
                println("ey")
                markdown(model.descriptionState.now)
            }
        }
    }
}

fun ViewScope.eventImageForm(model: EventEditor) =
    imageFormSection(
        instructions = "This image will appear in the feed and at the top of the event page.",
        imageEditor = model.imageEditor
    )

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
                    null -> heading5("someday", modify(OpacityHalf))
                    else -> {
                        heading5(date.toRelativeDayFormat())
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
                    null -> heading5(defaultLabel, modify(OpacityHalf))
                    else -> heading5(time.toTimeFormat())
                }
            }
        }
        button(SvgFile.ArrowRight, { changeTime(30) }, modify(Height4, OpacityHigh))
    }
}