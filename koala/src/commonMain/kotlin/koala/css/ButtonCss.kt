package koala.css

// language="CSS"
val ButtonCss get() = """
:root {
    --btn-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.8), 0 0 6px rgba(0, 0, 0, 0.6);
    --btn-font-size: .8rem;
    --btn-font-weight: 500;
    --btn-padding: .75rem 1rem;
}

.btn {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: var(--btn-padding);
    border: none;
    border-radius: 1.25rem;
    background: var(--primary-button);
    color: var(--ink);
    font-size: var(--btn-font-size);
    font-weight: var(--btn-font-weight);
    line-height: 1rem;
    cursor: pointer;
    transition: background 0.2s ease;
    box-shadow: 0 0 .8rem var(--primary-button);
    text-transform: uppercase;
    text-shadow: var(--btn-text-shadow);
    text-align: center;
    white-space: nowrap;
    min-width: calc(var(--unit-spacing) * 16);
}

.btn.accent {
    box-shadow: 0 0 .8rem var(--accent-button);
    background: var(--accent-button);
}

.btn.secondary {
    box-shadow: 0 0 .8rem var(--gray-fg);
    background: var(--secondary-button);
}

.btn.background-image {
    position: relative;
    overflow: hidden;
    color: inherit;
    z-index: 0;
    box-shadow: 0 0 .8rem rgba(var(--ink), .2);
}

.btn.background-image::before {
    content: "";
    position: absolute;
    inset: 0;
    background-image:
            linear-gradient(rgba(0, 0, 0, 0.2), rgba(0, 0, 0, 0.2)),
            var(--background-url);
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    filter: blur(2px);
    transform: scale(1.05);
    z-index: -1;
}

.btn[disabled] {
    background: var(--gray-bg);
    color: var(--white-disabled);
    cursor: not-allowed;
    box-shadow: none;
}

.btn[disabled]:hover {
    animation: none;
}

.btn:hover {
    animation: glow-shadow 10s infinite linear;
}

[onclick], .clickable {
    cursor: pointer;
    transition: box-shadow 200ms ease-in-out;
}

/*a > .layout-card:hover*/
[onclick]:hover, .clickable:hover {
    box-shadow: inset 0 0 0 9999px rgba(255,255,255,.04);
}
"""