package koala.css

// language="CSS"
val TypographyCss get() = """
:root {
    --heading-1-size: 4rem;
    --heading-1-weight: 200;
    --heading-2-size: 2rem;
    --heading-2-weight: 200;
    --heading-3-size: 1.5rem;
    --heading-3-weight: 300;
    --heading-4-size: 1.25rem;
    --heading-4-weight: 400;
    --heading-5-size: 1rem;
    --heading-5-weight: 500;
}

h1 {
    font-size: var(--heading-1-size);
    font-weight: var(--heading-1-weight);
}

h2 {
    font-size: var(--heading-2-size);
    font-weight: var(--heading-2-weight);
}

h3 {
    font-size: var(--heading-3-size);
    font-weight: var(--heading-3-weight);
}

h4 {
    font-size: var(--heading-4-size);
    font-weight: var(--heading-4-weight);
}

h5 {
    font-size: var(--heading-5-size);
    font-weight: var(--heading-5-weight);
}

@media (max-width: 600px) {
    .shrinkable h1 { font-size: calc(var(--heading-1-size) * 0.8); }
    .shrinkable h2 { font-size: calc(var(--heading-2-size) * 0.8); }
    .shrinkable h3 { font-size: calc(var(--heading-3-size) * 0.8); }
}

p {
    text-overflow: ellipsis;
}

h2.grow-text { font-size: calc(var(--heading-2-size) * 1.1); }

"""