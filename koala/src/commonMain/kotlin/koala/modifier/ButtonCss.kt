package koala.modifier

import koala.html.BtnStyle
import koala.html.ButtonStyle

object ButtonStyle {

}

// language="CSS"
val ButtonCss get() = with(ButtonStyle) {"""
body {
    --btn-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.8), 0 0 6px rgba(0, 0, 0, 0.6);
    --btn-outline: 1px 1px 1px rgba(0, 0, 0, 0.2);
    --btn-font-size: .9rem;
    --btn-font-weight: 500;
    --btn-padding: .72rem 1rem .78rem;
    --btn-border-radius: 1.25rem;

    --zen-button-day: rgba(var(--paper), .5);

    --accent-shadow: 0 0 .8rem var(--accent-button), var(--btn-outline);
    --primary-shadow: 0 0 .8rem var(--primary-button), var(--btn-outline);
    --danger-shadow: 0 0 .8rem rgb(var(--danger)), var(--btn-outline);
}

${BtnStyle.Class} {
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
    min-width: calc(var(--unit) * 16);

    > :first-child { width: 1rem; text-align: center; opacity: .75; }
    > :only-child { grid-column: 1 / -1; justify-self: center; opacity: 1; width: auto; }
    > :nth-child(2) { justify-self: center; }
    > span { font-size: inherit; line-height: 1rem; }
    &:not(:has(*)) { grid-template-columns: 1fr; }
    
    &$Editor {
        box-shadow: none;
        background: var(--system-bg);
        outline: var(--outline-low);
        outline-offset: -2px;
    }
}

$ButtonText {
    text-transform: uppercase;
    font-weight: var(--btn-font-weight);
    font-size: var(--btn-font-size);
    line-height: 1rem;
    white-space: nowrap;
}

${BtnStyle.Class}$Accent {
    box-shadow: var(--accent-shadow);
    background: var(--accent-button);
}

${BtnStyle.Class}$Secondary {
    box-shadow: none;
    background: var(--secondary-button);
}

${BtnStyle.Class}$Zen {
    color: var(--ink-fg);
    background: var(--zen-bg);
    box-shadow: none;
    text-shadow: none;
    outline: var(--outline-low);
    outline-offset: -2px;

    &$PrimaryBg {
        background: var(--primary-card-bg);
    }
}

$DayTheme {
    ${BtnStyle.Class} {
        background: var(--primary-button-day);
    }

    ${BtnStyle.Class}$Secondary {
        box-shadow: none;
        background: var(--secondary-button);
    }

    ${BtnStyle.Class}$Accent {
        box-shadow: var(--btn-outline);
        background: var(--accent-button-day);
    }

    ${BtnStyle.Class}$Zen {
        background: var(--zen-button-day);
    }
}

$Shimmer {
    &${BtnStyle.Class}, ${BtnStyle.Class} {
        pointer-events: none;
        opacity: .9;
    }
}

${BtnStyle.Class}$Danger {
    background: var(--danger-bg);
    box-shadow: var(--danger-shadow);
}

${BtnStyle.Class}$BackgroundImage {
    position: relative;
    overflow: hidden;
    z-index: 0;
    box-shadow: 0 0 .8rem rgba(var(--ink), .2);
}

${BtnStyle.Class}$BackgroundImage::before {
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

${BtnStyle.Class}[disabled] {
    background: var(--gray-bg);
    color: var(--ink-disabled);
    cursor: not-allowed;
    box-shadow: none;
}

${BtnStyle.Class}[disabled]:hover {
    animation: none;
}

${BtnStyle.Class}:hover {
    animation: glow-shadow 10s infinite linear;
}

${ButtonStyle.ElementClass}, [onclick], $Clickable {
    cursor: pointer;
    transition: var(--transition-color);
}

${ButtonStyle.ElementClass}:hover, [onclick]:hover, $Clickable:hover {
    color: var(--accent-fg);
}

$HoverBg {
    box-shadow: inset 0 0 80px transparent;
    transition: var(--transition-color), var(--transition-box-shadow);
}

$HoverBg:hover {
    box-shadow: inset 0 0 80px rgba(var(--ink), 0.2);
}

$Selected {
    outline: 2px solid rgb(var(--primary));
    outline-offset: -2px;
    border-radius: var(--unit-1);
}
"""}