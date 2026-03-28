package koala.css

// language="CSS"
val StylesCss get() = """
:root {
    --paper: 9, 13, 13;
    --paper-bg: rgb(var(--paper));
    --body-bg: color-mix(in srgb, var(--paper-bg) 95%, white);
    --card-bg: rgba(var(--paper), .4);
    --tabs-bg: rgba(var(--paper), .5);
    --ink: 240, 246, 246;
    --white-fg: rgb(var(--ink));
    --white-dim: rgba(var(--ink), .8);
    --white-disabled: color-mix(in srgb, var(--white-fg) 50%, black);
    --weak-outline: rgba(var(--ink), .2);
    --gray: 60, 62, 64;
    --gray-fg: rgb(var(--gray));
    --gray-bg: color-mix(in srgb, var(--gray-fg) 50%, black);
    --void: 34, 41, 41;
    --void-bg: rgb(var(--void));
    --void-border: color-mix(in srgb, var(--void-bg) 90%, white);
    --shadow: rgba(0, 0, 0, 0.35);
    --primary: 45, 199, 255; /* 70, 117, 153; */
    --primary-fg: rgb(var(--primary));
    --primary-button: rgba(var(--primary), .5);
    --primary-bg: color-mix(in srgb, var(--primary-fg) 15%, black);
    --primary-card-bg: color-mix(in srgb, rgba(var(--primary), .2) 80%, black);
    --secondary-button: var(--gray-fg);
    --accent-bg: 209, 43, 181;
    --accent: 255, 53, 221;
    --accent-button: rgba(var(--accent), .5);
    --danger: 255, 99, 132;
    --danger-bg: rgb(var(--danger)); /* not evaluated */
    --light-1: 255, 99, 132;
    --light-2: 88, 164, 255;
    --light-3: 88, 255, 188;
    --unit-spacing: 0.5rem;

    --body-width: 960px;

    --font-family: "Fira Sans", system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; /*  */

    --breakpoint-sm: 0px;
    --breakpoint-md: 600px;
    --breakpoint-lg: 768px;
    --breakpoint-vlg: 1024px;
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
    color: var(--white-fg);
    -webkit-font-smoothing: antialiased;
}

body::before {
    content: "";
    position: fixed;
    inset: 0;
    pointer-events: none;
    z-index: -1;
    background:
            radial-gradient(circle at 18% 22%, rgba(var(--light-1), 0.4) 0%, transparent 50%),
            radial-gradient(circle at 82% 20%, rgba(var(--light-2), 0.4) 0%, transparent 48%),
            radial-gradient(circle at 50% 65%, rgba(var(--light-3), 0.3) 0%, transparent 65%);
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
    background-color: rgb(var(--void));
    color: rgb(var(--ink));
    line-height: 1.5rem;

    border: 1px solid var(--void-border);
    border-radius: var(--unit-spacing);
    box-shadow: 0 0 var(--unit-spacing) var(--shadow);
}

input[type="checkbox"] {
    appearance: none;
    -webkit-appearance: none;

    background-color: rgb(var(--void));
    width: 1.5rem;
    height: 1.5rem;
    display: inline-grid;
    place-items: center;

    border: 1px solid var(--void-border);
    border-radius: 0.25rem;
    box-shadow: 0 0 0.25rem var(--shadow);
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
    background-color: rgb(var(--void));
    color: rgb(var(--ink));

    outline: 1px solid var(--void-border);
    border-radius: var(--unit-spacing);
    box-shadow: 0 0 var(--unit-spacing) var(--shadow);
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