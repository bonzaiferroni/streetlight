package koala.model

import kotlinx.serialization.Serializable
import kotlin.String
import kotlin.collections.List

@Serializable
data class Doc(
    val docId: DocId,
    val title: String,
    val sections: List<DocSection>,
    val links: List<DocLink>? = null,
) {
    constructor(docId: DocId, title: String, content: String, links: List<DocLink>? = null):
            this(docId, title, listOf(DocSection(null, content)), links)
}

typealias DocId = String

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
    val content: String
)