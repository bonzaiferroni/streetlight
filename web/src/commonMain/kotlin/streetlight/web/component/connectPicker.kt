package streetlight.web.component

import koala.SvgFile
import koala.SvgPack
import koala.component.icon
import koala.component.navigation
import koala.component.textBlock
import koala.layout.column
import koala.layout.grid
import koala.modifier.*
import kotlinx.css.GridTemplateColumns
import kotlinx.css.GridTemplateRows
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.model.data.ConnectType
import streetlight.model.ui.ConnectRoute
import streetlight.web.page.AppAttribute

/**
 * Renders a 3:2 grid of four quadrants, one per [ConnectType], each linking to its [ConnectRoute].
 *
 * The quadrant whose type matches an ancestor's [AppAttribute.ConnectType] is lit.
 */
fun FlowContent.connectPicker(mod: Modifier? = null) {
    grid(
        GridTemplateColumns("repeat(2, minmax(0, 1fr))"),
        modify(Aspect3By2, Gap2Px, BorderRadius2, OverflowClip, mod),
        GridTemplateRows("repeat(2, minmax(0, 1fr))")
    ) {
        ConnectType.entries.forEach { type ->
            navigation(ConnectRoute(type), modify(ConnectPickerStyle.Option, AppAttribute.ConnectOption.to(type), CardBg)) {
                column(modify(Height(100.pct), JustifyContentCenter, AlignItemsCenter, TextSmall, TextUppercase)) {
                    icon(type.svg.animated, Width(5))
                    textBlock(type.label)
                }
            }
        }
    }
}

/** The icon of this connect type. */
val ConnectType.svg: SvgPack get() = when (this) {
    ConnectType.BusinessOwner -> SvgFile.BuildingStore
    ConnectType.Performer -> SvgFile.AcousticGuitar
    ConnectType.Developer -> SvgFile.Code
    ConnectType.EventHost -> SvgFile.Speakerphone
}

object ConnectPickerStyle {
    val Option = Class("connect-picker__option")
}

// language="CSS"
val ConnectPickerCss get() = with(ConnectPickerStyle) { """
$Option {
    transition: var(--transition-background-color);
}

/* the option of the type chosen on an ancestor */
${ConnectType.entries.joinToString(",\n") { "${AppAttribute.ConnectType.selector(it)} $Option${AppAttribute.ConnectOption.selector(it)}" }} {
    background-color: var(--primary-bg);
}
""" }
