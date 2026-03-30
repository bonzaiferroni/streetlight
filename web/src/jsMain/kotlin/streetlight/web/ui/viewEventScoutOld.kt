package streetlight.web.ui

import kampfire.utils.takeEllipsis
import koala.css.*
import koala.dom.*
import koala.html.textProperty
import koala.model.mapDistinct
import koala.utils.prettyPrint
import streetlight.model.data.EventEdit
import streetlight.model.data.Location
import streetlight.model.data.MultiEventParseResponse
import streetlight.web.EditEventCallbackRoute
import streetlight.web.OldEventScoutRoute
import streetlight.web.model.Streetlight
import streetlight.web.model.EventScoutOld

fun RenderContext.viewEventScoutOld(app: Streetlight, route: OldEventScoutRoute) {
    val model = EventScoutOld(renderScope, route, app)
    val panelFlow = model.stateFlow.mapDistinct { it.location }

    flowBlock(panelFlow, defaultMagic) { location ->
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

fun ViewContext<EventScoutOld>.reviewPanel(parse: MultiEventParseResponse, location: Location) {
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

fun ViewContext<EventScoutOld>.locationPanel(location: Location) {
    val parseFlow = model.stateFlow.mapDistinct { it.parse }
    val fileFlow = model.stateFlow.mapDistinct { it.htmlUrl }

    column {
        headerOf(location)
        flowBlock(parseFlow, defaultMagic) { parse ->
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

fun RenderContext.viewEventScoutRoute(app: Streetlight) {
    routeBlock<OldEventScoutRoute>(app.portal) { route ->
        console.log(prettyPrint(route))
        viewEventScoutOld(app, route)
    }
}

private fun ViewContext<EventScoutOld>.viewEventParse(
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
                        textBlock(eventName ?: "[No name]")
                        // time/date
                        row {
                            textBlock(date.toString())
                            event.startsAt?.let { time ->
                                textBlock(time.toString())
                            }
                        }
                        // description
                        event.description?.let {
                            textProperty("description", it.takeEllipsis(200))
                        }
                        // ageMin
                        event.ageMin?.let {
                            textProperty("ages", "$it+")
                        }
                        event.cost?.let {
                            textProperty("cost", usdValue(it))
                        }
                        // contact
                        event.contact?.let {
                            textProperty("contact", it)
                        }
                        // url
                        event.link?.takeIf { it.startsWith("http") }?.let {
                            textProperty("url", it)
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