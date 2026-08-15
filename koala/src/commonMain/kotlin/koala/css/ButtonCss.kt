package koala.css

object ButtonStyle {
}

// language="CSS"
val ButtonCss get() = with(ButtonStyle) {"""
body {
    --btn-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.8), 0 0 6px rgba(0, 0, 0, 0.6);
    --btn-outline: 1px 1px 1px rgba(0, 0, 0, 0.2);
    --btn-font-size: .9rem;
    --btn-font-weight: 500;
    --btn-padding: .75rem 1rem;
    --btn-border-radius: 1.25rem;

    --zen-button-day: rgba(var(--paper), .5);

    --accent-shadow: 0 0 .8rem var(--accent-button), var(--btn-outline);
    --primary-shadow: 0 0 .8rem var(--primary-button), var(--btn-outline);
    --danger-shadow: 0 0 .8rem rgb(var(--danger)), var(--btn-outline);
}

.btn {
    display: grid;
    grid-template-columns: auto 1fr;
    align-items: center;
    padding: var(--btn-padding);
    border: none;
    border-radius: var(--btn-border-radius);
    background: var(--primary-button);
    color: white;
    font-size: var(--btn-font-size);
    font-weight: var(--btn-font-weight);
    line-height: 1rem;
    cursor: pointer;
    transition: background 0.2s ease;
    box-shadow: var(--primary-shadow);
    text-transform: uppercase;
    text-shadow: var(--btn-text-shadow);
    white-space: nowrap;
    min-width: calc(var(--unit-spacing) * 16);

    > :first-child { width: 1rem; text-align: center; opacity: .75; }
    > :only-child { grid-column: 1 / -1; justify-self: center; opacity: 1; width: auto; }
    > :nth-child(2) { justify-self: center; }
    > span { font-size: inherit; line-height: 1rem; }
    &:not(:has(*)) { grid-template-columns: 1fr; }
    
    &$Editor {
        box-shadow: none;
        background: var(--editor-bg);
        outline: var(--outline-low);
        outline-offset: -2px;
    }
}

.btn-text {
    text-transform: uppercase;
    font-weight: var(--btn-font-weight);
    font-size: var(--btn-font-size);
    line-height: 1rem;
    white-space: nowrap;
}

.btn.accent {
    box-shadow: var(--accent-shadow);
    background: var(--accent-button);
}

.btn.secondary {
    box-shadow: none;
    background: var(--secondary-button);
}

.btn.zen {
    color: var(--ink-fg);
    background: var(--zen-bg);
    box-shadow: none;
    text-shadow: none;
    outline: var(--outline-low);
    outline-offset: -2px;

    &.primary-bg {
        background: var(--primary-card-bg);
    }
}

.day-theme {
    .btn {
        background: var(--primary-button-day);
    }

    .btn.secondary {
        box-shadow: none;
        background: var(--secondary-button);
    }

    .btn.accent {
        box-shadow: var(--btn-outline);
        background: var(--accent-button-day);
    }

    .btn.zen {
        background: var(--zen-button-day);
    }
}

.working {
    &.btn, .btn {
        pointer-events: none;
        opacity: .9;
    }
}

.btn.danger {
    background: var(--danger-bg);
    box-shadow: var(--danger-shadow);
}

.btn.background-image {
    position: relative;
    overflow: hidden;
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
    color: var(--ink-disabled);
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
    transition: box-shadow var(--magic-interval) var(--magic-easing);
}

[onclick]:hover, .clickable:hover {
    box-shadow: inset 0 0 0 9999px rgba(255,255,255,.04);
}

$Selected {
    outline: 2px solid rgb(var(--primary));
    outline-offset: -2px;
    border-radius: var(--unit-spacing-1);
}

.highlighted {
    color: var(--primary-fg);
}    
"""}