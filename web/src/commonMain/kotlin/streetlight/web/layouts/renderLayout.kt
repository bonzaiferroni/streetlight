package streetlight.web.layouts

import koala.css.*
import koala.html.*
import koala.html.textBlock
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.model.ui.LocationUpdateRoute
import streetlight.web.pages.appFooter
import streetlight.web.shells.MediaShell
import streetlight.web.ui.BodyStyle
import streetlight.web.ui.LayoutStyle
import streetlight.web.ui.headerOf

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
        HeaderBlock -> renderHeader(content)
        EventsBlock -> renderEvents(content)
        CommentsBlock -> renderComments()
        PostsBlock -> renderPosts(content)
        MapBlock -> renderMap(content)
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
        is StarContent -> postSection(content.posts)
        is GalaxyContent -> postSection(content.posts)
        else -> error("not posts content")
    }
}

fun FlowContent.renderEvents(content: DesignContent) {
    when (content) {
        is LocationContent -> layoutPosts {
            content.events.forEach {
                entityRow(it)
            }
        }
        else -> error("not events content")
    }
}

fun FlowContent.renderHeader(content: DesignContent) {
    when (content) {
        is LocationContent -> headerOf(
            location = content.location,
            editRoute = if (content.canEdit) LocationUpdateRoute(content.location.slug) else null
        )
        is Media -> headerOf(content)
        is StarContent -> headerOf(content.star)
        is GalaxyContent -> headerOf(content.galaxy)
    }
}

fun FlowContent.renderImage(block: ImageBlock) {
    val shapeMod = block.shape.toMod()
    val fitMod = block.fit.toMod()
    metaImage(block.image, modify(LayoutStyle.Image, shapeMod, fitMod)) {
        block.width?.let {
            setStyle(Property.Width.to(it.pct))
        }
    }
}

fun FlowContent.renderGallery(block: GalleryBlock) {
    div(modify(LayoutStyle.Gallery)) {
        setStyle(Property.ColumnCount.to(block.columns))
        block.images.forEach {
            image(it, modify(block.shape.toMod(), OverflowClip))
        }
    }
}

fun FlowContent.renderMap(content: DesignContent) {
    geoMapMount(content.geoPoint, modify(MinHeight48))
}

fun FlowContent.renderTabs(block: TabsBlock, content: DesignContent) {
    tabs {
        block.tabs.forEach {
            if (it.name == "events" && content is LocationContent && content.events.isEmpty()) return@forEach // td: better solution
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
    markdown(block.text, modify(sizeMod))
}

fun FlowContent.renderFooter() {
    appFooter()
}

fun FlowContent.renderColumn(block: ColumnsBlock, content: DesignContent) {
    row(modify(BodyStyle.FormRow)) {
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