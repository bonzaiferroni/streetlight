package streetlight.web.view

import koala.dom.*
import streetlight.model.data.DocContent
import streetlight.model.ui.SiteDocRoute
import streetlight.web.shell.SiteDocStyle
import streetlight.web.shell.siteDocShell
import koala.core.queryAll
import koala.core.queryFirstOrNull
import koala.modifier.toggle
import koala.modifier.modify
import koala.modifier.unmodify
import koala.modifier.PrimaryFg
import koala.external.IntersectionObserver
import koala.external.IntersectionObserverInit
import streetlight.web.shell.sectionJumps
import web.dom.Element
import web.dom.document
import web.window.window
import koala.dom.page.*

fun ViewScope.viewSiteDoc(content: DocContent) {
    val shell = shellBox {
        siteDocShell(content)
    }

    val container = shell.queryFirstOrNull(SiteDocStyle.Container) ?: return
    shell.queryAll(SiteDocStyle.Switch).forEach { switch ->
        switch.onClick { container.toggle(SiteDocStyle.Toggle) }
    }

    highlightCurrentSection(shell, content)
}

/** Marks the link in the sections menu of [shell] whose section is current, as the reader scrolls. */
private fun ViewScope.highlightCurrentSection(shell: Element, content: DocContent) {
    val links = shell.queryAll(SiteDocStyle.SectionLink)
    if (links.isEmpty()) return
    val targets = content.node.sectionJumps().map { document.getElementOrNullById(it.second) }
    var current: Element? = null

    fun update() {
        val line = window.innerHeight * CurrentSectionLinePct / 100.0
        val index = targets.indexOfLast { it != null && it.getBoundingClientRect().top <= line }.coerceAtLeast(0)
        val link = links.getOrNull(index) ?: return
        if (link == current) return
        current?.unmodify(PrimaryFg)
        current = link.modify(PrimaryFg)
    }

    val observer = IntersectionObserver(
        { _, _ -> update() },
        IntersectionObserverInit(root = null, rootMargin = "0px 0px -${100 - CurrentSectionLinePct}% 0px", threshold = 0)
    )
    targets.filterNotNull().forEach { observer.observe(it) }
    update()
    onDispose { observer.disconnect() }
}

/** The percent of the viewport's height, from its top, past which a section's heading makes it current. */
private const val CurrentSectionLinePct = 25

fun RouteScope.viewSiteDocRoute() {
    routeBlock<SiteDocRoute, DocContent> { node ->
        viewSiteDoc(node)
    }
}
