package streetlight.web.promo

import koala.dom.mountRootView
import kotlinx.coroutines.MainScope
import streetlight.web.integration.buildTestApp
import streetlight.web.page.buildSiteStyles
import web.dom.document
import web.html.HTMLStyleElement
import web.url.URL
import web.window.window

/** Adds the site stylesheet and mounts the shot named by the `shot` query parameter as the page body. */
fun main() {
    val style = document.createElement("style") as HTMLStyleElement
    style.textContent = buildSiteStyles()
    document.head.append(style)
    val name = URL(window.location.href).searchParams.get("shot".toJsString())?.toString()
    val shot = shots[name] ?: error("no promo shot named $name; shots: ${shots.keys}")
    val scope = MainScope()
    val app = buildTestApp(scope, shot.api())
    document.body.mountRootView("promo-$name", scope, app, shot.view)
}

private val shots = mapOf(
    "theme" to themeShot,
)
