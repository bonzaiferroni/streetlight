package streetlight.web.ui

import kampfire.model.handleResponse
import koala.dom.*
import koala.model.storeOf
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.web.shells.cardOf

fun AppScope.viewGalaxyList() {
    val galaxyStore = storeOf<List<Galaxy>>(emptyList())

    parentScope.launch {
        val galaxies = api.readTopGalaxies().handleResponse(toaster::toast) ?: return@launch
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