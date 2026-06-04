package streetlight.web.ui

import kampfire.model.medium
import kampfire.model.thumb
import koala.css.Aspect1
import koala.css.BlurBackdrop
import koala.css.Bold
import koala.css.BorderRadius1
import koala.css.BorderRadius2
import koala.css.BorderRadiusTop1
import koala.css.CardBg
import koala.css.CardGradientBg
import koala.css.Flex1
import koala.css.Gap0
import koala.css.Height100P
import koala.css.Height24
import koala.css.Height8
import koala.css.LineHeight115
import koala.css.OpacityMost
import koala.css.OverflowClip
import koala.css.OverflowYAuto
import koala.css.Padding1
import koala.css.PaddingTop1
import koala.css.PaperGradientBg
import koala.css.SingleLine
import koala.css.SmallText
import koala.css.Width100P
import koala.css.Width32
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.image
import koala.dom.markdown
import koala.dom.navigation
import koala.dom.row
import koala.dom.selectionBlock
import koala.dom.textBlock
import koala.html.featureImage
import koala.html.heading3
import streetlight.model.data.GalaxyPost
import streetlight.web.EarthRoute
import streetlight.web.model.EarthMap

//fun RenderContext.galaxyListPanel(model: EarthMap) {
//    flowBlock(model.galaxiesFlow, modify(Height100P)) { galaxies ->
//        column(modify(Padding1)) {
//            galaxies.forEach { galaxy ->
//                if (galaxy.eventCount + galaxy.locationCount == 0) return@forEach
//                navigation(EarthRoute(galaxy.slug), modify(Width100P)) {
//                    row(modify(Height8, BorderRadius2, OverflowClip, Gap0)) {
//                        image(galaxy.images.thumb, modify(Aspect1))
//                        column(modify(PaperGradientBg, Padding1, Gap0)) {
//                            heading3(galaxy.name, modify(Bold, LineHeight115, SingleLine))
//                            textBlock(buildString {
//                                if (galaxy.eventCount > 0) append("${galaxy.eventCount} events")
//                                if (galaxy.locationCount > 0) {
//                                    if (isNotEmpty()) append(" • ")
//                                    append("${galaxy.locationCount} locations")
//                                }
//                            }, modify(SmallText))
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

//fun RenderContext.postListPanel(model: EarthMap, posts: List<GalaxyPost>) {
//    row(modify(Height100P, Gap0, BorderRadiusTop1, OverflowClip)) {
//        selectionBlock(model.postsFlow, model::setPost, model.postFlow, modify(Padding1, CardBg)) { post ->
//            image(post.images.thumb, modify(BorderRadius1, Height8))
//        }
//        flowBlock(model.postFlow, modify(Flex1)) { post ->
//            when (post) {
//                null -> {
//                    column(modify(PaddingTop1, CardGradientBg, Width32)) {
//                        posts.forEach { post ->
//                            column(modify(Gap0, Height8)) {
//                                heading3(post.label, modify(Bold, LineHeight115, SingleLine))
//                                post.sublabel?.let {
//                                    textBlock(it, modify(OpacityMost))
//                                }
//                            }
//                        }
//                    }
//                }
//                else -> postPanel(post)
//            }
//        }
//    }
//}

fun RenderContext.postPanel(post: GalaxyPost) {
    column(modify(Height100P, OverflowYAuto, CardBg, BlurBackdrop)) {
        featureImage(post.images.medium, modify(Width100P, Height24))
        column(modify(Padding1)) {
            heading3(post.label)
            post.description?.let {
                markdown(it)
            }
        }
    }
}