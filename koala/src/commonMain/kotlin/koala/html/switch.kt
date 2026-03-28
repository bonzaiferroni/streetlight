package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.css.modify
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.span

fun FlowContent.switch(
    label: String,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    initialOn: Boolean = false,
    block: (DIV.() -> Unit)? = null,
) {
    div {
        configureSwitch(label, modifiers, id, initialOn, block)
    }
}

fun DIV.configureSwitch(
    label: String,
    modifiers: ModifierSet? = null,
    id: Id? = null,
    initialOn: Boolean = false,
    block: (DIV.() -> Unit)? = null,
) {
    addModifiers(modify(SwitchKey.Class, modifiers))
    setId(id)
    attributes["role"] = "switch"
    attributes["aria-checked"] = initialOn.toString()
    setAttribute(TagAttribute.isOn, initialOn)

    // ghost text defines the inner pill width; outer padding makes the “constraints” larger
    span("switch__ghost") { +label }
    span("switch__pill") { +label }

    block?.invoke(this)
}

object SwitchKey {
    val Class = Css("switch")
}

// language="CSS"
val SwitchCss get() = """
.switch {
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

.switch__pill {
    position: absolute;
    top: 50%;
    transform: translate(0, -50%);
    transition:
            transform var(--magic-interval) var(--magic-easing),
            background var(--magic-interval) var(--magic-easing);

    padding: var(--btn-padding);
    border: none;
    border-radius: 2rem;
    background: var(--gray-bg);
    color: var(--ink);
    font-size: var(--btn-font-size);
    font-weight: var(--btn-font-weight);
    line-height: 1rem;
    text-transform: uppercase;
    text-shadow: var(--btn-text-shadow);
    white-space: nowrap;
}

/* sizing helper: invisible text to define track width */
.switch__ghost {
    visibility: hidden;
    pointer-events: none;

    padding: var(--btn-padding);
    font-size: var(--btn-font-size);
    line-height: 1rem;
    text-transform: uppercase;
    white-space: nowrap;
}

/* ON state: slide pill to the right, full opacity */
.switch[data-is-on="true"] .switch__pill {
    background: rgba(var(--primary), .6);
    transform: translate(1rem, -50%);
    box-shadow: 0 0 .8rem var(--primary-button);
}

/* optional: keyboard focus.svg ring if you add tabindex */
.switch:focus-visible {
    outline: 2px solid color-mix(in srgb, var(--primary-button) 70%, white);
    outline-offset: 2px;
}
"""