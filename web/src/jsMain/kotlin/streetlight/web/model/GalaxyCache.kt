package streetlight.web.model

import koala.dom.setStorageOf
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.model.data.LightEdit
import streetlight.web.io.ApiClient

class GalaxyCache(
    private val scope: CoroutineScope,
    private val config: SiteConfig,
    private val api: ApiClient,
) {
    private val state = storeOf(GalaxyCacheState())
    val stateNow get() = state.now
    val stateFlow = state.flow
    val lightFlow = stateFlow.mapDistinct { it.lights }
    val galaxiesFlow = stateFlow.mapDistinct { it.galaxies }

    private var lights by setStorageOf(GALAXY_CACHE_KEY) { GalaxyId(it) }

    init {
        scope.launch {
            launch {
                val lights = readLights()
                state.set { it.copy(lights = lights) }
            }
            launch {
                lightFlow.collect { light ->
                    val galaxies = when (light.isEmpty()) {
                        true -> emptyList()
                        else -> api.readGalaxies(light.toList()) ?: emptyList() // td: fail message
                    }.sortedBy { it.createdAt }
                    state.set { it.copy(galaxies = galaxies) }
                }
            }
        }
    }

    fun toggleLight(galaxyId: GalaxyId) {
        val isStar = stateNow.lights.contains(galaxyId)
        when (isStar) {
            true -> removeStar(galaxyId)
            else -> addStar(galaxyId)
        }
    }

    fun addStar(galaxyId: GalaxyId) = editStar(galaxyId, true)
    fun removeStar(galaxyId: GalaxyId) = editStar(galaxyId, false)

    private fun editStar(galaxyId: GalaxyId, isStar: Boolean) {
        when (config.stateNow.lightSync) {
            true -> {
                scope.launch {
                    val edit = LightEdit(galaxyId.value, true)
                    val isSuccess = api.editGalaxyLight(edit) ?: return@launch // td: ui message
                    if (isSuccess)
                        editState(galaxyId, isStar)
                }
            }
            else -> {
                when (isStar) {
                    true -> lights += galaxyId
                    else -> lights -= galaxyId
                }
                editState(galaxyId, isStar)
            }
        }
    }

    private fun editState(galaxyId: GalaxyId, isStar: Boolean) {
        when (isStar) {
            true -> state.set { it.copy(lights = it.lights + galaxyId) }
            else -> state.set { it.copy(lights = it.lights - galaxyId) }
        }
    }

    private suspend fun readLights() = when(config.stateNow.lightSync) {
        true -> api.readGalaxyLights()?.toSet() ?: emptySet()
        else -> lights
    }
}

data class GalaxyCacheState(
    val lights: Set<GalaxyId> = emptySet(),
    val galaxies: List<Galaxy> = emptyList(),
)

const val GALAXY_CACHE_KEY = "streetlight.galaxy-cache"