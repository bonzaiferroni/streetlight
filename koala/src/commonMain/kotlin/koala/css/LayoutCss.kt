package koala.css

val Box = Class("box")
val Card = Class("card")
val FlexRow = Class("row")
val FlexColumn = Class("column")
val FlexReverse = Class("flex-reverse")

// language="CSS"
val LayoutCss get() = """
:root {
    --unit-spacing-1: var(--unit-spacing);
    --unit-spacing-2: calc(var(--unit-spacing) * 2);
    --unit-spacing-3: calc(var(--unit-spacing) * 3);
    --unit-spacing-4: calc(var(--unit-spacing) * 4);
    --unit-spacing-8: calc(var(--unit-spacing) * 8);
    --unit-spacing-16: calc(var(--unit-spacing) * 16);
    --unit-spacing-32: calc(var(--unit-spacing) * 32);
}

$FlexColumn, $FlexRow {
    display: flex;
    min-width: 0;
    gap: var(--unit-spacing);
}

$FlexColumn {
    flex-direction: column;
    
    &$FlexReverse {
        flex-direction: column-reverse;
    }
}

$FlexRow {
    flex-direction: row;
     
    &$FlexReverse {
        flex-direction: row-reverse;
    }
}

$Card {
    display: flex;
    flex-direction: column;

    gap: var(--unit-spacing);
    border-radius: var(--unit-spacing-2);
    background-color: var(--card-bg);
    padding: var(--unit-spacing);
}

$Box {
    display: grid;
    
    > * {
        grid-area: 1 / 1 / 2 / 2;
    }
}

@media (min-width: 600px) {
    .media-md-row { flex-direction: row; }
    .media-md-column { flex-direction: column; }
    .media-md-row-reverse { flex-direction: row-reverse; justify-content: flex-end; }
}

@media (min-width: 760px) {
    .media-lg-row { flex-direction: row; }
    .media-lg-column { flex-direction: column; }
    .media-lg-row-reverse { flex-direction: row-reverse; justify-content: flex-end; }
}

@container (min-width: 300px) {
    .container-tn-row { flex-direction: row; }
    .container-tn-column { flex-direction: column; }
    .container-tn-row-reverse { flex-direction: row-reverse; justify-content: flex-end; }
}

@container (min-width: 380px) {
    .container-sm-row { flex-direction: row; }
    .container-sm-column { flex-direction: column; }
    .container-sm-row-reverse { flex-direction: row-reverse; justify-content: flex-end; }
}

@container (min-width: 600px) {
    .container-md-row { flex-direction: row; }
    .container-md-column { flex-direction: column; }
    .container-md-row-reverse { flex-direction: row-reverse; justify-content: flex-end; }
    .container-md-margin-top-0 { margin-top: 0 !important; }

    .flex-md-1 { flex: 1 !important; }
    .flex-md-2 { flex: 2 !important; }
}

@container (min-width: 760px) {
    .container-lg-row { flex-direction: row; }
    .container-lg-column { flex-direction: column; }
    .container-lg-row-reverse { flex-direction: row-reverse; justify-content: flex-end; }
}
"""