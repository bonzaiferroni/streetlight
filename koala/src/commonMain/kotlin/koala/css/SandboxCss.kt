package koala.css

import koala.html.Attribute

// language="CSS"
val SandboxCss get() = """
.fullscreen-box {
    position: fixed;
    inset: 0;
    margin: 0;
    padding: 0;
    width: 100vw;
    height: 100vh;
    max-width: none;
    max-height: none;
    border: none;
    border-radius: 0;

    box-sizing: border-box;
    background: var(--paper-bg);
    color: rgb(var(--ink));
}
"""