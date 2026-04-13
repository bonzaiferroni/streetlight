package koala.html

import koala.css.Class

object FlowBlockKey {
    val Class = Class("flow-block")
}

// language="CSS"
val FlowBlockCss get() = """
.flow-block.magic {
    opacity: 0;
    transition: 
        opacity var(--magic-interval) var(--magic-easing), 
        transform var(--magic-interval) var(--magic-easing), 
        filter var(--magic-interval) var(--magic-easing);
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
    transform: var(--slide-left-initial);
}

.flow-block.magic.slide-left.reveal {
    transform: translate(0px, 0px);
}

.flow-block.magic.slide-up {
    transform: var(--slide-up-initial);
}

.flow-block.magic.slide-up.reveal {
    transform: translate(0px, 0px);
}

"""