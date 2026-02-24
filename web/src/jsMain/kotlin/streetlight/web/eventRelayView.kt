package streetlight.web

import kampfire.utils.takeEllipsis
import koala.css.*
import koala.dom.*
import koala.external.ScrollIntoViewOptions
import koala.html.propertyValue
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.data.EventParse
import streetlight.model.data.toEventEdit
import kotlin.time.Duration.Companion.seconds

fun RenderContext.eventRelayView(app: AppContext) {
    val model = EventRelay(renderScope, app.client.api)
    val stateFlow = model.state.flow
    val linkFlow = stateFlow.mapDistinct { it.link }
    val stageFlow = stateFlow.mapDistinct { it.stage }
    val parseFlow = stateFlow.mapDistinct { it.parse }

    flowBlock(stageFlow) { stage ->
        if (stage == 0) {
            column {
                row {
                    textBlock("Share information about upcoming events. " +
                            "You may provide a link, image, or text and we'll do our best to understand it. " +
                            "You'll have a chance to review the information before it is posted."
                    )
                    button("📃 Use form")
                }
                row {
                    textField("link", modify(Flex1), model::setLink, linkFlow)
                    button("🤖 read link", onClick = model::readLink)
                }
            }
        } else if (stage == 1) {
            column {
                card {
                    row {
                        messageBox(model.message.flow, modify(Flex1))
                        button("start over", onClick = model::startOver)
                    }
                }

                flowBlock(parseFlow) { parse ->
                    eventRelayParse(app, model, parse)
                }
            }
        }
    }
}

fun RenderContext.eventRelayParse(app: AppContext, model: EventRelay, parse: EventParse?) {
    val events = parse?.events ?: return
    column {
        events.forEachIndexed { index, event ->
            val eventName = event.name ?: return@forEachIndexed
            val date = event.date ?: return@forEachIndexed
            val isCompleted = model.state.flow.mapDistinct { it.completed.contains(index) }

            val itemElement = card {
                flowBlock(isCompleted) { isCompleted ->
                    if (isCompleted) {
                        textBlock("Posted: $eventName ✅")
                    } else {
                        row {
                            event.imageUrl?.takeIf { it.startsWith("http") }?.let {
                                image(it, modify(Width16))
                            }
                            column(modify(Flex1)) {
                                // name
                                textBlock(eventName)
                                // time/date
                                row {
                                    textBlock(date.toString())
                                    event.time?.let { time ->
                                        textBlock(time.toString())
                                    }
                                }
                                // location
                                event.location?.let {
                                    propertyValue("location", it)
                                }
                                // address
                                event.address?.let {
                                    propertyValue("address", it)
                                }
                                // description
                                event.description?.let {
                                    propertyValue("description", it.takeEllipsis(200))
                                }
                                // ageMin
                                event.ageMin?.let {
                                    propertyValue("ages", "$it+")
                                }
                                // contact
                                event.contact?.let {
                                    propertyValue("contact", it)
                                }
                                // url
                                event.url?.takeIf { it.startsWith("http") }?.let {
                                    propertyValue("url", it)
                                }
                            }
                        }
                    }
                }
            }

            itemElement.onClick {
                val event = event.toEventEdit(null, null) ?: return@onClick
                val route = EditEventCallbackRoute(event) { event ->
                    if (event != null) {
                        model.setCompleted(index)
                    }
                    itemElement.scrollWhenPresent()
                }
                app.portal.go(route)
            }
        }
    }
}