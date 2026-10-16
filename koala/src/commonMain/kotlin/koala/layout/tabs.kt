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
 * Tabs over panels, with [initialTab] selected. The browser wires them with [initTabs].
 *
 * A container with [storageKey] restores its selected tab from `localStorage`.
 */
fun FlowContent.tabs(
    storageKey: String? = null,
    mod: Modifier? = null,
    viewportMod: Modifier? = null,
    initialTab: Int? = null,
    paramKey: String? = null,
    id: Id? = null,
    content: TabScope.() -> Unit,
) {
    column {
        configureTabs(storageKey, mod, viewportMod, initialTab, paramKey, id, content)
    }
}

/** Configures this element as [tabs]. */
fun DIV.configureTabs(
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
    val initialIndex = initialTab ?: 0

    configureTabsContainer(storageKey, mod, initialIndex, paramKey, id)
    configureTabsHeader(scope.tabs, initialIndex)
    configureTabsViewport(viewportMod, scope.tabs, initialIndex)
}

/** Marks this element as a tabs container, not yet wired, with [initialIndex] selected. */
fun DIV.configureTabsContainer(
    storageKey: String?,
    mod: Modifier?,
    initialIndex: Int,
    paramKey: String?,
    id: Id?,
) {
    addModifiers(modify(mod, TabsStyle.Container, TabsStyle.Initial))
    id?.let {
        setId(it)
    }
    setAttribute(TabsStyle.Index.to(initialIndex))
    paramKey?.let {
        setAttribute(TabsStyle.ParamKey.to(it))
    }
    storageKey?.let {
        setAttribute(TabsStyle.StorageKey.to(it))
    }
}

/** Adds the row of tab buttons, with [initialIndex] active. */
fun DIV.configureTabsHeader(
    tabs: List<TabHeader>,
    initialIndex: Int,
) {
    row(modify(TabsStyle.Header, Outline)) {
        tabs.forEachIndexed { index, tab ->
            p {
                if (index == initialIndex) {
                    addModifiers(TabsStyle.IsActive)
                }
                addModifiers(TabsStyle.Button)
                tab.colorScheme?.let {
                    setStyle(Css.ColorScheme.of(it))
                }
                tab.icon?.let { icon ->
                    span(modify(IconStyle.Icon, SmallIconHeight, OpacityHigh)) {
                        setStyle(Css.MaskUrl.of(icon.small))
                    }
                }
                +tab.label
            }
        }
    }
}

/** Adds the viewport holding a panel per tab, with [initialIndex] shown. */
fun DIV.configureTabsViewport(
    viewportMod: Modifier?,
    tabs: List<Tab>,
    initialIndex: Int,
) {
    box(modify(viewportMod, TabsStyle.Viewport)) {
        tabs.forEachIndexed { index, tab ->
            val content = tab.content
            column(TabsStyle.Panel) {
                if (index == initialIndex) {
                    addModifiers(TabsStyle.IsActive)
                }
                content()
            }
        }
    }
}

object TabsStyle {
    val Container = Class("tabs")
    val Button = Container.withBemElement("button")
    val Header = Container.withBemElement("header")
    val Viewport = Container.withBemElement("viewport")
    val Panel = Container.withBemElement("panel")
    val IsActive = Container.withBemModifier("is-active")
    val Initial = Container.withBemModifier("initial")

    val Index = intAttributeOf("tab")
    val ParamKey = stringAttributeOf("param")
    val StorageKey = stringAttributeOf("storage-key")
}

// language="CSS"
val TabsCss get() = with(TabsStyle) {"""
$Header {
    background-color: var(--void-bg);
    display: flex;
    flex-wrap: wrap;
    gap: var(--unit);
    border-radius: 24px 24px 12px 12px;
    width: 100%;
    text-transform: uppercase;
    font-size: var(--text-small);
}

$Button {
    color: var(--color-scheme);

    flex: 1 0 100px;
    min-width: 0;
    white-space: nowrap;
    padding: 0.5rem 1rem;
    border-radius: 24px 24px 12px 12px;
    cursor: pointer;
    transition: var(--transition-background-color);
    text-align: center;
    user-select: none;
    text-overflow: ellipsis;
    overflow: hidden;
    
    > ${IconStyle.Icon} {
        vertical-align: calc((1cap - var(--unit) * 3) / 2);
        margin-inline-end: var(--unit);
    }

    &:hover {
        background-color: rgba(var(--primary), 0.1);
    }
    
    &$IsActive {
        background-color: rgb(var(--primary), 0.4);
        cursor: default;
        animation: glow-shadow 10s infinite linear;
    }
}

$Viewport {
    position: relative;
    transition: var(--transition-height);
    width: 100%;
}

$Panel {
    position: absolute;
    top: 0;
    inline-size: 100%;
    visibility: hidden;
    opacity: 0;
    transition: var(--transition-opacity), var(--transition-transform), var(--transition-visibility);

    &:has(~ $IsActive) {
        transform: translateX(-20px);
    }

    &$IsActive ~ & {
        transform: translateX(20px);
    }

    &$IsActive {
        position: static;
        visibility: visible;
        opacity: 1;
        transform: translateX(0);
    }
}
"""}