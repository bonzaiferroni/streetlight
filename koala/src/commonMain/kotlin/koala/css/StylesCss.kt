@file:Suppress("CssOverwrittenProperties", "CssInvalidPseudoSelector")

package koala.css

const val BODY_WIDTH_PX = 960

// language="CSS"
val StylesCss get() = """
:root {
    --paper: ${Koala.paper};
    --ink: ${Koala.ink};
    --ink-fg: rgb(var(--ink));
    --paper-bg: rgb(var(--paper));
    
    /* color-mix(in srgb, var(--paper-bg) 95%, var(--ink-fg)); */
    --body-bg: var(--paper-bg);   
    --card-bg: rgba(var(--paper), .4);
    --zen-card-bg: rgba(var(--paper), .2);
    --tabs-bg: rgba(var(--paper), .5);
    --ink-dim: rgba(var(--ink), .8);
    --ink-disabled: color-mix(in srgb, var(--ink-fg) 50%, var(--paper-bg));
    --weak-outline: rgba(var(--ink), .2);
    
    --gray: 60, 62, 64;
    --gray-fg: rgb(var(--gray));
    --gray-bg: color-mix(in srgb, var(--gray-fg) 50%, var(--paper-bg));
    
    --void-bg: color-mix(in srgb, var(--paper-bg) 89%, var(--ink-fg));
    --void-border: color-mix(in srgb, var(--void-bg) 90%, var(--ink-fg));
    
    --ink-shadow: 0 1px 2px rgba(var(--paper), 0.8), 0 0 6px rgba(var(--paper), 0.6);
    --moon-shadow: 0 0 4px 4px rgba(0, 0, 0, 0.05), 0 0 12px 12px rgba(0, 0, 0, 0.05);
    --moon-shadow-text: 0 0 12px rgba(0, 0, 0, 0.25);
    --btn-text-shadow: 0 1px 2px rgba(0, 0, 0, 0.8), 0 0 6px rgba(0, 0, 0, 0.6);
    --input-shadow: 0 1px 2px rgba(0, 0, 0, 0.4), 0 0 6px rgba(0, 0, 0, 0.3);
    
    --primary: 58, 158, 200; /* 48, 138, 170; 70, 117, 153; 45, 199, 255*/
    --primary-button: rgb(var(--primary));
    --primary-bg: color-mix(in srgb, var(--primary-fg) 15%, black);
    --primary-button-day: color-mix(in srgb, var(--primary-button) 80%, black);
    --primary-card-bg: color-mix(in srgb, rgba(var(--primary), .2) 60%, black);
    
    --secondary-button: var(--gray-bg);
    
    --accent-bg: 209, 43, 181;
    --accent: 200, 87, 178; /* 170, 57, 148; 255, 53, 221 */
    --accent-button: rgb(var(--accent));
    --accent-button-day: color-mix(in srgb, var(--accent-button) 80%, black);
    
    --danger: 255, 99, 132;
    --danger-bg: rgb(var(--danger)); /* not evaluated */
    --light-1: 255, 99, 132;
    --light-2: 88, 164, 255;
    --light-3: 88, 255, 188;
    --unit-spacing: 0.5rem;

    --body-width: ${BODY_WIDTH_PX}px;

    --font-family: "Fira Sans", system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; /*  */

    --breakpoint-sm: 0px;
    --breakpoint-md: 600px;
    --breakpoint-lg: 768px;
    --breakpoint-vlg: 1024px;

    --strong-blur: blur(10px);
}

:root$DayTheme {
    --paper: ${Koala.ink};
    --ink: ${Koala.paper * 2};
}


html {
    font-size: 100%;
}

/*@media (max-width: 600px) {*/
/*    html {*/
/*        font-size: 90%;*/
/*    }*/
/*}*/

body {
    font-family: var(--font-family);
    font-size: 1rem;
    line-height: 1.5;
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

input[type="text"],
input[type="password"],
input[type="datetime-local"],
input[type="time"],
input[type="date"],
textarea {
    font-family: var(--font-family);
    outline: none;
    padding: var(--unit-spacing);
    font-size: 1rem;
    background-color: var(--void-bg);
    color: rgb(var(--ink));
    line-height: 1.5rem;

    border: 1px solid var(--void-border);
    border-radius: var(--unit-spacing);
    box-shadow: var(--input-shadow);
}

input, button, textarea, select {
    font: inherit;
    color: white;
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

@keyframes hueSpin {
    to { filter: hue-rotate(360deg); }
}

a {
    transition: color 0.3s ease;
}

p a {
    color: rgb(var(--primary));
}

a:hover {
    /*color: rgb(var(--accent));*/
    animation: glow-shadow 10s infinite linear;
}

label {
    display: inline-flex;
    align-items: center;
    gap: var(--unit-spacing);
}

img {
    height: auto;
    display: block;
}
"""