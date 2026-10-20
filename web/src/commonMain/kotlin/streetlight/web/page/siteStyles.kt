package streetlight.web.page

import koala.modifier.*
import koala.model.MarkerSheet
import streetlight.web.layout.CellGridCss
import streetlight.web.layout.EntityDialogCss
import streetlight.web.layout.FeedRowCss
import streetlight.web.layout.EntityBodyCss
import streetlight.web.layout.FeedSectionCss
import streetlight.web.component.LogoCss
import streetlight.web.view.EarthCss
import streetlight.web.view.RouteDockCss
import streetlight.web.component.TagFilterMenuCss
import koala.markdown.MarkdownCss
import koala.component.GlowControlCss
import koala.layout.LazyColumnCss
import koala.markdown.TextEditorCss
import streetlight.web.component.StarToggleCss
import streetlight.web.layout.BodyCss
import streetlight.web.component.CuratorMenuStyle
import streetlight.web.view.InboxCss
import streetlight.web.view.ConnectCss
import streetlight.web.component.ConnectPickerCss
import streetlight.web.view.LayoutBuilderCss
import streetlight.web.layout.LayoutStyleCss
import streetlight.web.view.TalkLogCss
import streetlight.web.layout.TextDeltaCss
import koala.component.*
import koala.interop.*
import koala.layout.*
import koala.markdown.*

private val KtStyles = listOf(
    // koala module
    IconCss,
    LogoCss,
    MarkdownCss,
    ListingCss,
    SearchFieldCss,
    TableCss,
    ListItemCss,
    PopoverCss,
    SectionCss,
    ActionCss,
    TextLabelCss,
    MessageBoxCss,
    SwitchCss,
    SwitchMenuCss,
    SideTabsCss,
    SubrouterCss,
    FlowBlockCss,
    ItemsBlockCss,
    WireBlockCss,
    FillImageCss,
    CarouselCss,
    HeaderImageCss,
    FilePickerCss,
    BlockLabelCss,
    DialogCss,
    LottieCss,
    IconButtonCss,
    ContainImageCss,
    GridColumnsCss,
    RouteDockCss,
    TagFilterMenuCss,
    BodyCss,
    HrCss,
    GlowControlCss,
    TextEditorCss,
    LazyColumnCss,
    ProgressBarCss,
    TabsCss,

    // web module
    StreetlightCss,
    AppBodyCss,
    PageThemeCss,
    AppOverlayCss,
    EarthCss,
    TalkLogCss,
    FeedRowCss,
    EntityDialogCss,
    EntityBodyCss,
    FeedSectionCss,
    CellGridCss,
    StarToggleCss,
    MarkerSheet,
    TextDeltaCss,
    LayoutBuilderCss,
    LayoutStyleCss,
    InboxCss,
    CuratorMenuStyle,
    ConnectPickerCss,
    ConnectCss,
)

/** The site-wide stylesheet: the base sheets of koala, then the stylesheet of each component and layout. */
fun buildSiteStyles() = buildString {
    appendLine(ResetCss)
    appendLine(StylesCss)
    appendLine(TypographyCss)
    appendLine(LayoutCss)
    appendLine(ThemeCss)
    appendLine(MagicCss)
    appendLine(ButtonCss)
    appendLine(GeoMapCss)

    // styles declared with components and layouts in koala
    KtStyles.forEach {
        appendLine(it)
    }

    appendLine(DisplayCss)
    appendLine(TextCss)
}
