package streetlight.web.view

import koala.modifier.*

object OverviewStyle {
    val Grid = Class("overview-grid")
    val Cells = Class("overview-cells")
    val Section = Cells.withBemElement("section")
    val Cell = Cells.withBemElement("cell")
    val Rest = Cell.withBemModifier("rest")
}

//language="CSS"
val OverviewCss get() = with(OverviewStyle) { """

/* one grid holds every row, so the labels share a column and the cells of each row share the same width */
$Grid {
    display: grid;
    grid-template-columns: max-content minmax(0, 1fr);
    align-items: center;
    gap: 2px var(--unit-2);
}

/* each section takes a share of the row by its measures, set on the section, so the rows line up section by section,
   and each cell takes a share of its section the same way */
$Cells   { display: flex; gap: 4px; }
$Section { display: flex; gap: 2px; }
$Cells > *, $Section > * { min-width: 0; }

$Cell {
    padding: 2px var(--unit-1);
    border-radius: 2px;
    text-align: start;
}

$Rest { box-shadow: inset 0 0 0 1px rgba(var(--ink), var(--opacity-ghost)); }
""" }
