package koala.model

import kampfire.model.Labeled

/** The levels of a heading. */
enum class HeadingLevel: Labeled {
    H1,
    H2,
    H3,
    H4,
    H5,
    H6;

    override val label get() = name
}
