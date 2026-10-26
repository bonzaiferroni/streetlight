package streetlight.web.layout

import kampfire.api.Markdown
import koala.Image
import koala.modifier.*
import koala.component.column
import koala.component.heading1
import koala.component.metaImage
import koala.component.textBlock
import koala.component.configureImage
import koala.component.filigree
import kotlinx.css.px
import kotlinx.html.img
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import koala.model.ExtraLink
import streetlight.web.model.ThemeColor

/**
 * The header of a page opened by its image across the body's width, the image dissolving into the page where the
 * title sits, then the cells, buttons, description, and links.
 *
 * [descriptor] is the small line under the title, such as "a galaxy". Without an [image], the title opens the header.
 */
fun FlowContent.heroHeader(
    title: String,
    descriptor: String,
    image: Image?,
    subtitle: String? = null,
    colorScheme: ThemeColor = ThemeColor.Primary,
    description: Markdown? = null,
    cells: List<EntityCell>? = null,
    buttons: List<EntityButton>? = null,
    links: List<ExtraLink>? = null,
    mod: Modifier? = null,
    block: DIV.() -> Unit = { },
) {
    column(modify(HeroHeader.Base, Gap(2), mod)) {
        setStyle(Css.ColorScheme.of(colorScheme.cssValue))
        block()

        image?.let {
            metaImage(image, modify(HeroHeader.Image, MinWidth(0), FocusInSlow)) {
                // the blurred image behind the page's sides, carrying its colors outward
                img {
                    configureImage(null, image, HeroHeader.Backdrop, null, true) { }
                }
            }
        }

        val fgMod = if (image != null) modify(WhiteFg, TextShadow) else null
        column(modify(HeroHeader.Title, PaddingX2, Gap(1), AlignSelfStart, AlignItemsCenter, fgMod)) {
            heading1(title, FocusTarget)
            filigree(AlignSelfStretch) {
                textBlock(descriptor, modify(Italic, OpacityHalf))
            }
            subtitle?.let {
                textBlock(subtitle, OpacityHigh)
            }
        }

        cellGrid(cells, buttons, modify(BorderRadius2, OverflowClip, Outline))

        entityBody(description, links, modify(TextSmall, Prose))
    }
}

object HeroHeader {
    val Base = Class("hero-header")
    val Image = Base.withBemElement("image")
    val Title = Base.withBemElement("title")
    val Backdrop = Base.withBemElement("backdrop")
}

// language="CSS"
val HeroHeaderCss get() = with(HeroHeader) { """
$Base {
    container-type: inline-size;
}

$Image {
    position: relative;
    aspect-ratio: 4 / 3;
    max-height: 50dvh;
    grid-template-rows: minmax(0, 1fr);
    border-inline: var(--outline-mid);
    border-image: linear-gradient(var(--outline-mid-fg), transparent) 1;

    @container (width >= 640px) { aspect-ratio: 21 / 9; }

    > img {
        width: 100%;
        height: 100%;
        mask-image: linear-gradient(to bottom, black 55%, transparent);
    }
}

/* centered on the viewport, its radial fade centered at the image's top */
$Image > $Backdrop {
    position: absolute;
    top: 0;
    left: 50%;
    z-index: -1;
    width: 100dvw;
    max-width: none;
    height: 150%;
    translate: -50%;
    object-fit: cover;
    filter: blur(48px) brightness(0.8);
    mask-image: radial-gradient(
        ellipse 50% 100% at 50% 0%,
        black 50%, rgb(0 0 0 / .9) 70%, rgb(0 0 0 / .6) 85%, transparent
    );
}

$Title {
    position: relative;

    > h1 {
        font-size: clamp(2.5rem, 10cqi, var(--heading-1-size));
        line-height: 1;
    }
}

$Image + $Title {
    margin-top: calc(var(--unit) * -10);
}
""" }
