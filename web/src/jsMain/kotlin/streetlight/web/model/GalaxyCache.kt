package streetlight.web.model

import koala.dom.setStorageOf
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.model.data.StarEdit
import streetlight.web.io.ApiClient

class GalaxyCache(
    private val scope: CoroutineScope,
    private val config: SiteConfig,
    private val api: ApiClient,
) {
    private val state = storeOf(GalaxyCacheState())
    val stateNow get() = state.now
    val stateFlow = state.flow
    val starFlow = stateFlow.mapDistinct { it.stars }
    val galaxiesFlow = stateFlow.mapDistinct { it.galaxies }

    private var stars by setStorageOf(GALAXY_CACHE_KEY) { GalaxyId(it) }

    init {
        scope.launch {
            launch {
                val stars = readStars()
                state.set { it.copy(stars = stars) }
            }
            launch {
                starFlow.collect { stars ->
                    val galaxies = when (stars.isEmpty()) {
                        true -> emptyList()
                        else -> api.readGalaxies(stars.toList()) ?: emptyList() // td: fail message
                    }.sortedBy { it.createdAt }
                    state.set { it.copy(galaxies = galaxies) }
                }
            }
        }
    }

    fun toggleStar(galaxyId: GalaxyId) {
        val isStar = stateNow.stars.contains(galaxyId)
        when (isStar) {
            true -> removeStar(galaxyId)
            else -> addStar(galaxyId)
        }
    }

    fun addStar(galaxyId: GalaxyId) = editStar(galaxyId, true)
    fun removeStar(galaxyId: GalaxyId) = editStar(galaxyId, false)

    private fun editStar(galaxyId: GalaxyId, isStar: Boolean) {
        when (config.stateNow.starSync) {
            true -> {
                scope.launch {
                    val edit = StarEdit(galaxyId.value, true)
                    val isSuccess = api.editGalaxyStar(edit) ?: return@launch // td: ui message
                    if (isSuccess)
                        editState(galaxyId, isStar)
                }
            }
            else -> {
                when (isStar) {
                    true -> stars += galaxyId
                    else -> stars -= galaxyId
                }
                editState(galaxyId, isStar)
            }
        }
    }

    private fun editState(galaxyId: GalaxyId, isStar: Boolean) {
        when (isStar) {
            true -> state.set { it.copy(stars = it.stars + galaxyId) }
            else -> state.set { it.copy(stars = it.stars - galaxyId) }
        }
    }

    private suspend fun readStars() = when(config.stateNow.starSync) {
        true -> api.readGalaxyStars()?.toSet() ?: emptySet()
        else -> stars
    }
}

data class GalaxyCacheState(
    val stars: Set<GalaxyId> = emptySet(),
    val galaxies: List<Galaxy> = emptyList(),
)

const val GALAXY_CACHE_KEY = "streetlight.galaxy-cache"