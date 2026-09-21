package koala.html

import koala.modifier.*

object ItemsBlockStyle {
    val Class = Class("items-block")
}

// language="CSS"
val ItemsBlockCss get() = with(ItemsBlockStyle) { """
    
$Class {
    position: relative;
}
    
$Class > * {
    width: 100%;
    position: absolute;
}    

$Class$Magic {
    transition: height 200ms ease-in-out;
    
    > :not($Reveal) {
        pointer-events: none;
    }
}

$Class$Magic > * {
    position: absolute;
    opacity: 0;
    transition: opacity 200ms ease-in-out, transform 200ms ease-in-out, filter 200ms ease-in-out, top 200ms ease-in-out;
}

$Class$Magic > $Reveal {
    opacity: 1;
}

$Class$Magic$Blur > * {
    filter: var(--magic-blur);
}

$Class$Magic > $Reveal {
    filter: blur(0px);
}

$Class$Magic$SlideLeft > * {
    transform: translate(20px, 0px);
}

$Class$Magic$SlideLeft > $Reveal {
    transform: translate(0px, 0px);
}

$Class$Magic$SlideUp > * {
    transform: translate(0px, 20px);
}

$Class$Magic$SlideUp > $Reveal {
    transform: translate(0px, 0px);
}
""" }