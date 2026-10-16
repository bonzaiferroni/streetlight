package streetlight.web.view

import kabinet.utils.toLocalDate
import kabinet.utils.toFutureFormat
import kabinet.utils.toTimeFormat
import koala.LottieFile
import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import koala.component.bulletsOf
import koala.component.heading3
import koala.component.heading5
import koala.component.textProperty
import kampfire.model.MutableTap
import koala.component.buttonPopover
import kotlinx.coroutines.flow.map
import kotlinx.css.pct
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import streetlight.model.data.EventProperty
import streetlight.web.model.EventEditor
import kotlin.time.Clock
import koala.dom.component.*
import koala.dom.layout.*
import koala.dom.markdown.*
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formFiller
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit

context(model: EventEditor)
fun ViewScope.eventEditFormBody(isAdmin: Boolean = false) = formColumn {
    if (isAdmin) eventWebsiteForm()
    eventDetailsForm()
    eventImageForm()
    eventLinksForm()
}

context(model: EventEditor)
fun ViewScope.eventWebsiteForm() = formCard("Parse Event") {
    formRow {
        formSection("Website") {
            centeredText("Does this event have a web page? We can read it to find certain details.")
            textField(model.urlState, "website")
            formSubmit("🤖 read page", model::parseFromUrl, model.parseMessage)
        }
        formFiller(LottieFile.Ghost)
    }
}

context(model: EventEditor)
fun ViewScope.eventDetailsForm() = formCard("Event Details") {
    formRow {
        formSection("Title") {
            textField(model.titleState, "title", maxLength = 50)
                .flowValid(EventProperty.Title, model.validityState, contentScope)
        }
        formSection("Cost") {
            row(modify(AlignItemsCenter, JustifyContentCenter)) {
                checkBox(model.isFreeState, "Free event")
                textField(model.costState, "cost", Width(12))
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
            column(modify(Gap0, MarginTop(2))) {
                textBlock("that's", modify(AlignSelfCenter, TextSmall, OpacityHigh))
                dayIndicator(model.dateState)
                textBlock("at", modify(AlignSelfCenter, TextSmall, OpacityHigh))
                timeIndicator(model.startTimeState, "some time")
            }
        }
    }
    formSection("Description") {
        markdownEditor(
            state = model.descriptionState,
            label = "description",
            mod = MinHeight(48)
        )
        row(JustifyContentSpaceBetween) {
            buttonPopover("Markdown Hints", flair = SvgFile.Bulb) {
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
            buttonDialog("Preview", emoji = "👀") {
                println("ey")
                markdown(model.descriptionState.now)
            }
        }
    }
}

context(model: EventEditor)
fun ViewScope.eventImageForm() = context(model.imageEditor) {
    imageFormSection("This image will appear in the feed and at the top of the event page.")
}

/** A date with buttons that step it by a day. */
fun ViewScope.dayIndicator(field: MutableTap<LocalDate?>) {
    fun changeDate(delta: Int) {
        val date = field.now ?: Clock.System.now().toLocalDate()
        field.set(date.plus(delta, DateTimeUnit.DAY))
    }

    row(modify(AlignItemsCenter, JustifyContentCenter)) {
        button(SvgFile.ArrowLeft.large, { changeDate(-1) }, modify(Height(4), OpacityHigh))
        flowBlock(field, modify(Magic, Blur, Width(24))) { date ->
            column(modify(Gap0, AlignItemsCenter, JustifyContentCenter, Height(100.pct))) {
                when (date) {
                    null -> heading5("someday", OpacityHalf)
                    else -> {
                        heading5(date.toFutureFormat())
                    }
                }
            }
        }
        button(SvgFile.ArrowRight.large, { changeDate(1) }, modify(Height(4), OpacityHigh))
    }
}

/** A time with buttons that step it, showing [defaultLabel] when unset. */
fun ViewScope.timeIndicator(field: MutableTap<LocalTime?>, defaultLabel: String) {
    fun changeTime(delta: Int) {
        val time = field.now ?: LocalTime(12, 0)
        val seconds = (time.toSecondOfDay() + delta * 60).mod(86400)
        field.set(LocalTime.fromSecondOfDay(seconds))
    }

    row(modify(AlignItemsCenter, JustifyContentCenter)) {
        button(SvgFile.ArrowLeft.large, { changeTime(-30) }, modify(Height(4), OpacityHigh))
        flowBlock(field, modify(Magic, Blur, Width(24))) { time ->
            column(modify(Gap0, AlignItemsCenter, JustifyContentCenter, Height(100.pct))) {
                when (time) {
                    null -> heading5(defaultLabel, OpacityHalf)
                    else -> heading5(time.toTimeFormat())
                }
            }
        }
        button(SvgFile.ArrowRight.large, { changeTime(30) }, modify(Height(4), OpacityHigh))
    }
}