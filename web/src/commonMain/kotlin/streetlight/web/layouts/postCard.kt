package streetlight.web.layouts

import kampfire.model.Url
import kampfire.model.medium
import koala.Svg
import koala.SvgFile
import koala.css.AlignItemsCenter
import koala.css.AlignSelfEnd
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
import koala.css.JustifySelfEnd
import koala.css.KoalaFun
import koala.css.LineHeight1
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
import koala.css.Top0
import koala.css.Zen
import koala.css.ZenBg
import koala.css.modify
import koala.css.setAnchorName
import koala.css.setStyle
import koala.html.AppRoute
import koala.html.btn
import koala.html.card
import koala.html.column
import koala.html.featureImage
import koala.html.heading3
import koala.html.icon
import koala.html.navigation
import koala.html.navigationIfNotNull
import koala.html.row
import koala.html.setPopoverTarget
import koala.html.stripMarkdown
import koala.html.textBlock
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.ExtraLink
import streetlight.model.data.PostId

fun FlowContent.postCard(
    postId: PostId?,
    title: String?,
    subtitle: String?,
    postRoute: AppRoute?,
    subRoute: AppRoute?,
    imageUrl: Url?,
    description: String?,
    colorScheme: ColorScheme,
    flairIcon: FlairIcon?,
    links: List<ExtraLink>?,
    cells: List<(FlowContent.() -> Unit)?>?,
) {
    card(modify(QueryContainer, Padding0, OverflowClip, ZenBg, MoonShadow)) {
        setStyle(Property.ColorScheme.to(colorScheme.cssValue))

        column(modify(QueryContainer, ContainerLgRow, Gap0)) {

            row(modify(Flex1, Gap0, Height24)) {

                // image
                navigationIfNotNull(postRoute, modify(Height24, Aspect1)) {
                    featureImage(imageUrl, modify(Size100P))
                }

                // middle column
                column(modify(Flex1, Padding1, Height24, PositionRelative)) {
                    flairIcon?.let {
                        icon(it.svg, modify(Height9, PositionAbsolute, Top0, Right0, OpacityGhost, ColorSchemeFg))
                    }

                    column(modify(Gap0)) {
                        navigationIfNotNull(postRoute) {
                            heading3(title, modify(LineHeight1, SingleLine, Bold, Shrinkable, Flex1, MarginTop1))
                        }
                        subtitle?.let {
                            navigationIfNotNull(subRoute) {
                                textBlock(subtitle, modify(OpacityMost))
                            }
                        }
                    }

                    navigationIfNotNull(postRoute, modify(Flex1, OverflowHidden, FadeBottom)) {
                        description?.let {
                            textBlock(it.stripMarkdown(400))
                        }
                    }

                    // button row
                    row(modify(AlignItemsCenter)) {
                        row(modify(Flex1, OverflowXAuto)) {
                            links?.forEach { link ->
                                btn(link.label, link.url, modify(Zen))
                            }
                        }
                        icon(SvgFile.MapPin, modify(Height4))
                        postId?.let { postId ->
                            val anchor = PositionAnchor("menu-${postId}")
                            icon(SvgFile.DotsVertical, modify(Height4)) {
                                setAnchorName(anchor)
                                setPopoverTarget(PostKey.PostMenuId)
                                onClick = KoalaFun.CallMenu.invoke(anchor, PostKey.PostMenuId, postId)
                            }
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
}