package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.applyBlockLabel
import koala.html.blockLabel
import koala.html.image
import koala.html.paragraph
import kotlinx.html.dialog

fun RenderContext.viewEventCreator(app: AppContext) {
    val eventCreator = app.home.eventCreator

    column {
        card(modify(Width100, AlignItemsStretch)) {
            setImage("feature image", modify(Height4))
            column(modify(QueryRow, AlignItemsStretch)) {
                setImage("icon", modify(Square, Center))
                textField(
                    label = "title",
                    onChangeValue = eventCreator::setEventTitle,
                    modifiers = modify(Flex1),
                    textModifiers = modify(Heading2),
                    placeholder = "Event Title"
                )
                row {
                    paragraph("Event category:", modify(MarginLeft1, Dim))
                    dropMenu(eventCreator::setEventType, provideLabel = { it.label })
                }
            }
            row {
                paragraph("Tags:", modify(MarginLeft1, Dim))
                textField()
            }
            dialogBox("yer dialog") {
                paragraph("ey")
            }
            column(modify(QueryRow, AlignItemsStretch)) {
                viewGeoMap(app.home.geoMap, modify(Flex1, Square))
                column(modify(Flex2, AlignItemsStretch)) {
                    textField(
                        label = "location name",
                        onChangeValue = eventCreator::setLocationName,
                        binding = eventCreator.stateFlow.mapDistinct { it.locationName })
                    button("query") {
                        eventCreator.queryLocation()
                    }
                }
            }
            textField(5, "description", placeholder = "Event description")
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