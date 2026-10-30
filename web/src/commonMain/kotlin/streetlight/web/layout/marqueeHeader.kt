package streetlight.web.layout

import kampfire.api.Markdown
import koala.Image
import koala.modifier.*
import koala.component.box
import koala.component.configureImage
import koala.component.column
import koala.component.filigree
import koala.component.heading1
import koala.component.metaImage
import koala.component.textBlock
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.img
import koala.model.ExtraLink
import streetlight.model.data.EntityType
import streetlight.web.model.cssValue
import streetlight.model.data.EntityDetail

/**
 * The header of a page with its title centered above, as a sign over a window: the image in a frame lit by the
 * page's glow, then the cells, buttons, description, and links.
 *
 * [descriptor] is the small line under the title, such as "a galaxy".
 */
fun FlowContent.marqueeHeader(
    title: String,
    descriptor: String,
    image: Image?,
    subtitle: String? = null,
    colorScheme: EntityType? = null,
    description: Markdown? = null,
    cells: List<EntityDetail>? = null,
    buttons: List<EntityButton>? = null,
    links: List<ExtraLink>? = null,
    mod: Modifier? = null,
    block: DIV.() -> Unit = { },
) {
    column(modify(MarqueeHeader.Base, Gap(2), mod)) {
        setStyle(Css.ColorScheme.of(colorScheme.cssValue))
        block()

        column(modify(MarqueeHeader.Title, AlignItemsCenter, Gap(1), MarginTop(4))) {
            heading1(title, FocusTarget)
            filigree(AlignSelfStretch) {
                textBlock(descriptor, modify(Italic, OpacityHalf))
            }
            subtitle?.let {
                textBlock(subtitle, OpacityHigh)
            }
        }

        image?.let {
            column(MarqueeHeader.Window) {
                // the blurred image around the frame, fading out evenly on every side
                img {
                    configureImage(null, image, MarqueeHeader.Backdrop, null, true) { }
                }
                box(modify(MarqueeHeader.Frame, Outline)) {
                    metaImage(image, modify(MarqueeHeader.Image, MinWidth(0), FocusInSlow))
                }
            }
        }

        cellGrid(cells, buttons, modify(BorderRadius2, OverflowClip, Outline))

        entityBody(description, links)
    }
}

object MarqueeHeader {
    val Base = Class("marquee-header")
    val Title = Base.withBemElement("title")
    val Window = Base.withBemElement("window")
    val Backdrop = Base.withBemElement("backdrop")
    val Frame = Base.withBemElement("frame")
    val Image = Base.withBemElement("image")
}

// language="CSS"
val MarqueeHeaderCss get() = with(MarqueeHeader) { """
$Base {
    container-type: inline-size;
}

$Title > h1 {
    font-size: clamp(2.5rem, 10cqi, var(--heading-1-size));
    line-height: 1;
    text-align: center;
    text-wrap: balance;
}

$Window {
    position: relative;
}

/* a quarter larger than the frame, centered on it */
$Backdrop {
    position: absolute;
    top: 50%;
    left: 50%;
    z-index: -1;
    width: 125%;
    max-width: none;
    height: 125%;
    translate: -50% -50%;
    object-fit: cover;
    filter: blur(48px) brightness(0.8);
    /* a fade across and a fade down, kept where both are, softens the corners */
    mask-image:
        linear-gradient(to right, transparent, rgb(0 0 0 / .6) 7.5%, rgb(0 0 0 / .9) 15%, black 25%, black 75%, rgb(0 0 0 / .9) 85%, rgb(0 0 0 / .6) 92.5%, transparent),
        linear-gradient(to bottom, transparent, rgb(0 0 0 / .6) 7.5%, rgb(0 0 0 / .9) 15%, black 25%, black 75%, rgb(0 0 0 / .9) 85%, rgb(0 0 0 / .6) 92.5%, transparent);
    mask-composite: intersect;
}

/* the window under the sign, lit at its edges by the page's glow */
$Frame {
    grid-template-rows: minmax(0, 1fr);
    aspect-ratio: 4 / 3;
    max-height: 50dvh;
    border-radius: calc(var(--curve-unit) * 2);
    overflow: clip;
    background: var(--paper-bg);
    box-shadow: 0 0 48px -16px var(--rho-color);

    @container (width >= 640px) { aspect-ratio: 16 / 9; }
}

/* the photo darkens toward the bottom */
$Image {
    grid-template-rows: minmax(0, 1fr);

    > img {
        width: 100%;
        height: 100%;
        mask-image: linear-gradient(to bottom, black 50%, transparent);
    }
}
""" }
