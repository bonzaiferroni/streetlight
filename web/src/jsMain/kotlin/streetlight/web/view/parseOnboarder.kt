package streetlight.web.view

import kampfire.model.MutableTap
import kampfire.model.storeOf
import kampfire.model.mutableTapOf
import kampfire.model.toUrl
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.MessageStore
import koala.modifier.*
import streetlight.model.data.GatherEventStatus
import streetlight.model.data.LinkAccess
import streetlight.model.data.LinkContent
import streetlight.model.data.ParseConfig
import streetlight.model.data.ParseMode
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.layout.BodyStyle
import streetlight.web.layout.cardOf
import streetlight.web.model.LocationOnboarder
import streetlight.web.model.ParseModeFocus

/** How the crawler reads the events of the [focus] location: the host's permission, and what it has gathered. */
context(model: LocationOnboarder)
fun ViewScope.parseOnboarder(focus: ParseModeFocus) {
    val configState = storeOf(
        ParseConfig(focus.content.location.locationId, focus.content.location.eventsUrl, focus.content.config.parseMode)
    )
    val messages = MessageStore()

    column(BodyStyle.MainColumn) {
        formCard("Events") {
            formRow {
                parsePermissionSection(configState)
                gatherStatusSection(focus.status)
            }
        }
        formSubmit("Next", { model.submitParseConfig(configState.now, messages) }, messages, Primary)
    }
}

/** The events page of the location, and how much of it the crawler may read. */
private fun ViewScope.parsePermissionSection(configState: MutableTap<ParseConfig>) = formSection("Permission") {
    val urlState = configState.mutableTapOf({ it.eventsUrl?.value ?: "" }) { copy(eventsUrl = it.takeIf { url -> url.isNotBlank() }?.toUrl()) }
    val modeState = configState.mutableTapOf({ it.parseMode }) { copy(parseMode = it) }

    centeredText("Our crawler can gather the events from your calendar or events page, so you don't have to post each one.")
    textField(urlState, "events page")
    dropMenu(modeState)
    flowBlock(modeState) { mode ->
        centeredText(mode.description, Italic)
    }
}

/** What the crawler has gathered from the events page so far. */
private fun ViewScope.gatherStatusSection(status: GatherEventStatus) = formSection("Status") {
    when (status) {
        GatherEventStatus.Url -> centeredText("Add the url of your events page so our crawler can find it.")
        GatherEventStatus.Unchecked -> centeredText("Our crawler hasn't read your events page yet. Check back soon.")
        is GatherEventStatus.Blocked -> centeredText(status.reason)
        is GatherEventStatus.Ready -> {
            centeredText("These are the latest events gathered from your page.")
            column {
                status.events.forEach { event ->
                    cardOf(event)
                }
            }
        }
    }
}

private val ParseMode.description get() = when (this) {
    ParseMode.None -> "Your events page is not read."
    ParseMode.Partial -> "Your events are gathered with a short description that links back to your page."
    ParseMode.Full -> "Your events are gathered with their whole descriptions."
}

private val GatherEventStatus.Blocked.reason get() = when {
    access == LinkAccess.RobotsBlock -> "Your site's robots.txt asks crawlers not to read your events page."
    access == LinkAccess.Refused -> "Your site refused our crawler's request for your events page."
    content == LinkContent.Unread -> "Your events page needs scripting to show its events, which our crawler can't run yet."
    content == LinkContent.OffSchema || content == LinkContent.OffScope -> "Your events page doesn't look like a list of events."
    else -> "Our crawler read your events page, but couldn't gather events from it."
}
