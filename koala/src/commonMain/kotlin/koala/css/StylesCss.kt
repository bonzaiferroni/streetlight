@file:Suppress("CssOverwrittenProperties", "CssInvalidPseudoSelector")

package koala.css

const val CONTENT_PANEL_WIDTH_PX = 1080
const val RIGHT_PANEL_WIDTH_PX = 360
const val SIDE_PANEL_WIDTH_PX = 192

// language="CSS"
val StylesCss get() = """
:root {
    --content-panel-width: ${CONTENT_PANEL_WIDTH_PX}px;
    --right-panel-width: ${RIGHT_PANEL_WIDTH_PX}px;

    --breakpoint-sm: 0px;
    --breakpoint-md: 600px;
    --breakpoint-lg: 760px;
    --breakpoint-vlg: 1000px;
}

html {
    font-size: 100%;
}

@media (max-width: 600px) {
    html {
        font-size: 87.5%; 
    }
}

body {
    font-family: var(--font-family);
    font-size: var(--paragraph-size);
    line-height: var(--paragraph-line-height);
    color: var(--ink-fg);
    -webkit-font-smoothing: antialiased;
    transition: 
        background-color var(--magic-interval) var(--magic-easing), 
        color var(--magic-interval) var(--magic-easing);
}

@keyframes hueSpin {
    to { filter: hue-rotate(360deg); }
}

input[type="text"],
input[type="password"],
input[type="datetime-local"],
input[type="time"],
input[type="date"],
textarea {
    font-family: var(--font-family);
    outline: none;
    padding: var(--unit-spacing);
    font-size: var(--paragraph-size);
    background-color: var(--void-bg);
    color: rgb(var(--ink));
    line-height: var(--paragraph-line-height);

    border: 1px solid var(--void-border);
    border-radius: var(--unit-spacing);
    box-shadow: var(--input-shadow);
}

input[type="checkbox"] {
    appearance: none;
    -webkit-appearance: none;

    background-color: var(--void-bg);
    width: 1.5rem;
    height: 1.5rem;
    display: inline-grid;
    place-items: center;

    border: 1px solid var(--void-border);
    border-radius: 0.25rem;
    box-shadow: var(--input-shadow);
}

input[type="checkbox"]::before {
    content: "👍";
    font-size: 1rem;
    opacity: 0;
    transform: rotate(-90deg) scale(0.8);
    transition:
            opacity 150ms ease,
            transform 150ms ease;
}

input[type="checkbox"]:checked::before {
    opacity: 1;
    transform: rotate(0deg) scale(1);
}

select {
    border: none;
    padding: var(--unit-spacing);
    font-size: 1rem;
    background-color: var(--void-bg);
    color: rgb(var(--ink));

    outline: 1px solid var(--void-border);
    border-radius: var(--unit-spacing);
    box-shadow: var(--btn-text-shadow);
}

a {
    transition: color 0.3s ease;
}

p a {
    color: var(--primary-fg);
}

a:hover {
    /*color: rgb(var(--accent));*/
    animation: var(--glow-shadow-infinite);
}

label {
    display: inline-flex;
    align-items: start;
    gap: var(--unit-spacing);
}

img {
    height: auto;
    display: block;
}

button {
    font-family: inherit;
}


"""