package streetlight.web.layouts

import kampfire.api.Markdown
import koala.Image
import koala.SiteImage
import koala.css.*
import koala.html.*
import kotlinx.css.GridTemplateColumns
import kotlinx.css.fr
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import streetlight.model.data.ExtraLink

fun FlowContent.feedRow(
    heading: String,
    postRoute: AppRoute?,
    image: Image?,
    description: Markdown?,
    colorScheme: ColorScheme = ColorScheme.Primary,
    links: List<ExtraLink>?,
    cells: (FlowContent.() -> Unit)?,
    subheading: (DIV.() -> Unit)?,
) {
    val imageUrl = image?.thumb ?: SiteImage.placeholder.thumb // td: make placeholder depend on post type

    div(modify(FeedRow.Base, modify(Padding1, ZenBg))) {

        div(modify(FeedRow.Content)) {
            setStyle(Property.ColorScheme.to(colorScheme.cssValue))
            row(modify(Height10)) {
                navigationIfNotNull(postRoute, modify(Width10, OverflowClip, BorderRadius50P, BorderSolid2Px, MoonShadow)) {
                    image(imageUrl, modify(Size100P, ObjectFitCover))
                }
                column(modify(Gap0, JustifyContentCenter, AlignItemsCenter, Flex1)) {
                    navigationIfNotNull(postRoute) {
                        heading5(heading, modify(LineHeight115, Shrinkable, LineClamp2, TextOverflowEllipses))
                    }
//                    spacer(modify(ColorSchemeFg, Height2Px, InkGradientBg, MarginTopTiny, ParticleRay)) {
//                        setRandomSeed()
//                    }
                    subheading?.invoke(this)
                }
            }
            // spacer(modify(Height2Px, InkGradientBg, MarginTop2Px))

            cells?.let {
                cellBlock(modify(FeedRow.Cells, BorderRadius2, OverflowClip, Outline), cells)
            }
        }

        grid(GridTemplateColumns("1fr min-content"), mod = modify(FeedRow.ExpandedContent, Padding2, Gap2)) {
            description?.let {
                markdown(it, modify(Flex1), limit = 1000)
            }
            links?.let { links ->
                row(modify(FlexWrap, AlignItemsStart, AlignContentStart)) {
                    links.forEach { link ->
                        btn(link.label, link.url, modify(Zen))
                    }
                }
            }
        }
    }
}

object FeedRow {
    val Base = Class("feed-row")
    val Content = Base.withBemElement("content")
    val ExpandedContent = Base.withBemElement("expanded-content")
    val Cells = Base.withBemElement("cells")

    val ToggleExpand = Base.withBemModifier("expand-row")
}

//language="CSS"
val FeedProtoCss get() = with(FeedRow) { """
    
$Base {
    display: grid;
    gap: 0;
    grid-template-rows: auto 1fr;
    
    &:not($ToggleExpand) {
        $ExpandedContent {
            display: none;
        }
    }
    
    &$Transitioning {
        transition: grid-template-rows var(--magic-interval) var(--magic-easing);
    }
}

$Content {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    align-items: center;
    gap: var(--unit-spacing);
    align-self: start;
}

$ExpandedContent {
    overflow: hidden;
}

"""}