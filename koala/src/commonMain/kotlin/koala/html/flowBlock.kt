package koala.html

import koala.css.Class

object FlowBlockKey {
    val Class = Class("flow-block")
}

// language="CSS"
val FlowBlockCss get() = """
.flow-block.magic {
    opacity: 0;
    transition: opacity 200ms ease-in-out, transform 200ms ease-in-out, filter 200ms ease-out;
    pointer-events: none;
}

.flow-block.magic.reveal {
    opacity: 1;
    pointer-events: auto;
}

.flow-block.magic.blur {
    filter: var(--magic-blur);
}

.flow-block.magic.blur.reveal {
    filter: blur(0px);
}

.flow-block.magic.slide-left {
    transform: translate(20px, 0px);
}

.flow-block.magic.slide-left.reveal {
    transform: translate(0px, 0px);
}

.flow-block.magic.slide-up {
    transform: translate(0px, 20px);
}

.flow-block.magic.slide-up.reveal {
    transform: translate(0px, 0px);
}
"""