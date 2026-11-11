package koala.component

import koala.modifier.*
import kotlinx.css.LinearDimension

object SwitchMenuStyle {
    val Class = Class("switch-menu")
    val Option = Class("switch-menu__option")
    val Label = Class("switch-menu__label")
    val Light = Class("switch-menu__light")
    val OptionWidth = Property<LinearDimension>("option-width", true)
    const val LabelTransition = "switch-menu-label"
}

// language="CSS"
val SwitchMenuCss get() = with(SwitchMenuStyle) { """
/* options on a row share its width, each at least as wide as the widest option */
$Class {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(min(var($OptionWidth, 8rem), 100%), 1fr));
    /* the lighted option's pill fills its corner */
    border-radius: var(--btn-border-radius);
    background: var(--gray-bg);
}

$Option {
    position: relative;
    isolation: isolate;
    padding: var(--btn-padding);
    border: none;
    background: none;
    color: rgb(255 255 255 / .9);
    text-shadow: var(--btn-text-shadow);
    font-size: var(--btn-font-size);
    font-weight: var(--btn-font-weight);
    line-height: 1rem;
    text-transform: uppercase;
    white-space: nowrap;
    cursor: pointer;
    user-select: none;
    -webkit-tap-highlight-color: transparent;

    &[aria-checked="true"] {
        color: white;
        cursor: default;
    }

    &:focus-visible {
        outline: 2px solid color-mix(in srgb, var(--primary-button) 70%, white);
        outline-offset: 2px;
    }
}

/* the light behind the selected option, moved between options and paired with itself in a view transition */
$Light {
    position: absolute;
    inset: 0;
    z-index: -1;
    border-radius: var(--btn-border-radius);
    background: var(--primary-button);
    box-shadow: var(--primary-shadow);
    view-transition-name: match-element;
}

/* labels paired with themselves, drawn over the light while it travels */
$Label {
    view-transition-name: match-element;
    view-transition-class: $LabelTransition;
}

::view-transition-group(*.$LabelTransition) { z-index: 1; }

/* the day light matches the day button: darker, without the glow */
$DayTheme $Light {
    background: var(--primary-button-day);
    box-shadow: var(--btn-outline);
}
""" }
