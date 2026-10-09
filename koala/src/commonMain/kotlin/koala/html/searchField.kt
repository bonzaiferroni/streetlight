package koala.html

import koala.SvgFile
import koala.modifier.*
import kotlinx.html.*

/**
 * A text input for a search: a pill holding a search icon before its text, showing [placeholder] while empty.
 *
 * The input grows with its text from a default minimum width to a default maximum, which [textMod] may override.
 */
fun FlowContent.searchField(
    mod: Modifier? = null,
    textMod: Modifier? = null,
    placeholder: String = "search",
    block: (INPUT.() -> Unit)? = null
) {
    div {
        configureSearchFieldContainer(mod)
        input {
            configureSearchFieldInput(textMod, placeholder, null)
            block?.invoke(this)
        }
    }
}

/** Configures this element as the pill of a [searchField], holding its icon. The input is added after. */
fun DIV.configureSearchFieldContainer(mod: Modifier?) {
    addModifiers(SearchField.Container, DisplayFlex, AlignItemsCenter, Gap(1), BorderRadiusPill, PaddingX2, PaddingY1, mod)
    icon(SvgFile.Search)
}

/** Configures this element as the input of a [searchField]. */
fun INPUT.configureSearchFieldInput(textMod: Modifier?, placeholder: String, initialValue: String?) {
    type = InputType.search
    addModifiers(textMod)
    attributes["aria-label"] = placeholder
    this.placeholder = placeholder
    value = initialValue ?: ""
}

object SearchField {
    val Container = Class("search-field")
}

// language="CSS"
val SearchFieldCss get() = with(SearchField) { """
$Container {
    color: var(--ink-fg);
    background: var(--zen-bg);
    outline: var(--outline-low);
    outline-offset: -2px;
    transition: var(--transition-outline-color);

    &:focus-within { outline-color: rgb(var(--primary)); }

    > input {
        padding: 0;
        border: none;
        outline: none;
        background: none;
        color: inherit;
        font: inherit;
    }
}

/* the default range of the input's width, at no specificity so a modifier overrides it */
:where($Container) > input {
    field-sizing: content;
    min-width: 8ch;
    max-width: 24ch;

    @supports not (field-sizing: content) { width: 16ch; }
}

$DayTheme $Container { background: var(--zen-button-day); }
""" }
