package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import kampfire.api.Slug
import kampfire.api.Username
import koala.css.Bold
import koala.css.SmallText
import koala.css.modify
import koala.html.navigation
import koala.html.span
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.web.GalaxyRoute
import streetlight.web.StarRoute
import kotlin.time.Clock
import kotlin.time.Instant

fun FlowContent.postInfo(
    galaxySlug: Slug?,
    galaxyName: String?,
    username: Username?,
    postedAt: Instant? = null,
) {
    textBlock(mod = modify(SmallText)) {
        galaxySlug?.let {
            navigation(GalaxyRoute(it)) {
                span("${galaxyName ?: "g/$it"} • ")
            }
        }
        +"posted by "
        when (username) {
            null -> {
                span("Someone ", modify(Bold))
            }
            else -> {
                navigation(StarRoute(Slug(username.value))) {
                    span("$username ")
                }
            }
        }
        postedAt?.let {
            span((Clock.System.now() - postedAt).toAgoFormat())
        }
    }
}