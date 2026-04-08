package streetlight.web.ui

import kampfire.model.thumb
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.ButtonMenu
import koala.html.buttonMenu
import koala.html.filigree
import koala.html.heading1
import koala.html.heading3
import koala.html.section
import koala.html.spacer
import koala.model.mapDistinct
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPostResult
import streetlight.model.data.PostResult
import streetlight.web.GalaxySlugRoute
import streetlight.web.LocationScoutRoute
import streetlight.web.model.LocationFinder
import streetlight.web.model.LocationScout
import streetlight.web.model.Streetlight

fun RenderContext.viewLocationScout(app: Streetlight, galaxy: Galaxy, galaxies: List<Galaxy>) {
    val sectionMod = modify()
    val finder = LocationFinder(renderScope, app)
    val model = LocationScout(renderScope, app, finder, galaxy.galaxyId)
    val locationFlow = finder.stateFlow.mapDistinct { it.location }
    val resultFlow = model.stateFlow.mapDistinct { it.result }
    val allGalaxies = when (galaxies.any { it.galaxyId == galaxy.galaxyId }) {
        true -> galaxies
        else -> listOf(galaxy) + galaxies
    } // combine target galaxy with starred galaxies

    column(modify(Gap8)) {
        section(sectionMod) {
            filigree {
                heading1("Location Scout", modify(Shrinkable))
            }
            heading3("Posting to ${galaxy.name}", modify(OpacityMost, TextAlignCenter))
        }

        section(sectionMod) {
            filigree {
                heading3("Find a location")
            }
            viewContextOf(finder) {
                viewLocationFinder()
            }
        }

        flowBlock(locationFlow, defaultMagic) { location ->
            val location = location ?: return@flowBlock

            column { // required for boxing FlowContent
                section {
                    filigree {
                        heading3("Post ${location.name}")
                    }

                    flowBlock(resultFlow, defaultMagic) { result ->
                        viewContextOf(model) {
                            when (result) {
                                null -> postLocationEditor(allGalaxies)
                                else -> postResult(result, allGalaxies)
                            }
                        }
                    }
                }
            }
        }

        appFooter("web/src/jsMain/kotlin/streetlight/web/ui/viewLocationScout.kt")
    }
}

fun ViewContext<LocationScout>.postLocationEditor(galaxies: List<Galaxy>) {
    val titleFlow = model.stateFlow.mapDistinct { it.title }
    val textFlow = model.stateFlow.mapDistinct { it.text }
    val galaxiesFlow = model.stateFlow.mapDistinct { it.galaxyIds }

    card(modify(ZenCardBg)) {
        flowBlock(galaxiesFlow) { galaxyIds ->
            val toGalaxies = galaxies.filter { galaxyIds.contains(it.galaxyId) }
            val availableGalaxies = galaxies.filter { !galaxyIds.contains(it.galaxyId) }

            row(modify(MinHeight5)) {
                row(modify(Flex1, AlignItemsCenter, WrapFlex)) {
                    textBlock("Post to:", modify(WhiteSpaceNoWrap, MarginLeft1, OpacityMost))
                    toGalaxies.forEach { galaxy ->
                        row(modify(AlignItemsCenter, Gap0)) {
                            textBlock(galaxy.name, modify(WhiteSpaceNoWrap))
                            button(SvgFile.Backspace, onClick = { model.removeGalaxyId(galaxy.galaxyId) })
                        }
                    }
                }
                if (availableGalaxies.isNotEmpty()) {
                    buttonMenu("galaxies", modify(AlignSelfStart)) {
                        card(ButtonMenu.CardMod) {
                            availableGalaxies.forEach { galaxy ->
                                button(galaxy.name, onClick = { model.addGalaxyId(galaxy.galaxyId) })
                            }
                        }
                    }
                }
            }
        }

        textField("title", onValue = model::setTitle, flow = titleFlow)
        textEditor("text", onValue = model::setText, flow = textFlow)
        row(modify(JustifyContentSpaceBetween)) {
            spacer()
            button("Post", modify(Accent), model::createPost)
        }
    }
}

fun ViewContext<LocationScout>.postResult(result: GalaxyPostResult, galaxies: List<Galaxy>) {
    column {
        card(modify(ZenCardBg)) {
            result.results.forEach { (galaxyId, result) ->
                val galaxy = galaxies.first { it.galaxyId == galaxyId }
                val emoji = when (result) {
                    PostResult.Posted -> "✔"
                    PostResult.Conflict -> "👍"
                }
                val msg = when (result) {
                    PostResult.Posted -> "Posted."
                    PostResult.Conflict -> "Already in galaxy."
                }
                cardOf("${galaxy.name}: $emoji", galaxy.images.thumb, msg) {
                    portal.go(GalaxySlugRoute(galaxy.slug))
                }
            }
        }
        row(modify(JustifyContentSpaceBetween)) {
            button("Post another location", onClick = model::reset )
            button("Done", onClick = { portal.goBack() })
        }
    }
}

fun ViewContext<Streetlight>.viewLocationScoutRoute() {
    routeBlock<LocationScoutRoute, Galaxy>({
        api.readGalaxy(it.slug)
    }) { galaxy ->
        viewLocationScout(model, galaxy, model.cache.galaxy.stateNow.items)
    }
}