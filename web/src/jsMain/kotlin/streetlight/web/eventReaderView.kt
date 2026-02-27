package streetlight.web

import kampfire.utils.takeEllipsis
import koala.css.*
import koala.dom.*
import koala.html.propertyValue
import koala.model.mapDistinct
import streetlight.model.data.EventParse
import streetlight.model.data.toEventEdit

fun RenderContext.eventReaderView(app: AppContext, route: ReadEventRoute) {
    val model = EventReader(renderScope, app.client.api, route)
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
                    eventReaderResult(app, model, parse)
                }
            }
        }
    }
}

fun RenderContext.eventReaderView(app: AppContext) {
    routeBlock<ReadEventRoute>(app.portal) { route ->
        eventReaderView(app, route)
    }
}

fun RenderContext.eventReaderResult(app: AppContext, model: EventReader, parse: EventParse?) {
    val events = parse?.events ?: return

    column {
        flowBlock(model.location.flow) { location ->
            card {
                if (location != null) {
                    row {
                        textBlock("If the information looks correct, you can post these events to ${location.name}.", modify(Flex1))
                        button("post all", onClick = model::postAll)
                    }
                }
            }
        }

        events.forEachIndexed { index, event ->
            val eventName = event.name ?: return@forEachIndexed
            val date = event.date ?: return@forEachIndexed
            val statusFlow = model.state.flow.mapDistinct { it.getStatus(index) }

            val itemElement = card {
                flowBlock(statusFlow) { status ->
                    if (status == 200) {
                        textBlock("Posted: $eventName ✅")
                    } else if (status == 409) {
                        textBlock("Already posted: $eventName 👍")
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
                val event = event.toEventEdit(null, null, model.location.now?.locationId) ?: return@onClick
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