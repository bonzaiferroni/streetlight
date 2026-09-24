package koala.html

import kotlinx.html.*
import kotlinx.html.span as spanTag
import kotlinx.html.em as emTag
import kotlinx.html.strong as strongTag
import koala.modifier.*
import koala.markdown.HeadingLevel

/** [content] as a paragraph, after what [block] builds. */
inline fun FlowContent.textBlock(
    content: String = "",
    mod: Modifier? = null,
    crossinline block: P.() -> Unit = { }
) {
    p {
        addModifiers(mod)
        block()
        +content
    }
}

fun FlowContent.heading1(
    content: String? = null,
    mod: Modifier? = null,
    block: H1.() -> Unit = {},
) {
    h1 {
        addModifiers(mod)
        block()
        content?.let {
            +it
        }
    }
}

fun FlowContent.heading2(
    content: String? = null,
    mod: Modifier? = null,
    block: H2.() -> Unit = {},
) {
    h2 {
        addModifiers(mod)
        block()
        content?.let {
            +it
        }
    }
}

fun FlowContent.heading3(
    content: String? = null,
    mod: Modifier? = null,
    block: H3.() -> Unit = {},
) {
    h3 {
        addModifiers(mod)
        block()
        content?.let {
            +it
        }
    }
}

fun FlowContent.heading4(
    content: String? = null,
    mod: Modifier? = null,
    block: H4.() -> Unit = {},
) {
    h4 {
        addModifiers(mod)
        block()
        content?.let {
            +it
        }
    }
}

fun FlowContent.heading5(
    content: String? = null,
    mod: Modifier? = null,
    block: H5.() -> Unit = {},
) {
    h5 {
        addModifiers(mod)
        block()
        content?.let {
            +it
        }
    }
}

fun FlowContent.heading6(
    content: String? = null,
    mod: Modifier? = null,
    block: H6.() -> Unit = {},
) {
    h6 {
        addModifiers(mod)
        block()
        content?.let {
            +it
        }
    }
}

/** [text] as a heading of [level]. */
fun FlowContent.heading(
    level: HeadingLevel,
    text: String,
    mod: Modifier? = null,
) {
    when (level) {
        HeadingLevel.H1 -> heading1(text, mod)
        HeadingLevel.H2 -> heading2(text, mod)
        HeadingLevel.H3 -> heading3(text, mod)
        HeadingLevel.H4 -> heading4(text, mod)
        HeadingLevel.H5 -> heading5(text, mod)
        HeadingLevel.H6 -> heading6(text, mod)
    }
}

fun FlowContent.p(
    mod: Modifier,
    block: P.() -> Unit,
) {
    p {
        addModifiers(mod)
        block()
    }
}

fun FlowContent.span(
    mod: Modifier? = null,
    block: SPAN.() -> Unit,
) {
    spanTag {
        addModifiers(mod)
        block()
    }
}

fun FlowContent.span(
    text: String,
    mod: Modifier? = null,
    block: SPAN.() -> Unit = {},
) {
    span(mod) {
        block()
        +text
    }
}

fun FlowContent.em(
    text: String,
    mod: Modifier? = null,
    block: EM.() -> Unit = {},
) {
    emTag {
        addModifiers(mod)
        block()
        +text
    }
}

fun FlowContent.strong(
    text: String,
    mod: Modifier? = null,
    block: STRONG.() -> Unit = {},
) {
    strongTag {
        addModifiers(mod)
        block()
        +text
    }
}