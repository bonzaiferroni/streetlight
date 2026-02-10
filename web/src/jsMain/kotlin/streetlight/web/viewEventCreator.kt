package streetlight.web

import koala.css.*
import koala.dom.*
import koala.html.paragraph
import streetlight.model.Api

fun RenderContext.viewEventCreator(app: AppContext) {
    val eventCreator = app.home.eventCreator

    column {
        card(modify(Width100, AlignItemsStretch)) {
            blockLabel("feature image") {
                imageChoice(modify(Height4), onUpload = {
                    app.client.event.uploadFeatureImage(it)
                })
            }
            column(modify(QueryRow, AlignItemsStretch)) {
                row(modify(Flex1)) {
//                    blockLabel("icon", modify(Center, Square, Height100)) {
//                        imageChoice(Api.Events.Upload.path, modify(Size100))
//                    }
                    textField(
                        label = "title",
                        onChangeValue = eventCreator::setEventTitle,
                        modifiers = modify(Flex1),
                        textModifiers = modify(Heading2),
                        placeholder = "Event Title"
                    )
                }
                row {
                    paragraph("Event category:", modify(MarginLeft1, Dim))
                    dropMenu(eventCreator::setEventType, provideLabel = { it.label })
                }
            }
            row {
                textField(placeholder = "Add a tag")
            }
            column(modify(QueryRow, AlignItemsStretch)) {
                viewGeoMap(app.home.geoMap, modify(Flex1, Square))
                column(modify(Flex2, AlignItemsStretch)) {
                    textField(
                        label = "location name",
                        onChangeValue = eventCreator::setLocationName,
                        binding = eventCreator.stateFlow.mapDistinct { it.locationName })
                    button("query", onClick = {
                        eventCreator.queryLocation()
                    })
                }
            }
            textField(5, "description", placeholder = "Event description")
        }
        message(eventCreator.stateFlow.mapDistinct { it.message })
        row {
            button("cancel", onClick = {
                // eventCreator.toggle()
            })
            button("create", onClick = {
                eventCreator.createEvent()
            })
        }
    }
}