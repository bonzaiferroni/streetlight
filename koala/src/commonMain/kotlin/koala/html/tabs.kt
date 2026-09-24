package koala.html

import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.p

/**
 * Tabs over panels, with [initialTab] selected. The browser wires them with [initTabs].
 *
 * A container with [id] restores its selected tab from `localStorage`.
 */
fun FlowContent.tabs(
    id: Id? = null,
    mod: Modifier? = null,
    viewportMod: Modifier? = null,
    initialTab: Int? = null,
    content: TabScope.() -> Unit,
) {
    column {
        configureTabs(id, mod, viewportMod, initialTab, content)
    }
}

/** Configures this element as [tabs]. */
fun DIV.configureTabs(
    id: Id?,
    mod: Modifier?,
    viewportMod: Modifier?,
    initialTab: Int?,
    content: TabScope.() -> Unit,
) {
    val scope = TabScope()
    scope.content()
    val initialIndex = initialTab ?: 0

    configureTabsContainer(id, mod, initialIndex)
    configureTabsHeader(scope.tabs, initialIndex)
    configureTabsViewport(viewportMod, scope.tabs, initialIndex)
}

/** Marks this element as a tabs container, not yet wired, with [initialIndex] selected. */
fun DIV.configureTabsContainer(
    id: Id?,
    mod: Modifier?,
    initialIndex: Int,
) {
    addModifiers(modify(mod, TabsStyle.Container, TabsStyle.Initial))
    id?.let {
        setId(it)
    }
    setAttribute(TabsStyle.Index.to(initialIndex))
}

/** Adds the row of tab buttons, with [initialIndex] active. */
fun DIV.configureTabsHeader(
    tabs: List<TabHeader>,
    initialIndex: Int,
) {
    row(TabsStyle.Header) {
        tabs.forEachIndexed { index, tab ->
            p {
                if (index == initialIndex) {
                    addModifiers(TabsStyle.IsActive)
                }
                addModifiers(TabsStyle.Button)
                tab.colorScheme?.let {
                    setStyle(Css.ColorScheme.of(it))
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

    val IndexDelta = Property<Int>("index-delta", true)
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
    outline: var(--outline-low);
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