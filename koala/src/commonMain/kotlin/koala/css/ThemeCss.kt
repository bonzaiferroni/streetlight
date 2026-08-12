@file:Suppress("CssInvalidPseudoSelector")

package koala.css

// language="CSS"
val ThemeCss get() = """
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
    
    --primary: ${Koala.primary}; 
    --primary-fg: color-mix(in srgb, rgb(var(--primary)) 50%, rgb(var(--ink)));
    --primary-bg: color-mix(in srgb, rgb(var(--primary)) 75%, rgb(var(--paper)));
    --primary-button-day: color-mix(in srgb, var(--primary-button) 80%, black);
    --primary-card-bg: color-mix(in srgb, rgba(var(--primary), .2) 50%, var(--card-bg));
    
    --secondary-button: var(--gray-bg);
    
    --accent: ${Koala.accent}; /* 170, 57, 148; 255, 53, 221 */
    --accent-fg: color-mix(in srgb, rgb(var(--accent)) 50%, rgb(var(--ink)));
    --accent-bg: color-mix(in srgb, rgb(var(--accent)) 75%, rgb(var(--paper)));
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
    --valid-fg: color-mix(in srgb, rgb(var(--green)) 75%, rgb(var(--ink)));
    --success-bg : color-mix(in srgb, rgb(var(--green)) 25%, rgb(var(--paper)));
    
    --yellow: 255, 240, 32;
    --yellow-fg: color-mix(in srgb, rgb(var(--yellow)) 25%, rgb(var(--ink)));
    --required-bg: color-mix(in srgb, rgb(var(--yellow)) 33%, rgb(var(--paper)));
    --caution-fg: color-mix(in srgb, rgb(var(--yellow)) 75%, rgb(var(--ink)));
    
    --gold: 200, 178, 87;
    --lamp-fg: color-mix(in srgb, rgb(var(--gold)) 75%, rgb(var(--ink)));
    
    --purple: 158, 87, 200;
    --selection-overlay: rgba(var(--purple), .2);
    --editor-bg: color-mix(in srgb, rgba(var(--purple), .5) 50%, var(--card-bg));
    --editor-fg: color-mix(in srgb, rgb(var(--purple)) 50%, rgb(var(--ink)));
    --editor-zen-bg: color-mix(in srgb, rgba(var(--purple), .75) 50%, var(--zen-bg));
    
    --paper-gradient2-bg: linear-gradient(to right, var(--card-bg) 50%, transparent 95%);
    --paper-gradient-bg: linear-gradient(to right, rgba(var(--paper), .8) 0%, transparent 100%);
    --card-gradient-bg: linear-gradient(to right, var(--card-bg) 0%, transparent 100%);
    --ink-gradient-bg: linear-gradient(to right, rgba(var(--ink), .3) 0%, transparent 75%);

    --unit-spacing: 0.5rem;
    
    --strong-blur: blur(10px);
    --ghost-border: 2px solid rgba(var(--ink), .1);
    --color-scheme: var(--ink-fg);

    --rho-color: ${Koala.rho.color};
    --beta-color: ${Koala.beta.color};
    --gamma-color: ${Koala.gamma.color};
    --rho-position: ${Koala.rho.position};
    --beta-position: ${Koala.beta.position};
    --gamma-position: ${Koala.gamma.position};
    --rho-brightness: ${Koala.rho.brightness / 100.0};
    --beta-brightness: ${Koala.beta.brightness / 100.0};
    --gamma-brightness: ${Koala.gamma.brightness / 100.0};
}

body {
    --primary-button: rgb(var(--primary));
    --accent-button: rgb(var(--accent));
    
    --rho-bg: rgb(var(--rho-color));
    --beta-bg: rgb(var(--beta-color));
    --gamma-bg: rgb(var(--gamma-color));
}

:root$DayTheme {
    --paper: ${Koala.ink};
    --ink: ${Koala.paper * 2};
}

body::before {
    content: "";
    position: fixed;
    inset: 0;
    pointer-events: none;
    z-index: -1;
    background:
            radial-gradient(var(--rho-position), rgba(var(--rho-color), var(--rho-brightness)) 0%, transparent 50%),
            radial-gradient(var(--beta-position), rgba(var(--beta-color), var(--beta-brightness)) 0%, transparent 48%),
            radial-gradient(var(--gamma-position), rgba(var(--gamma-color), var(--gamma-brightness)) 0%, transparent 65%);
    filter: hue-rotate(0deg);
    animation: hueSpin 30s linear infinite;
    will-change: filter;
}

@keyframes hueSpin {
    to { filter: hue-rotate(360deg); }
}
"""