package koala.html

import koala.css.Class

object FlowBlockKey {
    val Class = Class("flow-block")
}

// language="CSS"
val FlowBlockCss get() = """
.flow-block.transitioning {
    opacity: 0;
    transition: var(--transition-opacity);
    pointer-events: none;
    
    &.blur {
        transition: var(--transition-opacity), var(--transition-filter);
    }
    
    &.slide-down,
    &.slide-up,
    &.slide-right,
    &.slide-left {
        transition: var(--transition-opacity), var(--transition-transform);
        
        &.blur {
            transition: var(--transition-opacity), var(--transition-transform), var(--transition-filter);
        }
    }
}

.flow-block.transitioning.reveal {
    opacity: 1;
    pointer-events: auto;
}

.flow-block.transitioning.blur {
    filter: var(--magic-blur);
}

.flow-block.transitioning.blur.reveal {
    filter: blur(0px);
}

.flow-block.transitioning.slide-left {
    transform: var(--slide-left-initial);
}

.flow-block.transitioning.slide-left.reveal {
    transform: translate(0px, 0px);
}

.flow-block.transitioning.slide-up {
    transform: var(--slide-up-initial);
}

.flow-block.transitioning.slide-up.reveal {
    transform: translate(0px, 0px);
}

.flow-block.transitioning.slide-right {
    transform: var(--slide-right-initial);
}

.flow-block.transitioning.slide-right.reveal {
    transform: translate(0px, 0px);
}

"""