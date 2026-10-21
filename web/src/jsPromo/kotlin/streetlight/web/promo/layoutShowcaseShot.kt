package streetlight.web.promo

import kampfire.model.storeOf
import koala.dom.layout.flowBlock
import koala.dom.mountChildView
import koala.modifier.*
import koala.modifier.Glow
import koala.modifier.GlowPosition
import koala.modifier.Rgb
import koala.page.setId
import koala.utility.jsonConfig
import kotlinx.html.js.div
import streetlight.model.data.HeaderBlock
import streetlight.model.data.HeaderType
import streetlight.model.data.LocationContent
import streetlight.model.data.PageBackgroundMode
import streetlight.model.data.PageTheme
import streetlight.web.dom.page.applyTheme
import streetlight.web.layout.BodyStyle
import streetlight.web.layout.FeedMode
import streetlight.web.layout.FeedRowStyle
import streetlight.web.layout.renderLayout
import streetlight.web.page.AppBody
import web.dom.document
import web.window.window

/**
 * FizzBuzz's page in the frame of a LocationApp, its header laid out by the type that `promoHeader(name)` sets in
 * the page, starting with the hero, in that layout's theme. Its lights hold still, so every frame of a layout is the same, and its feed is
 * laid out in the grid the site falls back to.
 */
val layoutShowcaseShot = PromoShot {
    val content = jsonConfig.decodeFromString<LocationContent>(fizzBuzzContentJson)
    val headerTypeState = storeOf(HeaderType.Hero)
    val themes = showcaseThemes(content.design?.theme ?: error("FizzBuzz has no theme"))
    window.asDynamic().promoHeader = { name: String ->
        val headerType = HeaderType.valueOf(name)
        applyTheme(themes.getValue(headerType))
        headerTypeState.set(headerType)
    }

    applyTheme(themes.getValue(HeaderType.Hero))
    document.documentElement.setAttribute(FeedRowStyle.Mode.to(FeedMode.Grid))
    div {
        setId(AppBody.Viewport)
        div {
            setId(AppBody.PanelGrid)
            div {
                setId(AppBody.ContentPanel)
            }
        }
    }
    mountChildView(AppBody.ContentPanel) {
        flowBlock(headerTypeState) { headerType ->
            div {
                addModifiers(BodyStyle.ShellColumn)
                renderLayout(content.withHeader(headerType))
            }
        }
    }
}

/** The theme of each header layout, with its lights held still: FizzBuzz's own [theme] for the hero. */
private fun showcaseThemes(theme: PageTheme): Map<HeaderType, PageTheme> {
    val rho = Rgb(255, 99, 132)
    val beta = Rgb(106, 72, 51)
    val gamma = Rgb(255, 255, 255)
    return mapOf(
        HeaderType.Hero to theme,
        HeaderType.Marquee to theme.copy(
            rho = Glow(rho, GlowPosition(85, 23, 39), energy = 13, focus = 37),
            beta = Glow(beta, GlowPosition(81, 28, 130), energy = 0, focus = 48),
            gamma = Glow(gamma, GlowPosition(51, 76, 56), energy = 16, focus = 0),
        ),
        HeaderType.Card to theme.copy(
            rho = Glow(rho, GlowPosition(33, 36, 39), energy = 0, focus = 37),
            beta = Glow(beta, GlowPosition(81, 28, 130), energy = 45, focus = 48),
            gamma = Glow(gamma, GlowPosition(51, 76, 35), energy = 16, focus = 0),
        ),
    ).mapValues { it.value.copy(backgroundMode = PageBackgroundMode.Static) }
}

/** This content with its layout's header block laid out by [headerType], without its cells. */
private fun LocationContent.withHeader(headerType: HeaderType): LocationContent {
    val design = design ?: error("FizzBuzz has no design")
    val layout = design.layout ?: error("FizzBuzz has no layout")
    val blocks = layout.blocks.map { if (it is HeaderBlock) it.copy(headerType = headerType, showCells = false) else it }
    return copy(design = design.copy(layout = layout.copy(blocks = blocks)))
}
