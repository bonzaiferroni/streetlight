package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.heading2
import koala.html.image
import kotlinx.html.js.p

fun RenderContext.viewEventCreator(app: AppContext) {
    val eventCreator = app.home.eventCreator

    column {
        card {
            row(modify(Width100, AlignItemsStretch)) {
                image(modifiers = modify(Flex1, Width100, BorderRadius1))
                textField(
                    label = "title",
                    onChangeValue = eventCreator::setEventTitle,
                    modifiers = modify(Flex2, Height100),
                    textModifiers = modify(Heading2),
                    placeholder = "Event Title"
                )
            }
            column(modify(QueryRow, Width100, FlexItemsBasis50)) {
                dropMenu(eventCreator::setEventType) { it.label }
                column(modify(ItemsWidth100)) {
                    textField(
                        label = "location name",
                        onChangeValue = eventCreator::setLocationName,
                        binding = eventCreator.stateFlow.mapDistinct { it.locationName })
                    button("query") {
                        eventCreator.queryLocation()
                    }
                }
            }
        }
        message(eventCreator.stateFlow.mapDistinct { it.message })
        row {
            button("cancel") {
                // eventCreator.toggle()
            }
            button("create") {
                eventCreator.createEvent()
            }
        }
    }
}