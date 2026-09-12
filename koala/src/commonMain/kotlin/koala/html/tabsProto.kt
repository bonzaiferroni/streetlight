package koala.html

import koala.css.Class
import koala.css.ModifierSet
import koala.css.Property
import koala.css.addModifiers
import koala.css.modify
import koala.css.setStyle
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.p

fun FlowContent.tabsProto(
    id: Id? = null,
    mod: ModifierSet? = null,
    viewportMod: ModifierSet? = null,
    content: TabScope.() -> Unit,
) {
    column {
        configureTabsProto(id, mod, viewportMod, content)
    }
}

fun DIV.configureTabsProto(
    id: Id?,
    mod: ModifierSet?,
    viewportMod: ModifierSet?,
    content: TabScope.() -> Unit,
) {
    val scope = TabScope()
    scope.content()
    val initialIndex = scope.initialIndex

    addModifiers(modify(mod, TabsProtoStyle.Container, TabsProtoStyle.Initial))
    id?.let {
        setId(it)
    }
    setAttribute(TabsProtoStyle.Index.to(initialIndex))

    row(modify(TabsProtoStyle.Header)) {
        scope.tabs.forEachIndexed { index, tab ->
            p {
                if (index == initialIndex) {
                    addModifiers(TabsProtoStyle.IsActive)
                }
                addModifiers(TabsProtoStyle.Button)
                tab.colorScheme?.let {
                    setStyle(Property.ColorScheme.to(it))
                }
                +tab.label
            }
        }
    }

    box(modify(viewportMod, TabsProtoStyle.Viewport)) {
        scope.tabs.forEachIndexed { index, tab ->
            val content = tab.content
            column(modify(TabsProtoStyle.Panel)) {
                if (index == initialIndex) {
                    addModifiers(TabsProtoStyle.IsActive)
                }
                content()
            }
        }
    }
}

object TabsProtoStyle {
    val Container = Class("tabs-proto")
    val Button = Container.withBemElement("button")
    val Header = Container.withBemElement("header")
    val Viewport = Container.withBemElement("viewport")
    val Panel = Container.withBemElement("panel")
    val IsActive = Container.withBemModifier("is-active")
    val Initial = Container.withBemModifier("initial")

    val Index = intAttributeOf("tab")

    val IndexDelta = Property<Int>("index-delta")
}

// language="CSS"
val TabsProtoCss get() = with(TabsProtoStyle) {"""
$Header {
    background-color: var(--void-bg);
    display: flex;
    flex-wrap: wrap;
    gap: var(--unit-spacing);
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
    transition: background-color var(--magic-interval) var(--magic-easing);
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
    transition: height var(--magic-interval) var(--magic-easing);
    width: 100%;
}

$Panel {
    position: absolute;
    top: 0;
    inline-size: 100%;
    visibility: hidden;
    opacity: 0;
    transition:
        opacity var(--magic-interval) var(--magic-easing),
        transform var(--magic-interval) var(--magic-easing),
        visibility var(--magic-interval) var(--magic-easing);

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