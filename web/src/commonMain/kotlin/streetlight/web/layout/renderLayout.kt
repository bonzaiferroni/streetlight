package streetlight.web.layout

import koala.modifier.*
import koala.component.textBlock
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.web.page.appFooter
import streetlight.web.shell.MediaShell
import koala.component.*
import koala.layout.*
import koala.markdown.*
import koala.page.*

/** Renders the page of [content] by its design's layout, or the default layout of its kind. */
fun FlowContent.renderLayout(content: DesignContent) {
    val layout = content.design?.layout ?: PageLayout.defaultOf(content)
    renderColumn(layout.blocks, content)
}

fun FlowContent.renderColumn(blocks: List<LayoutBlock>, content: DesignContent) {
    column(BodyStyle.MainColumn) {
        blocks.forEach {
            renderBlock(it, content)
        }
    }
}

fun FlowContent.renderBlock(block: LayoutBlock, content: DesignContent) {
    when (block) {
        is HeaderBlock -> renderHeader(block, content)
        EventsBlock -> renderEvents(content)
        CommentsBlock -> renderComments()
        PostsBlock -> renderPosts(content)
        MapBlock -> renderMap(content)
        SkillsBlock -> renderSkills(content)
        is TextBlock -> renderText(block)
        is RichTextBlock -> renderRichText(block)
        is HeadingBlock -> renderHeading(block)
        is ImageBlock -> renderImage(block)
        is GalleryBlock -> renderGallery(block)
        is TabsBlock -> renderTabs(block, content)
        is ColumnsBlock -> renderColumn(block, content)
    }
}

fun FlowContent.renderHeading(block: HeadingBlock) {
    when (block.hasFiligree) {
        true -> filigree { heading(block.level, block.text) }
        else -> heading(block.level, block.text)
    }
}

fun FlowContent.renderComments() {
    mount(MediaShell.TalkId)
}

fun FlowContent.renderPosts(content: DesignContent) {
    when (content) {
        is StarContent -> feedSection(content.feed)
        is GalaxyContent -> feedSection(content.feed)
        else -> error("not posts content")
    }
}

fun FlowContent.renderEvents(content: DesignContent) {
    when (content) {
        is LocationContent -> feedSection(content.feed)
        else -> error("not events content")
    }
}

/** The star's skills, nothing when they have none. */
fun FlowContent.renderSkills(content: DesignContent) {
    when (content) {
        is StarContent -> if (content.skills.isNotEmpty()) skillsSection(content.skills)
        else -> error("not skills content")
    }
}

/** The header of [content], laid out by [block]. A medium has one layout of its own. */
fun FlowContent.renderHeader(block: HeaderBlock, content: DesignContent) {
    when (content) {
        is LocationContent -> headerOf(
            location = content.location,
            header = block,
        )
        is Media -> headerOf(content)
        is StarContent -> headerOf(content.star, block)
        is GalaxyContent -> headerOf(content.galaxy, block)
    }
}

fun FlowContent.renderImage(block: ImageBlock) {
    val shapeMod = block.shape.toMod()
    val fitMod = block.fit.toMod()
    metaImage(block.image, modify(LayoutStyle.Image, shapeMod, fitMod)) {
        block.width?.let {
            setStyle(Css.Width.of(it.pct))
        }
    }
}

fun FlowContent.renderGallery(block: GalleryBlock) {
    div(LayoutStyle.Gallery) {
        setStyle(Css.ColumnCount.of(block.columns))
        block.images.forEach {
            image(it, modify(block.shape.toMod(), OverflowClip))
        }
    }
}

fun FlowContent.renderMap(content: DesignContent) {
    geoMapMount(content.geoPoint, MinHeight(48))
}

fun FlowContent.renderTabs(block: TabsBlock, content: DesignContent) {
    tabs {
        block.tabs.forEach {
            if (it.name == "events" && content is LocationContent && content.feed.entities.isEmpty()) return@forEach // td: better solution
            tab(it.name) {
                renderColumn(it.blocks, content)
            }
        }
    }
}

fun FlowContent.renderText(block: TextBlock) {
    textBlock(block.text)
}

fun FlowContent.renderRichText(block: RichTextBlock) {
    val sizeMod = block.size.toMod()
    markdown(block.text, sizeMod)
}

fun FlowContent.renderFooter() {
    appFooter()
}

fun FlowContent.renderColumn(block: ColumnsBlock, content: DesignContent) {
    row(BodyStyle.ColumnRow) {
        block.blocks.forEach {
            renderBlock(it, content)
        }
    }
}

fun ImageShape?.toMod() = when (this) {
    ImageShape.Square -> null
    ImageShape.Rounded, null -> BorderRadius2
    ImageShape.Circle -> CircleShape
    ImageShape.Ellipse -> BorderRadius50P
    ImageShape.Pill -> BorderRadiusPill
    ImageShape.Chopped -> Chopped
}

fun ObjectFit?.toMod() = when (this) {
    ObjectFit.Fill, null, -> ObjectFitFill
    ObjectFit.Stretch -> null
    ObjectFit.Contain -> ObjectFitContain
    ObjectFit.Cover -> ObjectFitCover
    ObjectFit.ScaleDown -> ObjectFitScaleDown
}

fun Size3?.toMod() = when (this) {
    Size3.Small -> TextSmall
    Size3.Normal, null -> null
    Size3.Large -> TextLarge
}