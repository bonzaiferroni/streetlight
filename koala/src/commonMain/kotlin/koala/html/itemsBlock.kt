package koala.html

import koala.css.Css

object ItemsBlockKey {
    val Class = Css("items-block")
}

// language="CSS"
val ItemsBlockCss get() = """
.items-block.magic {
    transition: height 200ms ease-in-out;
}

.items-block.magic > * {
    position: absolute;
    opacity: 0;
    transition: opacity 200ms ease-in-out, transform 200ms ease-in-out, filter 200ms ease-in-out, top 200ms ease-in-out;
    pointer-events: none;
}

.items-block.magic > .reveal {
    opacity: 1;
    pointer-events: auto;
}

.items-block.magic.blur > * {
    filter: var(--magic-blur);
}

.items-block.magic > .reveal {
    filter: blur(0px);
}

.items-block.magic.slide-left > * {
    transform: translate(20px, 0px);
}

.items-block.magic.slide-left > .reveal {
    transform: translate(0px, 0px);
}

.items-block.magic.slide-up > * {
    transform: translate(0px, 20px);
}

.items-block.magic.slide-up > .reveal {
    transform: translate(0px, 0px);
}
"""