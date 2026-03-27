package koala.css

// language="CSS"
val ResetCss get() = """
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

a {
    color: inherit;
    text-decoration: none;
}
a:hover {
    text-decoration: none;
}

ul, ol {
    padding: 0;
    margin: 0;
}

img, picture, video, canvas, svg {
    display: block;
    max-width: 100%;
}

input, button, textarea, select {
    font: inherit;
}

@media (prefers-reduced-motion: no-preference) {
    html {
        interpolate-size: allow-keywords;
    }
}
"""