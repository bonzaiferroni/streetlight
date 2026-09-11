package streetlight.web.layouts

import kabinet.utils.toMetricString
import kampfire.api.Username
import koala.SvgFile
import koala.css.MinWidth32
import kotlinx.html.FlowContent
import streetlight.model.data.Event
import streetlight.model.data.EventEdit
import streetlight.model.data.EventLocation
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationEdit
import streetlight.model.data.Post
import streetlight.web.ui.postMenu
import streetlight.web.ui.starToggle


fun cellContentOf(location: Location, post: Post? = null): FlowContent.() -> Unit = {
    // starCell(location.username)
    val mapType = location.mapType ?: "Location"
    cell(SvgFile.MapPin, mapType)
    location.city?.let {
        cell(SvgFile.City, it)
    }
    buttonsCell {
        starToggle(location)
        moreButton()
        post?.let {
            postMenu(post.postId, post.username)
        }
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
    event.cost?.let {
        costCell(it, event.website)
    }
    // exampleLightCell()
}

fun cellContentOf(event: EventLocation, showMore: Boolean, post: Post? = null): FlowContent.() -> Unit = {
    event.startsAt?.let { startsAt ->
        dateCell(startsAt)
        startsAtCell(startsAt)
    }

    event.cost?.let {
        costCell(it, event.url)
    }

//    event.city?.let {
//        cell(SvgFile.City, it)
//    }
    event.locationName?.let {
        cell(SvgFile.MapPin, it)
    }
    buttonsCell(MinWidth32) {
        starToggle(event)
        if (showMore) {
            moreButton()
        }
        post?.let {
            postMenu(post.postId, post.username)
        }
    }
}

fun cellContentOf(event: Event): FlowContent.() -> Unit = {
    event.startsAt?.let { startsAt ->
        dateCell(startsAt)
        startsAtCell(startsAt)
    }
    event.cost?.let {
        costCell(it, event.website)
    }

    buttonsCell {
        starToggle(event)
        moreButton()
    }
}

fun cellContentOf(galaxy: Galaxy): FlowContent.() -> Unit = {
    cell(SvgFile.Calendar, galaxy.eventCount.toMetricString())
    buttonsCell {
        starToggle(galaxy)
    }
}