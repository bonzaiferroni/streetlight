@file:Suppress("RegExpRedundantEscape") // necessary for regex in js context

package koala.markdown

import kampfire.model.Labeled
import koala.markdown.ContentBlock.*

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

enum class HeadingLevel: Labeled {
    H1,
    H2,
    H3,
    H4,
    H5,
    H6;

    override val label get() = name
}


