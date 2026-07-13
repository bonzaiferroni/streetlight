package koala.model

import kampfire.api.Markdown
import kampfire.model.Url
import koala.Image
import koala.html.Id
import kotlinx.serialization.Serializable
import kotlin.String
import kotlin.collections.List

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
typealias DocTable = List<DocTableItem>

@Serializable
data class DocNode(
    val doc: Doc,
    val parent: DocLink?,
    val previous: DocLink?,
    val next: DocLink?,
    val children: List<DocLink>?,
)

@Serializable
data class DocLink(
    val docId: DocId,
    val label: String,
)

@Serializable
data class DocSection(
    val title: String?,
    val content: Markdown,
    val id: Id? = title?.toElementId()
)

@Serializable
data class DocTableItem(
    val docId: DocId,
    val label: String,
    val children: DocTable?
)

fun String.toElementId() = Id(buildString {
    for (c in this@toElementId) when {
        c.isLetterOrDigit() -> append(c.lowercaseChar())
        c == ' ' -> append('-')
    }
})