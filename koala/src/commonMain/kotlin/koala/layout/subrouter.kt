package koala.layout

import koala.modifier.*

object SubrouterStyle {
    val Class = Class("subrouter")
    val Header = Class("subrouter__header")
    val Crumbs = Class("subrouter__crumbs")
    val Panel = Class("subrouter__panel")
    val Content = Class("subrouter__content")
}

// language="CSS"
val SubrouterCss get() = with(SubrouterStyle) { """
/* the panel shows beside the content only on a wide screen; narrower, the index route serves as the menu */
$Class {
    display: grid;
    grid-template-areas: "header" "content";
    gap: var(--unit-2);
}

$Header { grid-area: header; }

$Crumbs {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: var(--unit-1);
}

$Panel {
    grid-area: panel;
    display: none;
    flex-direction: column;
    gap: 2px;

    > a {
        padding: var(--unit-1) var(--unit-2);
        background: var(--card-bg);
        transition: var(--transition-background-color);

        &[aria-current="page"] { background: var(--primary-bg); }
    }
}

$Content { grid-area: content; min-width: 0; }

@media (min-width: 960px) {
    $Class {
        grid-template-areas: "header header" "panel content";
        grid-template-columns: var(--unit-32) minmax(0, 1fr);
    }

    $Panel { display: flex; }
}
""" }
