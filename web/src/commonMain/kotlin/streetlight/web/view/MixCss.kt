package streetlight.web.view

import koala.modifier.*

object MixStyle {
    val Cards = Class("mix-cards")
    val Slot = Cards.withBemElement("slot")
}

//language="CSS"
val MixCss get() = with(MixStyle) { """

/* as many cards as fit across, each at least 300px wide, and no more than three */
$Cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(max(300px, (100% - var(--unit) * 2) / 3), 1fr));
    gap: var(--unit);
}

/* a place for a clip, which keeps its height while it holds none */
$Slot {
    width: 100%;
    min-height: var(--unit-4);
    text-align: start;
}
"""}
