package koala.dom

import kotlinx.html.TagConsumer
import org.w3c.dom.HTMLElement

/** A builder that appends elements to a live DOM parent. */
typealias AppendScope = TagConsumer<HTMLElement>
