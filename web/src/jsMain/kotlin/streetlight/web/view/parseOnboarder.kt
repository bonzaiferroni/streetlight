package streetlight.web.view

import kampfire.model.MutableTap
import kampfire.model.storeOf
import kampfire.model.mutableTapOf
import kampfire.model.toUrl
import koala.dom.ViewScope
import koala.dom.component.*
import koala.dom.markdown.markdown
import koala.markdown.lazyMarkdown
import koala.model.MessageStore
import koala.modifier.*
import streetlight.model.data.GatherEventStatus
import streetlight.model.data.LinkAccess
import streetlight.model.data.LinkContent
import streetlight.model.data.ParseConfig
import streetlight.model.data.ParseMode
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.formCard
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.layout.BodyStyle
import streetlight.web.layout.cardOf
import streetlight.web.model.LocationOnboarder
import streetlight.web.model.ParseModeFocus

/**
 * The section of how the crawler reads the events of the [focus] location: the host's permission, and what it has
 * gathered.
 */
context(model: LocationOnboarder)
fun ViewScope.parseOnboarder(focus: ParseModeFocus) {
    val configState = storeOf(
        ParseConfig(
            locationId = focus.content.location.locationId,
            eventsUrl = focus.content.location.eventsUrl,
            parseMode = focus.content.config.parseMode ?: ParseMode.Partial,
        )
    )
    val messages = MessageStore()

    column(BodyStyle.MainColumn) {
        formCard {
            formRow {
                parsePermissionSection(configState)
                formSection("How it works") {
                    markdown(parseOperationInfo)
                }
            }
        }
        formSubmit("Next", { model.submitParseConfig(configState.now, messages) }, messages, Primary)
    }
}

/** The events page of the location, and how much of it the crawler may read. */
private fun ViewScope.parsePermissionSection(configState: MutableTap<ParseConfig>) = formSection("Finding your events") {
    val urlState = configState.mutableTapOf({ it.eventsUrl?.value ?: "" }) { copy(eventsUrl = it.takeIf { url -> url.isNotBlank() }?.toUrl()) }
    val modeState = configState.mutableTapOf({ it.parseMode }) { copy(parseMode = it) }

    markdown(parseIntro)
    textField(urlState, "events page")
    switchMenu(modeState)
    markdown(parseModeInfo, MarginTop(1))
}

// td: move to dashboard
/** The section of what the crawler has gathered from the events page so far. */
private fun ViewScope.gatherStatusSection(status: GatherEventStatus) = actionSection("Gathered Events") {
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

private val GatherEventStatus.Blocked.reason get() = when {
    access == LinkAccess.RobotsBlock -> "Your site's robots.txt asks crawlers not to read your events page."
    access == LinkAccess.Refused -> "Your site refused our crawler's request for your events page."
    content == LinkContent.Unread -> "Your events page needs scripting to show its events, which our crawler can't run yet."
    content == LinkContent.OffSchema || content == LinkContent.OffScope -> "Your events page doesn't look like a list of events."
    else -> "Our crawler read your events page, but couldn't gather events from it."
}

//language="MD"
val parseIntro by lazyMarkdown("""
You can share the **web address** of your calendar or events page. 
Streetlight users can click the link to view the events on your page. 

We can also read the page periodically and add the events to Streetlight. This makes them visible on our map.
""")

//language="MD"
val parseModeInfo by lazyMarkdown("""
You can choose how much information we automatically gather.

| | |
|:---:|---|
| **None** | Your events page is not read. |
| **Partial** | Your events are gathered with a short description that links back to your page. |
| **Full** | Your events are gathered with all description content and a link to your page. |
""")

//language="MD"
val parseOperationInfo by lazyMarkdown("""
We send a crawler to read your content once a day. It identifies itself as `Streetlight/1.0` and follows any directives in **robots.txt**. It typically makes one request per page. We look for event information declared within json or html. 

We gather the following information declared as meta information or within the body:

* Title
* Date / Start time / End time
* Description
* Event Image
* Cost
* Contact
* Tickets link
* Event page link

""")