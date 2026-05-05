package streetlight.web.layouts

import kabinet.utils.toAgoFormat
import kampfire.model.medium
import koala.SvgFile
import koala.css.AlignItemsCenter
import koala.css.Aspect1
import koala.css.Bold
import koala.css.ColorSchemeFg
import koala.css.ContainerLgColumn
import koala.css.ContainerLgRow
import koala.css.FadeBottom
import koala.css.Flex1
import koala.css.FlexWrap
import koala.css.Gap0
import koala.css.Height24
import koala.css.Height4
import koala.css.Height9
import koala.css.JustifyContentEnd
import koala.css.KoalaFun
import koala.css.LineHeight1
import koala.css.MarginRight2
import koala.css.MarginTop1
import koala.css.MoonShadow
import koala.css.OpacityGhost
import koala.css.OpacityMost
import koala.css.OverflowClip
import koala.css.OverflowHidden
import koala.css.OverflowXAuto
import koala.css.Padding0
import koala.css.Padding1
import koala.css.PositionAbsolute
import koala.css.PositionAnchor
import koala.css.PositionRelative
import koala.css.Property
import koala.css.QueryContainer
import koala.css.Right0
import koala.css.Shrinkable
import koala.css.SingleLine
import koala.css.Size100P
import koala.css.SmallText
import koala.css.Top0
import koala.css.Zen
import koala.css.ZenBg
import koala.css.modify
import koala.css.setAnchorName
import koala.css.setStyle
import koala.html.btn
import koala.html.card
import koala.html.column
import koala.html.featureImage
import koala.html.heading3
import koala.html.icon
import koala.html.navigation
import koala.html.navigationIfNotNull
import koala.html.row
import koala.html.setAttribute
import koala.html.setPopoverTarget
import koala.html.span
import koala.html.textBlock
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.Post

fun FlowContent.layoutPost(
    post: Post,
) {
    val colorScheme = post.colorScheme
    val postRoute = post.route
    val subRoute = post.subRoute
    val subtitle = post.subtitle
    val cells = post.cells

    column {
        setAttribute(PostKey.Attribute.to(post.postId))

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
                                textBlock(it)
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
                            icon(SvgFile.DotsVertical, modify(Height4)) {
                                setAnchorName(anchor)
                                setPopoverTarget(PostKey.PostMenuId)
                                onClick = KoalaFun.CallMenu.invoke(anchor, PostKey.PostMenuId, post.postId)
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