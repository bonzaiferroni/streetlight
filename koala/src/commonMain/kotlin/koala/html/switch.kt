package koala.html

import koala.css.Class
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.css.modify
import kotlinx.html.DIV
import kotlinx.html.span

object SwitchStyle {
    val Class = Class("switch")
    val Pill = Class("switch__pill")
    val Ghost = Class("switch__ghost")
}

// language="CSS"
val SwitchCss get() = with(SwitchStyle) { """
$Class {
    position: relative;
    display: inline-flex;
    align-items: center;

    padding-right: 1rem;
    border-radius: 2rem;

    background: color-mix(in srgb, var(--primary-button) 25%, transparent);
    /*box-shadow: 0 0 .8rem color-mix(in srgb, var(--primary-button) 35%, transparent);*/

    cursor: pointer;
    user-select: none;
    -webkit-tap-highlight-color: transparent;
}

$Pill {
    position: absolute;
    top: 50%;
    left: 0;
    right: 1rem;
    transform: translate(0, -50%);
    transition:
            transform var(--magic-interval) var(--magic-easing),
            background var(--magic-interval) var(--magic-easing);

    padding: var(--btn-padding);
    border: none;
    border-radius: 2rem;
    background: var(--gray-bg);
    color: white;
    font-size: var(--btn-font-size);
    font-weight: var(--btn-font-weight);
    line-height: 1rem;
    text-transform: uppercase;
    text-shadow: var(--btn-text-shadow);
    text-align: center;
    white-space: nowrap;
}

/* sizing helper: invisible text to define track width */
$Ghost {
    visibility: hidden;
    pointer-events: none;

    padding: var(--btn-padding);
    font-size: var(--btn-font-size);
    line-height: 1rem;
    text-transform: uppercase;
    white-space: nowrap;
}

/* ON state: slide pill to the right, full opacity */
$Class[data-is-on="true"] $Pill {
    background: var(--primary-button);
    transform: translate(1rem, -50%);
    box-shadow: 0 0 .8rem var(--primary-button), var(--btn-outline);
}

/* optional: keyboard focus.svg ring if you add tabindex */
$Class:focus-visible {
    outline: 2px solid color-mix(in srgb, var(--primary-button) 70%, white);
    outline-offset: 2px;
}
""" }