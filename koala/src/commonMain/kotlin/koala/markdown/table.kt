package koala.markdown

// language="CSS"
val TableCss get() = """
${MarkdownStyle.Container} th,
${MarkdownStyle.Container} td {
    padding: .5rem 0.75rem;
    font-size: var(--paragraph-size);
}

/* a border rather than an outline, so the column lines end at its inner edge */
${MarkdownStyle.Container} table {
    border-collapse: separate;
    border-spacing: 0;
    border: 2px solid var(--outline-mid-fg);
    border-radius: var(--curve-unit);
    background: var(--zen-bg);
    background-clip: padding-box;
    box-shadow: var(--moon-shadow);
    overflow: clip;
}

/* a unit beyond the block spacing, on what follows so a table that ends the markdown adds nothing */
${MarkdownStyle.Block} > table + * {
    margin-top: var(--unit-2);
}

${MarkdownStyle.Container} th + th,
${MarkdownStyle.Container} td + td {
    border-left: 2px solid var(--outline-mid-fg);
}

${MarkdownStyle.Container} th {
    text-align: inherit;
    background: var(--zen-bg);
}
"""
