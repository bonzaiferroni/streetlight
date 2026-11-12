@file:Suppress("RegExpRedundantEscape") // necessary for regex in js context

package koala.markdown

import koala.markdown.ContentBlock.*

/** The kinds of markdown block. */
enum class ContentBlock {
    Image,
    Paragraph,
    Heading,
    HorizontalRule,
    Code,
    BlockQuote,
    UnorderedList,
    OrderedList,
    Table,
}



