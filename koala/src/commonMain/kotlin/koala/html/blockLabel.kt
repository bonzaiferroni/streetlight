package koala.html

// language="CSS"
val BlockLabelCss get() = """
${Attribute.BlockLabel} {
    position: relative;
}

${Attribute.BlockLabel} > * {
    width: 100%;
}

${Attribute.BlockLabel}::after {
    content: attr(block-label);
    position: absolute;
    top: -0.38rem;
    right: .5rem;

    font-size: 0.65rem;
    padding: 0.12rem 0.5rem;

    background: var(--void-border);
    color: var(--white-dim);
    line-height: 1;
    max-width: 75%;
    white-space: nowrap;
    text-overflow: ellipsis;

    border-radius: 0.4rem;
    box-shadow: 0 0 0.4rem var(--shadow);
}

${Attribute.BlockLabel}.start::after {
    left: .5rem;
    right: auto;
}

${Attribute.BlockLabel}.center::after {
    left: 50%;
    right: auto;
    transform: translateX(-50%);
}
"""