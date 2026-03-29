package streetlight.web.ui

import koala.dom.*
import koala.model.storeOf
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.web.model.Streetlight
import streetlight.web.shells.cardOf

fun RenderContext.viewGalaxyList(app: Streetlight) {
    val galaxyStore = storeOf<List<Galaxy>>(emptyList())

    renderScope.launch {
        val galaxies = app.client.api.readTopGalaxies() ?: return@launch
        galaxyStore.set { galaxies }
    }

    flowBlock(galaxyStore.flow) { galaxies ->
        galaxies.forEach {
            box {
                cardOf(it)
            }
        }
    }
}