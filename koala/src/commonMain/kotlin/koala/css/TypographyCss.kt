package koala.css

val Prose = Class("prose")

// language="CSS"
val TypographyCss get() = """
:root {
    --heading-1-size: 4rem;
    --heading-1-weight: 200;
    --heading-2-size: 2.5rem;
    --heading-2-weight: 200;
    --heading-3-size: 2rem;
    --heading-3-weight: 300;
    --heading-4-size: 1.5rem;
    --heading-4-weight: 300;
    --heading-5-size: 1.1rem;
    --heading-5-weight: 500;
    --heading-6-size: .9rem;
    --heading-6-weight: 400;
    --paragraph-size: 1.1rem;
    --paragraph-line-height: 1.4;
    --text-small: .9rem;
    --text-large: 1.4rem;

    --font-family: "Fira Sans", system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
    --mono-family: ui-monospace, Menlo, Consolas, monospace;
}

span,
p {

}

h1 {
    font-size: var(--heading-1-size);
    font-weight: var(--heading-1-weight);
}

h2 {
    font-size: var(--heading-2-size);
    font-weight: var(--heading-2-weight);
}

h2.bold {
    font-weight: var(--heading-4-weight);
}

h3 {
    font-size: var(--heading-3-size);
    font-weight: var(--heading-3-weight);
}

h3.bold {
    font-weight: var(--heading-4-weight);
}

h4 {
    font-size: var(--heading-4-size);
    font-weight: var(--heading-4-weight);
    text-transform: uppercase;
}

h5 {
    font-size: var(--heading-5-size);
    font-weight: var(--heading-5-weight);
}

h6 {
    font-size: var(--heading-6-size);
    font-weight: var(--heading-6-weight);
    text-transform: uppercase;
}

$Prose {
    line-height: 1.7;
    
    $FlexColumn {
        gap: var(--unit-spacing-2);
    }
}

@media (max-width: 600px) {
    h1.shrinkable,
    .shrinkable h1 { font-size: calc(var(--heading-1-size) * 0.8); }
    h2.shrinkable,
    .shrinkable h2 { font-size: calc(var(--heading-2-size) * 0.8); }
    h3.shrinkable,
    .shrinkable h3 { font-size: calc(var(--heading-3-size) * 0.8); }
}

p {
    text-overflow: ellipsis;
}

h2.grow-text { font-size: calc(var(--heading-2-size) * 1.1); } 
"""

