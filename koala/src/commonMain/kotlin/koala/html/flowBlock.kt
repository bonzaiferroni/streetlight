package koala.html

import koala.css.*

object FlowBlockKey {
    val Class = Class("flow-block")
}

// language="CSS"
val FlowBlockCss get() = with(FlowBlockKey) { """
$Class$Transitioning {
    opacity: 0;
    transition: var(--transition-opacity);
    pointer-events: none;

    &$Reveal {
        opacity: 1;
        pointer-events: auto;
    }
    
    &$SlideDown, &$SlideUp, &$SlideRight, &$SlideLeft {
        transition: var(--transition-opacity), var(--transition-transform);
        
        &$Blur {
            transition: var(--transition-opacity), var(--transition-transform), var(--transition-filter);
        }
        
        &$Reveal {
            transform: translate(0px, 0px);
        }
    }
    
    &$SlideUp    { transform: var(--slide-up-initial); }
    &$SlideDown  { transform: var(--slide-down-initial); }
    &$SlideLeft  { transform: var(--slide-left-initial); }
    &$SlideRight { transform: var(--slide-right-initial); }
    
    &$Scale {
        transition: var(--transition-opacity), var(--transition-transform-bounce);
        transform: scale(${MagicStyle.InitialScale});
        
        &$Reveal {
            transform: scale(1);            
        }
    }
    
    &$Blur {
        transition: var(--transition-opacity), var(--transition-filter);
        filter: var(--magic-blur);
    
        &$Reveal {
            filter: blur(0px);
        }
    }
}

"""}