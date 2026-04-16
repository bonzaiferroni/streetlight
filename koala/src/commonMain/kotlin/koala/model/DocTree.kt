package koala.model

data class DocTree(
    val nodes: Map<DocId, DocNode>,
    val roots: List<DocNode>
)

class DocNodeBuilder(
    val doc: Doc?
) {
    private val children: MutableList<DocChild> = mutableListOf()
    internal val childNodes: MutableList<DocNode> = mutableListOf()

    fun add(child: Doc, block: (DocNodeBuilder.() -> Unit)? = null) {
        children.add(DocChild(child, block))
    }

    internal fun buildNode(parent: DocLink?, previous: DocLink?, next: DocLink?, nodes: MutableMap<DocId, DocNode>): DocNode? {
        val docId = doc?.docId
        if (docId != null && nodes.contains(docId)) error("docId already added: $docId")
        val childLinks = mutableListOf<DocLink>()

        val docLink = doc?.let { DocLink(doc.docId, doc.title) }
        var previousChild: DocLink? = null
        children.forEachIndexed { index, child ->
            val builder = DocNodeBuilder(child.doc)
            val nextChild = children.getOrNull(index + 1)?.let {
                DocLink(it.doc.docId, it.doc.title)
            }
            child.block?.invoke(builder)
            builder.buildNode(docLink, previousChild, nextChild, nodes)
            val link = DocLink(child.doc.docId, child.doc.title)
            childLinks.add(link)
            previousChild = link
        }

        if (docId == null) return null
        val node = DocNode(doc, parent, previous, next, childLinks)
        nodes[docId] = node
        childNodes.add(node)
        return node
    }
}

internal data class DocChild(val doc: Doc, val block: (DocNodeBuilder.() -> Unit)?)

fun buildDocTree(block: DocNodeBuilder.() -> Unit): DocTree {
    val builder = DocNodeBuilder(null)
    val nodes: MutableMap<DocId, DocNode> = mutableMapOf()
    block(builder)
    builder.buildNode(null, null, null, nodes)
    return DocTree(nodes, builder.childNodes)
}

