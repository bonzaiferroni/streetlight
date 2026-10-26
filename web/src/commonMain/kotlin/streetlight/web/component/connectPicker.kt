package streetlight.web.component

import koala.SvgFile
import koala.SvgPack
import koala.component.IconStyle
import koala.component.icon
import koala.component.navigation
import koala.component.textBlock
import koala.component.column
import koala.component.grid
import koala.modifier.*
import kotlinx.css.GridTemplateColumns
import kotlinx.css.GridTemplateRows
import kotlinx.css.pct
import kotlinx.html.FlowContent
import streetlight.model.data.OnboardType
import streetlight.model.ui.ConnectRoute
import streetlight.web.page.AppAttribute

/**
 * Renders a 3:2 grid of four quadrants, one per [OnboardType], each linking to its [ConnectRoute].
 *
 * The quadrant whose type matches an ancestor's [AppAttribute.OnboardType] is lit.
 */
fun FlowContent.connectPicker(mod: Modifier? = null) {
    grid(
        GridTemplateColumns("repeat(2, minmax(0, 1fr))"),
        modify(ShineBg, Outline, MoonShadow, Aspect4By3, Gap2Px, BorderRadius2, OverflowClip, mod),
        GridTemplateRows("repeat(2, minmax(0, 1fr))")
    ) {
        OnboardType.entries.forEach { type ->
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
val OnboardType.svg: SvgPack get() = when (this) {
    OnboardType.BusinessOwner -> SvgFile.BuildingStore
    OnboardType.Performer -> SvgFile.AcousticGuitar
    OnboardType.Developer -> SvgFile.Code
    OnboardType.EventHost -> SvgFile.Speakerphone
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
${OnboardType.entries.joinToString(",\n") { "${AppAttribute.OnboardType.selector(it)} $Option${AppAttribute.ConnectOption.selector(it)}" }} {
    background-color: var(--ink-bg);
    
    ${IconStyle.Icon} {
        color: var(--accent-fg);
    }
}
""" }
