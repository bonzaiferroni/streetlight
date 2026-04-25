package streetlight.web.ui

import kabinet.utils.toAgoFormat
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.heading5
import koala.html.image
import koala.html.markdown
import koala.html.navigation
import koala.model.createDiv
import kotlinx.browser.document
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import streetlight.model.data.Comment
import streetlight.web.StarRoute

class CommentView(
    val comment: Comment,
    val body: HTMLElement,
    val childColumn: HTMLElement,
) {

}

