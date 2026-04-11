package koala.html

import koala.css.Class
import koala.css.Reveal

object DialogKey {
    val Class = Class("dialog")
}

// language="CSS"
val DialogCss get() = """
${DialogKey.Class} {
    position: fixed;
    inset: 0;

    border-radius: var(--unit-spacing);
    box-shadow: var(--btn-text-shadow);
    background-color: var(--primary-bg);
    margin: auto var(--unit-spacing);
    padding: var(--unit-spacing-2) 0;
    color: rgb(var(--ink));
    border: none;
    outline: none;
    max-width: var(--body-width);
    overflow-x: hidden;

    opacity: 0;
    transform: translateY(10px);

    transition:
            opacity 200ms ease-in-out,
            transform 200ms ease-in-out;
}

${DialogKey.Class}[open]$Reveal {
    opacity: 1;
    transform: translateY(0);
    display: flex;
    align-items: center;
    justify-content: center;
}

${DialogKey.Class}::backdrop {
    backdrop-filter: blur(0px);
    -webkit-backdrop-filter: blur(0px);
}

${DialogKey.Class}[open]$Reveal::backdrop {
    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
}
"""