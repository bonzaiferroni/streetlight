package koala.html

import koala.css.Class
import koala.css.Reveal

object DialogStyle {
    val Class = Class("dialog")
    val Card = Class("dialog-card")
    val Content = Class("dialog-content")
}

// language="CSS"
val DialogCss get() = with(DialogStyle) { """
$Class {
    position: fixed;
    inset: 0;

    margin: var(--unit-spacing-8) auto;
    color: rgb(var(--ink));
    border: none;
    outline: none;
    width: min(calc(100% - var(--unit-spacing-2)), var(--body-width));
    background-color: transparent;
    scrollbar-width: none;
    
    $Card, .tabs-header {
        border: 2px solid var(--weak-outline);
    }
}

$Content, $Content > * {
    max-height: 80vh;
}

$Class[open] {
    display: flex;
    align-items: center;
    justify-content: center;
    
    opacity: 0;
    transform: translateY(10px);
    transition: var(--transition-opacity), var(--transition-transform);
    
    &$Reveal {
        opacity: 1;
        transform: translateY(0);
    }
}

$Class::backdrop {
    backdrop-filter: blur(0px);
    -webkit-backdrop-filter: blur(0px);
}

$Class[open]::backdrop {
    backdrop-filter: blur(0px);
    -webkit-backdrop-filter: blur(0px);
    
    background-color: rgba(var(--ink), 0);
    transition: var(--transition-background-color);
}

$Class[open]$Reveal::backdrop {
    background-color: rgba(var(--white), .1);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
}

$Card {
    border-radius: var(--unit-spacing-2);
    background-color: var(--dialog-bg);
    padding: var(--unit-spacing-2);
    overflow-y: auto;
    max-height: 70vh;
}
""" }