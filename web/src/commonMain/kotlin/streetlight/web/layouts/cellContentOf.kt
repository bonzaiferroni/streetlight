package streetlight.web.layouts

import kabinet.utils.toMetricString
import kampfire.api.Username
import kampfire.model.toUrl
import koala.SvgFile
import koala.css.MinWidth32
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPost
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.web.ui.postMenu
import streetlight.web.ui.starLightCell


fun cellContentOf(location: Location): FlowContent.() -> Unit = {
    // starCell(location.username)
    val mapType = location.mapType ?: "Location"
    cell(SvgFile.MapPinOutline, mapType)
    location.city?.let {
        cell(SvgFile.City, it)
    }
    buttonsCell {
        starLightCell(location)
        moreButton()
    }
}

fun cellContentOf(username: Username?, edit: LocationEdit): FlowContent.() -> Unit = {
    starCell(username)
    // exampleLightCell()
}

fun cellContentOf(event: EventEdit): FlowContent.() -> Unit = {
    when (val startsAt = event.startsAt) {
        null -> exampleStartsAtCell()
        else -> startsAtCell(startsAt)
    }
    costCell(event.cost, event.website)
    // exampleLightCell()
}

fun cellContentOf(event: EventLocation, showMore: Boolean, post: GalaxyPost? = null): FlowContent.() -> Unit = {
    dateCell(event.startsAt)
    startsAtCell(event.startsAt)
    costCell(event.cost, event.url)
//    event.city?.let {
//        cell(SvgFile.City, it)
//    }
    event.locationName?.let {
        cell(SvgFile.MapPinOutline, it)
    }
    buttonsCell(MinWidth32) {
        post?.let {
            postLight(post)
        }
        starLightCell(event)
        if (showMore) {
            moreButton()
        }
        post?.let {
            postMenu(post.base.postId, post.base.username)
        }
    }
}

fun cellContentOf(event: Event): FlowContent.() -> Unit = {
    dateCell(event.startsAt)
    startsAtCell(event.startsAt)
    costCell(event.cost, event.website)
    buttonsCell {
        starLightCell(event)
        moreButton()
    }
}

fun cellContentOf(galaxy: Galaxy): FlowContent.() -> Unit = {
    cell(SvgFile.Calendar, galaxy.eventCount.toMetricString())
    buttonsCell {
        starLightCell(galaxy)
    }
}