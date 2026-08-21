package koala.dom

import kampfire.model.Url
import koala.css.Modifier
import koala.css.modify
import koala.markdown.ParsedBlock
import kotlinx.html.a

class MarkdownEditorRenderer() {
    private var index = 0
    private var chunk = ""

    fun AppendScope.renderBlock(block: ParsedBlock) {
        index = 0
        chunk = block.chunk
    }

    private fun markRender(endIndex: Int) {
        index = endIndex
    }

    private fun AppendScope.renderSpan(endIndex: Int, mod: Modifier?) {
        if (index == endIndex) return
        span(chunk.substring(index, endIndex), modify(mod))
        markRender(endIndex)
    }

    private fun AppendScope.renderLink(url: Url) {
        // do we use the url or a substring from chunk?
        a {
            // td: make clickable with shift or something
            // href = url.value
            +url.value
        }
        markRender(index + url.value.length)
    }
}