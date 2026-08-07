package streetlight.web.layouts

import kampfire.model.GeoPoint
import koala.html.column
import koala.html.div
import koala.html.geoMapMount
import koala.html.image
import koala.html.markdown
import koala.html.tab
import koala.html.tabs
import koala.html.textBlock
import kotlinx.html.FlowContent
import streetlight.model.data.DefaultLayout
import streetlight.model.data.EventsBlock
import streetlight.model.data.FooterBlock
import streetlight.model.data.HeaderBlock
import streetlight.model.data.ImageBlock
import streetlight.model.data.LayoutBlock
import streetlight.model.data.LocationContent
import streetlight.model.data.MapBlock
import streetlight.model.data.RichTextBlock
import streetlight.model.data.TabsBlock
import streetlight.model.data.TextBlock
import streetlight.model.ui.LocationUpdateRoute
import streetlight.web.pages.appFooter
import streetlight.web.ui.BodyStyle
import streetlight.web.ui.headerOf

fun FlowContent.buildLayout(content: LocationContent) {
    val layout = content.layout ?: DefaultLayout.location
    buildColumn(layout.blocks, content)
}

fun FlowContent.buildColumn(blocks: List<LayoutBlock>, content: LocationContent) {
    column(BodyStyle.column) {
        blocks.forEach {
            buildBlock(it, content)
        }
    }
}

fun FlowContent.buildBlock(block: LayoutBlock, content: LocationContent) {
    when (block) {
        EventsBlock -> buildEvents(content)
        FooterBlock -> buildFooter()
        HeaderBlock -> buildHeader(content)
        is ImageBlock -> buildImage(block)
        MapBlock -> buildMap(content.location.geoPoint)
        is TabsBlock -> buildTabs(block, content)
        is TextBlock -> buildText(block)
        is RichTextBlock -> buildRichText(block)
    }
}

fun FlowContent.buildEvents(content: LocationContent) {
    layoutPosts {
        content.events.forEach {
            entityRow(it)
        }
    }
}

fun FlowContent.buildHeader(content: LocationContent) {
    val location = content.location
    headerOf(
        location = location,
        editRoute = if (content.canEdit) LocationUpdateRoute(location.slug) else null
    )
}

fun FlowContent.buildImage(block: ImageBlock) {
    image(block.image)
}

fun FlowContent.buildMap(geoPoint: GeoPoint) {
    geoMapMount(geoPoint)
}

fun FlowContent.buildTabs(block: TabsBlock, content: LocationContent) {
    tabs {
        block.tabs.forEach {
            if (it.name == "events" && content.events.isEmpty()) return@forEach // td: better solution
            tab(it.name) {
                buildColumn(it.blocks, content)
            }
        }
    }
}

fun FlowContent.buildFooter() {
    appFooter()
}

fun FlowContent.buildText(block: TextBlock) {
    textBlock(block.text)
}

fun FlowContent.buildRichText(block: RichTextBlock) {
    markdown(block.text)
}