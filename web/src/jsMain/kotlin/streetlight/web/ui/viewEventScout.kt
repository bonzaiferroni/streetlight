package streetlight.web.ui

import kampfire.utils.takeEllipsis
import koala.css.*
import koala.dom.*
import koala.html.propertyValue
import koala.model.mapDistinct
import koala.utils.prettyPrint
import streetlight.model.data.EventEdit
import streetlight.model.data.Location
import streetlight.model.data.MultiEventParseResponse
import streetlight.web.EditEventCallbackRoute
import streetlight.web.EventScoutRoute
import streetlight.web.model.AppContext
import streetlight.web.model.EventScout

fun RenderContext.viewEventScout(app: AppContext, route: EventScoutRoute) {
    val model = EventScout(renderScope, route, app)
    val panelFlow = model.stateFlow.mapDistinct { it.location }

    flowBlock(panelFlow, defaultMagic, magic = true) { location ->
        viewOf(model) {
            if (location != null) {
                locationPanel(location)
            } else {
                card {
                    column {
                        row {
                            messageBox(model.messageFlow, modify(Flex1))
                            button("📃 Use form")
                        }
                        row {
                            textField("link", modify(Flex1), model::setLink, model.stateFlow.mapDistinct { it.link })
                            button("🤖 read link", onClick = model::readLink)
                        }
                    }
                }
            }
        }
    }
}

fun ViewContext<EventScout>.reviewPanel(parse: MultiEventParseResponse, location: Location) {
    val events = parse.events?.takeIf { it.isNotEmpty() }

    card {
        row {
            textBlock("If the information looks correct, you can post these events to ${location.name}.", modify(Flex1))
            button("post all", onClick = model::postAll)
        }

        events?.forEachIndexed { index, event ->
            viewEventParse(event, index)
        }
    }
}

fun ViewContext<EventScout>.locationPanel(location: Location) {
    val parseFlow = model.stateFlow.mapDistinct { it.parse }
    val fileFlow = model.stateFlow.mapDistinct { it.htmlUrl }

    column {
        headerOf(location)
        flowBlock(parseFlow, defaultMagic, magic = true) { parse ->
            viewOf(model) {
                if (parse != null) {
                    reviewPanel(parse, location)
                } else {
                    card {
                        row {
                            messageBox(model.messageFlow, modify(Flex1))
                            button("start over", onClick = model::startOver)
                            button("read calendar", onClick = model::readCalendar)
                            button("read html", onClick = model::readHtml)
                        }
                        // file choice
                        fileDrop(fileFlow, model::setHtmlUrl)
                    }
                }
            }
        }
    }
}

fun RenderContext.viewEventScoutRoute(app: AppContext) {
    routeBlock<EventScoutRoute>(app.portal) { route ->
        console.log(prettyPrint(route))
        viewEventScout(app, route)
    }
}

private fun ViewContext<EventScout>.viewEventParse(
    event: EventEdit,
    index: Int,
) {
    val eventName = event.title
    val date = event.date
    val portal = model.app.portal
    val statusFlow = model.stateFlow.mapDistinct { it.getStatus(index) }

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
                            event.startsAt?.let { time ->
                                textBlock(time.toString())
                            }
                        }
                        // description
                        event.description?.let {
                            propertyValue("description", it.takeEllipsis(200))
                        }
                        // ageMin
                        event.ageMin?.let {
                            propertyValue("ages", "$it+")
                        }
                        event.cost?.let {
                            propertyValue("cost", usdValue(it))
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
        val route = EditEventCallbackRoute(event) { event ->
            if (event != null) {
                model.setCompleted(index)
            }
            itemElement.scrollWhenPresent()
        }
        portal.go(route)
    }
}

fun usdValue(amount: Float): String =
    (kotlin.math.round(amount * 100) / 100.0)
        .toString()
        .let { value ->
            val dot = value.indexOf('.')
            when {
                dot == -1 -> "$value.00"
                value.length - dot == 2 -> "${value}0"
                else -> value
            }
        }