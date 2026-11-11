package koala.layout

import koala.modifier.*

object OrderColumnStyle {
    val Column = Class("order-column")
    val Moving = Column.withBemElement("moving")
    val Grip = Column.withBemElement("grip")
}

// language="CSS"
val OrderColumnCss get() = with(OrderColumnStyle) { """

/* the frame the items measure their places from */
$Column { position: relative; }

$Moving { opacity: var(--opacity-low); }

/* a press on the grip moves its item rather than scrolling the page */
$Grip {
    cursor: grab;
    touch-action: none;
}

$Column:has($Moving) { cursor: grabbing; }

/* each item pairs with itself, so items that arrive or leave pop */
$Column$Transitioner > * {
    view-transition-name: match-element;
    view-transition-class: ${PrestoKey.Pop};
}
""" }
