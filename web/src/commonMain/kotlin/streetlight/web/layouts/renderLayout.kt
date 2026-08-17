package streetlight.web.layouts

import kampfire.model.GeoPoint
import koala.css.MinHeight48
import koala.css.modify
import koala.html.column
import koala.html.geoMapMount
import koala.html.markdown
import koala.html.metaImage
import koala.html.row
import koala.html.tab
import koala.html.tabs
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.*
import streetlight.model.ui.LocationUpdateRoute
import streetlight.web.pages.appFooter
import streetlight.web.ui.BodyStyle
import streetlight.web.ui.headerOf

fun FlowContent.renderLayout(content: LocationContent) {
    val layout = content.design?.layout ?: DefaultLayout.location
    renderColumn(layout.blocks, content)
}

fun FlowContent.renderColumn(blocks: List<LayoutBlock>, content: LocationContent) {
    column(BodyStyle.Column) {
        blocks.forEach {
            renderBlock(it, content)
        }
    }
}

fun FlowContent.renderBlock(block: LayoutBlock, content: LocationContent) {
    when (block) {
        HeaderBlock -> renderHeader(content)
        EventsBlock -> renderEvents(content)
        is ImageBlock -> renderImage(block)
        MapBlock -> renderMap(content.location.geoPoint)
        is TabsBlock -> renderTabs(block, content)
        is TextBlock -> renderText(block)
        is RichTextBlock -> renderRichText(block)
        is ColumnsBlock -> renderColumn(block, content)
    }
}

fun FlowContent.renderEvents(content: LocationContent) {
    layoutPosts {
        content.events.forEach {
            entityRow(it)
        }
    }
}

fun FlowContent.renderHeader(content: LocationContent) {
    val location = content.location
    headerOf(
        location = location,
        editRoute = if (content.canEdit) LocationUpdateRoute(location.slug) else null
    )
}

fun FlowContent.renderImage(block: ImageBlock) {
    metaImage(block.image)
}

fun FlowContent.renderMap(geoPoint: GeoPoint) {
    geoMapMount(geoPoint, modify(MinHeight48))
}

fun FlowContent.renderTabs(block: TabsBlock, content: LocationContent) {
    tabs {
        block.tabs.forEach {
            if (it.name == "events" && content.events.isEmpty()) return@forEach // td: better solution
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
    markdown(block.text)
}

fun FlowContent.renderFooter() {
    appFooter()
}

fun FlowContent.renderColumn(block: ColumnsBlock, content: LocationContent) {
    row(modify(BodyStyle.FlexGrid2)) {
        block.blocks.forEach {
            renderBlock(it, content)
        }
    }
}