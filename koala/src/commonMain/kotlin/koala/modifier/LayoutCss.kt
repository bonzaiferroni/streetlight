package koala.modifier

val Box = Class("box")
val Card = Class("card")
val FlexRow = Class("row")
val FlexColumn = Class("column")
val FlexReverse = Class("flex-reverse")
val Size100P = Class("size-100")
val FlexItems1 = Class("flex-items-1")
val PaddingX1 = Class("padding-x-1")
val PaddingX2 = Class("padding-x-2")
val PaddingY1 = Class("padding-y-1")
val PaddingY2 = Class("padding-y-2")
val Shrinkable = Class("shrinkable")
val MediaMdRow = Class("media-md-row")
val ContainerMdRow = Class("container-md-row")
val ContainerLgRow = Class("container-lg-row")
val ContainerLgColumn = Class("container-lg-column")
val ContainerMdMarginTop0 = Class("container-md-margin-top-0")

// language="CSS"
val LayoutCss get() = """
:root {
    --unit-1:  var(--unit);
    --unit-2:  calc(var(--unit) * 2);
    --unit-3:  calc(var(--unit) * 3);
    --unit-4:  calc(var(--unit) * 4);
    --unit-8:  calc(var(--unit) * 8);
    --unit-16: calc(var(--unit) * 16);
    --unit-32: calc(var(--unit) * 32);
}

$FlexColumn, $FlexRow {
    display: flex;
    min-width: 0;
    gap: var(--unit);
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

    gap: var(--unit);
    border-radius: var(--unit-2);
    background-color: var(--card-bg);
    padding: var(--unit);
}

$Box {
    display: grid;
    
    > * {
        grid-area: 1 / 1 / 2 / 2;
    }
}

$Size100P { width: 100%; height: 100%; }

$FlexItems1 > * { flex: 1; }

$PaddingX1 { padding-left: var(--unit);   padding-right:  var(--unit); }
$PaddingX2 { padding-left: var(--unit-2); padding-right:  var(--unit-2); }
$PaddingY1 { padding-top:  var(--unit);   padding-bottom: var(--unit); }
$PaddingY2 { padding-top:  var(--unit-2); padding-bottom: var(--unit-2); }

@media (min-width: 600px) {
    $MediaMdRow { flex-direction: row; }
}

@container (min-width: 600px) {
    $ContainerMdRow        { flex-direction: row; }
    $ContainerMdMarginTop0 { margin-top:     0 !important; }
}

@container (min-width: 760px) {
    $ContainerLgRow    { flex-direction: row; }
    $ContainerLgColumn { flex-direction: column; }
}
"""
