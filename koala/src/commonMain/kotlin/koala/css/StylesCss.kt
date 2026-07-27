@file:Suppress("CssOverwrittenProperties", "CssInvalidPseudoSelector")

package koala.css

const val CONTENT_PANEL_WIDTH_PX = 1080
const val RIGHT_PANEL_WIDTH_PX = 360
const val SIDE_PANEL_WIDTH_PX = 192

// language="CSS"
val StylesCss get() = """
:root {
    --paper: ${Koala.paper};
    --ink: ${Koala.ink};
    --white: ${Koala.ink};
    --black: ${Koala.paper};
    --white-fg: rgb(var(--white));
    --black-bg: rgb(var(--black));
    --ink-fg: rgb(var(--ink));
    --paper-bg: rgb(var(--paper));
    
    /* color-mix(in srgb, var(--paper-bg) 95%, var(--ink-fg)); */
    --body-bg: var(--paper-bg);   
    --card-bg: rgba(var(--paper), .4);
    --dialog-bg: rgba(var(--paper), .8);
    --zen-bg: rgba(var(--paper), .25);
    --tabs-bg: rgba(var(--paper), .5);
    --ink-dim: rgba(var(--ink), .8);
    --ink-disabled: color-mix(in srgb, var(--ink-fg) 50%, var(--paper-bg));
    --outline-low-fg: rgba(var(--ink), .2);
    --outline-mid-fg: rgba(var(--ink), .5);
    --outline-low: 2px solid var(--outline-low-fg);
    
    --gray: 60, 62, 64;
    --gray-fg: rgb(var(--gray));
    --gray-bg: color-mix(in srgb, var(--gray-fg) 50%, var(--paper-bg));
    
    --void-bg: color-mix(in srgb, var(--paper-bg) 89%, var(--ink-fg));
    --void-border: color-mix(in srgb, var(--void-bg) 90%, var(--ink-fg));
    
    --ink-shadow: 0 1px 2px rgba(var(--paper), 0.8), 0 0 6px rgba(var(--paper), 0.6);
    --moon-shadow: 0 0 4px 4px rgba(0, 0, 0, 0.05), 0 0 12px 12px rgba(0, 0, 0, 0.05);
    --moon-shadow-inset: 0 0 4px 4px rgba(0, 0, 0, 0.05) inset, 0 0 8px 8px rgba(0, 0, 0, 0.02) inset;
    --moon-shadow-text: 0 0 12px rgba(0, 0, 0, 0.25);
    --btn-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.8), 0 0 6px rgba(0, 0, 0, 0.6);
    --zen-button-shadow: 0 0 20px 5px rgba(0, 0, 0, 0.1) inset;
    --vignette-shadow: 0 0 100px rgba(0, 0, 0, 0.4) inset;
    --input-shadow: 0 1px 2px rgba(0, 0, 0, 0.4), 0 0 6px rgba(0, 0, 0, 0.3);
    --neumo-shadow: 6px 6px 12px 0 rgba(0, 0, 0, 0.15), 2px 2px 4px 0 rgba(0, 0, 0, 0.1), 
        -6px -6px 12px 0 rgba(255, 255, 255, 0.7), -2px -2px 4px 0 rgba(255, 255, 255, 0.5);
            
    --vignette-gradient: radial-gradient(ellipse at center, transparent 50%, rgba(0, 0, 0, 0.3) 100%);
    
    --primary: 58, 158, 200; /* 48, 138, 170; 70, 117, 153; 45, 199, 255*/
    --primary-fg: color-mix(in srgb, rgb(var(--primary)) 50%, rgb(var(--ink)));
    --primary-bg: color-mix(in srgb, rgb(var(--primary)) 75%, rgb(var(--paper)));
    --primary-button: rgb(var(--primary));
    --primary-button-day: color-mix(in srgb, var(--primary-button) 80%, black);
    --primary-card-bg: color-mix(in srgb, rgba(var(--primary), .2) 50%, var(--card-bg));
    
    --secondary-button: var(--gray-bg);
    
    --accent: 200, 87, 178; /* 170, 57, 148; 255, 53, 221 */
    --accent-fg: color-mix(in srgb, rgb(var(--accent)) 50%, rgb(var(--ink)));
    --accent-bg: color-mix(in srgb, rgb(var(--accent)) 75%, rgb(var(--paper)));
    --accent-button: rgb(var(--accent));
    --accent-button-day: color-mix(in srgb, var(--accent-button) 80%, black);
    
    --red: 255, 99, 132;
    --red-fg: color-mix(in srgb, rgb(var(--red)) 75%, rgb(var(--ink)));
    --danger-fg: color-mix(in srgb, rgb(var(--red)) 75%, rgb(var(--ink)));
    --danger-bg: color-mix(in srgb, rgb(var(--red)) 75%, rgb(var(--paper)));
    --error-bg: color-mix(in srgb, rgb(var(--red)) 25%, rgb(var(--paper)));
    --error-fg: color-mix(in srgb, rgb(var(--red)) 75%, rgb(var(--ink)));
    
    --sea-green: 58, 200, 158; 
    --sea-green-fg: color-mix(in srgb, rgb(var(--sea-green)) 50%, rgb(var(--ink)));
    
    --green: 99, 255, 132;
    --green-fg: color-mix(in srgb, rgb(var(--green)) 25%, rgb(var(--ink)));
    --valid-bg: color-mix(in srgb, rgb(var(--green)) 33%, rgb(var(--paper)));
    --success-bg : color-mix(in srgb, rgb(var(--green)) 25%, rgb(var(--paper)));
    
    --yellow: 255, 240, 32;
    --yellow-fg: color-mix(in srgb, rgb(var(--yellow)) 25%, rgb(var(--ink)));
    --required-bg: color-mix(in srgb, rgb(var(--yellow)) 33%, rgb(var(--paper)));
    
    --gold: 200, 178, 87;
    --lamp-fg: color-mix(in srgb, rgb(var(--gold)) 75%, rgb(var(--ink)));
    
    --paper-gradient2-bg: linear-gradient(to right, var(--card-bg) 50%, transparent 95%);
    --paper-gradient-bg: linear-gradient(to right, rgba(var(--paper), .8) 0%, transparent 100%);
    --card-gradient-bg: linear-gradient(to right, var(--card-bg) 0%, transparent 100%);
    --ink-gradient-bg: linear-gradient(to right, rgba(var(--ink), .3) 0%, transparent 75%);
    
    --light-1: 255, 99, 132;
    --light-2: 88, 164, 255;
    --light-3: 88, 255, 188;
    --unit-spacing: 0.5rem;
    
    --content-panel-width: ${CONTENT_PANEL_WIDTH_PX}px;
    --right-panel-width: ${RIGHT_PANEL_WIDTH_PX}px;

    --font-family: "Fira Sans", system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; /*  */

    --breakpoint-sm: 0px;
    --breakpoint-md: 600px;
    --breakpoint-lg: 760px;
    --breakpoint-vlg: 1000px;

    --strong-blur: blur(10px);
    --ghost-border: 2px solid rgba(var(--ink), .1);
    --color-scheme: var(--ink-fg);
}

:root$DayTheme {
    --paper: ${Koala.ink};
    --ink: ${Koala.paper * 2};
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
    background-color: var(--body-bg);
    color: var(--ink-fg);
    -webkit-font-smoothing: antialiased;
    transition: 
        background-color var(--magic-interval) var(--magic-easing), 
        color var(--magic-interval) var(--magic-easing);
}

body::before {
    content: "";
    position: fixed;
    inset: 0;
    pointer-events: none;
    z-index: -1;
    background:
            radial-gradient(circle at 18% 22%, rgba(var(--light-1), 0.5) 0%, transparent 50%),
            radial-gradient(circle at 82% 20%, rgba(var(--light-2), 0.5) 0%, transparent 48%),
            radial-gradient(circle at 50% 65%, rgba(var(--light-3), 0.4) 0%, transparent 65%);
    filter: hue-rotate(0deg);
    animation: hueSpin 30s linear infinite;
    will-change: filter;
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