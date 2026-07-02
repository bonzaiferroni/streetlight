package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import kampfire.api.Slug
import kampfire.api.Username
import koala.css.AlignItemsCenter
import koala.css.Bold
import koala.css.MarginTopTiny
import koala.css.SmallText
import koala.css.modify
import koala.html.navigation
import koala.html.row
import koala.html.span
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.Post
import streetlight.web.GalaxyRoute
import streetlight.web.StarRoute
import kotlin.time.Clock
import kotlin.time.Instant

fun FlowContent.postInfo(
    post: Post,
    isGalaxyContext: Boolean,
) {
    row(modify(AlignItemsCenter, MarginTopTiny)) {
        textBlock(mod = modify(SmallText)) {
            if (!isGalaxyContext) {
                navigation(GalaxyRoute(post.galaxySlug)) {
                    span("${post.galaxyName} • ")
                }
            }
            +"posted by "
            when (val username = post.username) {
                null -> {
                    span("Someone ", modify(Bold))
                }
                else -> {
                    navigation(StarRoute(Slug(username.value))) {
                        span("$username ")
                    }
                }
            }
            span((Clock.System.now() - post.createdAt).toAgoFormat())
        }
    }
}