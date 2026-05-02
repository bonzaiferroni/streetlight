package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import kampfire.model.medium
import koala.SvgFile
import koala.css.*
import koala.html.*
import koala.utils.jsonConfig
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import kotlinx.serialization.Serializable
import streetlight.model.data.*
import streetlight.web.shells.SectionHeadingMod

fun FlowContent.layoutPosts(posts: List<Post>) {
    section {
        filigree {
            heading2("Posts", SectionHeadingMod)
        }

        column(modify(Gap2)) {
            posts.forEach { post ->
                postRow(post)
            }
        }
    }
}

fun FlowContent.postRow(
    post: Post,
) {
    val colorScheme = post.colorScheme
    val postRoute = post.route
    val subRoute = post.subRoute
    val subtitle = post.subtitle
    val cells = post.cells

    column {
        card(modify(QueryContainer, Padding0, OverflowClip, ZenBg, MoonShadow)) {
            setStyle(Property.ColorScheme.to(colorScheme))

            column(modify(QueryContainer, ContainerLgRow, Gap0)) {

                row(modify(Flex1, Gap0, Height24)) {

                    // image
                    navigation(postRoute, modify(Height24, Aspect1)) {
                        featureImage(post.images.medium, modify(Size100P))
                    }

                    // middle column
                    column(modify(Flex1, Padding1, Height24, PositionRelative)) {
                        post.flairIcon?.let {
                            icon(it, modify(Height9, PositionAbsolute, Top0, Right0, OpacityGhost, ColorSchemeFg))
                        }

                        column(modify(Gap0)) {
                            navigation(postRoute) {
                                heading3(post.title, modify(LineHeight1, SingleLine, Bold, Shrinkable, Flex1, MarginTop1))
                            }
                            subtitle?.let {
                                navigationIfNotNull(subRoute) {
                                    textBlock(subtitle, modify(OpacityMost))
                                }
                            }
                        }
                        post.description?.let {
                            navigation(postRoute, modify(Flex1, OverflowHidden, FadeBottom)) {
                                textBlock(it, modify(SmallText))
                            }
                        }

                        // button row
                        row(modify(AlignItemsCenter)) {
                            row(modify(Flex1, OverflowXAuto)) {
                                post.links?.forEach { link ->
                                    btn(link.label, link.url, modify(Zen))
                                }
                            }
                            icon(SvgFile.MapPin, modify(Height4))
                            val anchor = PositionAnchor("menu-${post.postId}")
                            val data = PostMenuData(post.postId, post.username).encode()
                            icon(SvgFile.DotsVertical, modify(Height4)) {
                                setAnchorName(anchor)
                                setPopoverTarget(PostKey.PostMenuId)
                                onClick = KoalaFun.CallMenu.invoke(anchor, PostKey.PostMenuId, data)
                            }
                        }
                    }
                }

                // cell content
                if (cells != null) {
                    cellRow(cells, modify(ContainerLgColumn, FlexWrap))
                }
            }
        }
        row(modify(JustifyContentEnd, MarginRight2)) {
            textBlock(modifiers = modify(SmallText, LineHeight1, OpacityMost)) {
                span("— posted by ")
                span(post.username ?: "Someone", modify(Bold))
                span(" ${post.createdAt.toAgoFormat()}")
            }
        }
    }
}

object PostKey {
    val PostMenuId = Id("post-menu")
}

@Serializable
data class PostMenuData(
    val postId: PostId,
    val username: String?,
) {
    fun encode() = jsonConfig.encodeToString(this)

    companion object {
        fun decode(data: String): PostMenuData = jsonConfig.decodeFromString(data)
    }
}