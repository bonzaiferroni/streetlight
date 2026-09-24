package koala.model

import kampfire.api.Markdown
import kampfire.model.Url
import koala.Image
import koala.html.Id
import kotlinx.serialization.Serializable
import kotlin.String
import kotlin.collections.List

/** A document of the site, in [sections], with an optional image and links. */
@Serializable
data class Doc(
    val docId: DocId,
    val title: String,
    val sections: List<DocSection>,
    val image: Image? = null,
    val links: List<DocLink>? = null,
) {
    constructor(
        docId: DocId,
        title: String,
        content: Markdown,
        image: Image? = null,
        links: List<DocLink>? = null
    ): this(docId, title, listOf(DocSection(null, content)), image, links)
}

typealias DocId = String
/** The table of contents of a doc tree. */
typealias DocTable = List<DocTableItem>

/** A [Doc] with links to its parent, its neighbors in reading order, and its children. */
@Serializable
data class DocNode(
    val doc: Doc,
    val parent: DocLink?,
    val previous: DocLink?,
    val next: DocLink?,
    val children: List<DocLink>?,
)

/** A link to a [Doc], by id and title. */
@Serializable
data class DocLink(
    val docId: DocId,
    val label: String,
)

/** A section of a [Doc], with an optional title that also gives its [id]. */
@Serializable
data class DocSection(
    val title: String?,
    val content: Markdown,
    val id: Id? = title?.toElementId()
)

/** An entry of a [DocTable], with its children. */
@Serializable
data class DocTableItem(
    val docId: DocId,
    val label: String,
    val children: DocTable?
)

/** An [Id] made from the text: lowercase letters and digits, with spaces as hyphens. */
fun String.toElementId() = Id(buildString {
    for (c in this@toElementId) when {
        c.isLetterOrDigit() -> append(c.lowercaseChar())
        c == ' ' -> append('-')
    }
})