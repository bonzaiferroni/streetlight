package koala.model

/** The docs of a site as nodes by id, and the root nodes in order. */
data class DocTree(
    val nodes: Map<DocId, DocNode>,
    val roots: List<DocNode>
)

/** The table of contents of the tree. */
fun DocTree.toTable(): List<DocTableItem> {
    fun DocNode.toTableItem(): DocTableItem = DocTableItem(
        docId = doc.docId,
        label = doc.title,
        children = children?.map {
            nodes.getValue(it.docId).toTableItem()
        }
    )

    return roots.map {
        it.toTableItem()
    }
}

/** Builds the children of a doc in a [docTreeOf] block. */
class DocNodeBuilder(
    val doc: Doc?
) {
    internal val children: MutableList<DocChild> = mutableListOf()
    internal val childNodes: MutableList<DocNode> = mutableListOf()

    /** Adds [child] after the previous children, with its own children added in [block]. */
    fun add(child: Doc, block: (DocNodeBuilder.() -> Unit)? = null) {
        children.add(DocChild(child, block))
    }

    internal fun buildNode(parent: DocLink?, previous: DocLink?, next: DocLink?, nodes: MutableMap<DocId, DocNode>): DocNode? {
        val docId = doc?.docId
        if (docId != null && nodes.contains(docId)) error("docId already added: $docId")
        val childLinks = mutableListOf<DocLink>()

        val docLink = doc?.let { DocLink(doc.docId, doc.title) }
        var previousLink: DocLink? = docLink
        children.forEachIndexed { index, child ->
            val builder = DocNodeBuilder(child.doc)
            val nextLink = children.getOrNull(index + 1)?.let {
                DocLink(it.doc.docId, it.doc.title)
            } ?: next
            child.block?.invoke(builder)

            val node = builder.buildNode(docLink, previousLink, nextLink, nodes) ?: error("child node was null")
            childNodes.add(node)

            val link = DocLink(child.doc.docId, child.doc.title)
            childLinks.add(link)
            previousLink = link
        }

        if (docId == null) return null
        val nextNodeLink = childLinks.firstOrNull() ?: next
        val node = DocNode(doc, parent, previous, nextNodeLink, childLinks)
        nodes[docId] = node
        return node
    }
}

internal data class DocChild(val doc: Doc, val block: (DocNodeBuilder.() -> Unit)?)

/**
 * Builds a [DocTree] from the docs added in [block], linking each to its parent, neighbors and children. Throws
 * on a repeated doc id.
 */
fun docTreeOf(block: DocNodeBuilder.() -> Unit): DocTree {
    val builder = DocNodeBuilder(null)
    val nodes: MutableMap<DocId, DocNode> = mutableMapOf()
    block(builder)
    builder.buildNode(null, null, null, nodes)
    return DocTree(nodes, builder.childNodes)
}

