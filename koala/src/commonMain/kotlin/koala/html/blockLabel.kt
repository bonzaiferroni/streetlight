package koala.html

import koala.modifier.Attribute
import koala.modifier.Required
import koala.modifier.Valid

// language="CSS"
val BlockLabelCss get() = """
${Attribute.BlockLabel} {
    position: relative;
}

${Attribute.BlockLabel} > * {
    /* width: 100%; */
}

${Attribute.BlockLabel}::after {
    content: attr(${Attribute.BlockLabel.identifier});
    position: absolute;
    top: -0.38rem;
    right: .5rem;

    font-size: 0.65rem;
    font-weight: 500;
    padding: 0.12rem 0.5rem 0.1rem;

    background: var(--void-border);
    color: var(--ink-dim);
    font-family: var(--font-family);
    line-height: 1;
    max-width: 75%;
    white-space: nowrap;
    text-overflow: ellipsis;
    text-transform: uppercase;

    border-radius: 0.4rem;
    box-shadow: var(--input-shadow);
}

$Required${Attribute.BlockLabel}::after,
$Required ${Attribute.BlockLabel}::after {
    background: var(--required-bg);
} 

$Valid${Attribute.BlockLabel}::after,
$Valid ${Attribute.BlockLabel}::after {
    background: var(--valid-bg);
} 
"""