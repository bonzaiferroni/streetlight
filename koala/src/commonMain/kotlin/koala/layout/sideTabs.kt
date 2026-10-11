package koala.layout

import koala.modifier.*

object SideTabsStyle {
    val Class = Class("side-tabs")
    val Rail = Class("side-tabs__rail")
    val Tab = Class("side-tabs__tab")
    val Panel = Class("side-tabs__panel")
}

// language="CSS"
val SideTabsCss get() = with(SideTabsStyle) { """
/* the rail and panel stretch to the height a caller limits, and scroll on their own */
$Class {
    display: flex;

    > $Panel { flex: 1; min-width: 0; overflow: hidden auto; }
}

$Rail {
    display: flex;
    flex-direction: column;
    gap: 2px;
    overflow-y: auto;
}

$Tab {
    padding: var(--unit-1) var(--unit-2);
    border: none;
    background: var(--card-bg);
    color: inherit;
    text-align: center;
    cursor: pointer;
    transition: var(--transition-background-color);

    &[aria-selected="true"] {
        background: var(--primary-bg);
        cursor: default;
    }

    &:focus-visible {
        outline: 2px solid currentColor;
        outline-offset: -2px;
    }
}
""" }
