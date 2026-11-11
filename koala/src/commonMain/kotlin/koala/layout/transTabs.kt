package koala.layout

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.p
import koala.component.IconStyle
import koala.page.Id
import koala.page.setId
import koala.component.box
import koala.component.column
import koala.component.row
import koala.component.span

/**
 * Tabs over panels whose changes run as view transitions, with [initialTab] selected. The browser wires them with
 * `initTransTabs`.
 *
 * A container with [storageKey] restores its selected tab from `localStorage`, and one with [paramKey] from the url.
 */
fun FlowContent.transTabs(
    storageKey: String? = null,
    mod: Modifier? = null,
    viewportMod: Modifier? = null,
    initialTab: Int? = null,
    paramKey: String? = null,
    id: Id? = null,
    content: TabScope.() -> Unit,
) {
    column {
        configureTransTabs(storageKey, mod, viewportMod, initialTab, paramKey, id, content)
    }
}

/** Configures this element as [transTabs]. */
fun DIV.configureTransTabs(
    storageKey: String?,
    mod: Modifier?,
    viewportMod: Modifier?,
    initialTab: Int?,
    paramKey: String?,
    id: Id?,
    content: TabScope.() -> Unit,
) {
    val scope = TabScope()
    scope.content()
    val initialIndex = (initialTab ?: 0).coerceIn(0, scope.tabs.size - 1)

    addModifiers(modify(mod, TransTabsStyle.Container, TransTabsStyle.Initial))
    id?.let { setId(it) }
    setAttribute(TabsStyle.Index.to(initialIndex))
    paramKey?.let { setAttribute(TabsStyle.ParamKey.to(it)) }
    storageKey?.let { setAttribute(TabsStyle.StorageKey.to(it)) }

    configureTransTabsHeader(scope.tabs, initialIndex)

    box(modify(viewportMod, TransTabsStyle.Viewport)) {
        scope.tabs.forEachIndexed { index, tab ->
            val tabContent = tab.content
            column(TransTabsStyle.Panel) {
                if (index == initialIndex) addModifiers(TabsStyle.IsActive)
                tabContent()
            }
        }
    }
}

/** Adds the row of tab buttons, with [initialIndex] active and holding the light. */
fun DIV.configureTransTabsHeader(
    tabs: List<TabHeader>,
    initialIndex: Int,
) {
    row(modify(TabsStyle.Header, Outline)) {
        tabs.forEachIndexed { index, tab ->
            p {
                addModifiers(TabsStyle.Button)
                if (index == initialIndex) {
                    addModifiers(TabsStyle.IsActive)
                    span(TransTabsStyle.Light) { }
                }
                tab.colorScheme?.let { setStyle(Css.ColorScheme.of(it)) }
                tab.icon?.let { icon ->
                    span(modify(IconStyle.Icon, SmallIconHeight, OpacityHigh)) {
                        setStyle(Css.MaskUrl.of(icon.small))
                    }
                }
                span(tab.label, TransTabsStyle.Label)
            }
        }
    }
}

object TransTabsStyle {
    val Container = Class("trans-tabs")
    val Initial = Container.withBemModifier("initial")
    val Viewport = Container.withBemElement("viewport")
    val Forward = Viewport.withBemModifier("forward")
    val Backward = Viewport.withBemModifier("backward")
    val Panel = Container.withBemElement("panel")
    val Light = Container.withBemElement("light")
    val Label = Container.withBemElement("label")
}

// language="CSS"
val TransTabsCss get() = with(TransTabsStyle) { """

/* the light behind the active button draws its background, so the button itself stays clear */
$Container ${TabsStyle.Button} {
    position: relative;
    isolation: isolate;

    &${TabsStyle.IsActive} { background-color: transparent; }
}

/* moved between buttons and paired with itself, it slides to the tab chosen */
$Light {
    position: absolute;
    inset: 0;
    z-index: -1;
    border-radius: inherit;
    background-color: rgb(var(--primary), 0.4);
    view-transition-name: match-element;
}

/* labels paired with themselves, drawn over the light while it travels */
$Label {
    view-transition-name: match-element;
    view-transition-class: ${PrestoKey.Raised};
}

/* paired with itself, its height eases between panels as they cross-fade */
$Viewport {
    view-transition-name: match-element;
    view-transition-class: ${PrestoKey.Height};
}

/* the direction of the change, set before it starts, slides the panels the way the tabs moved */
$Viewport$Forward  { view-transition-class: ${PrestoKey.Height} trans-tabs-forward;  }
$Viewport$Backward { view-transition-class: ${PrestoKey.Height} trans-tabs-backward; }

::view-transition-old(*.trans-tabs-forward)  { animation-name: trans-tabs-out-left;   }
::view-transition-new(*.trans-tabs-forward)  { animation-name: trans-tabs-in-right;   }
::view-transition-old(*.trans-tabs-backward) { animation-name: trans-tabs-out-right;  }
::view-transition-new(*.trans-tabs-backward) { animation-name: trans-tabs-in-left;    }

@keyframes trans-tabs-out-left  { to   { opacity: 0; transform: var(--slide-right-initial); } }
@keyframes trans-tabs-in-right  { from { opacity: 0; transform: var(--slide-left-initial);  } }
@keyframes trans-tabs-out-right { to   { opacity: 0; transform: var(--slide-left-initial);  } }
@keyframes trans-tabs-in-left   { from { opacity: 0; transform: var(--slide-right-initial); } }

/* a panel not shown is not rendered, so the names within it never meet those of the panel shown */
$Panel:not(${TabsStyle.IsActive}) { display: none; }
""" }
