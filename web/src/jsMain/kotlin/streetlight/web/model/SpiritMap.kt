package streetlight.web.model

import koala.model.GeoMap
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import streetlight.model.data.Spirit
import streetlight.model.data.SpiritFrame
import streetlight.model.data.SpiritId
import streetlight.web.io.ApiClient
import streetlight.web.io.SpiritSocket

class SpiritMap(
    private val geoMap: GeoMap,
    private val scope: CoroutineScope,
    private val api: ApiClient
) {
    private val state = storeOf(SpiritMapState())
    val stateNow get() = state.now

    val spiritVision = SpiritSocket(api, scope)

    init {
        scope.launch {
            launch {
                geoMap.stateFlow.filter { it.isViewed }.collect { geoMapState ->
                    if (!geoMapState.isMoving) {
                        spiritVision.updatePosition(geoMapState.center)
                    }
                }
            }
            launch {
                spiritVision.spiritFlow.collect { frame ->
                    when (frame) {
                        is SpiritFrame.Initial -> geoMap.addEntity(SpiritEntity(frame.spirit))
                        is SpiritFrame.Position -> geoMap.moveEntity(frame.id.toEntityId(), frame.pos)
                    }
                }
            }
        }
    }

    fun spiritVision(isOn: Boolean) {
        val spirit = Spirit(
            spiritId = SpiritId.random(),
            position = geoMap.stateNow.center,
            name = stateNow.spiritName
        )
        if (isOn) {
            spiritVision.connect(spirit)
        } else {
            spiritVision.disconnect()
        }
    }
}

data class SpiritMapState(
    val hasSpiritVision: Boolean = false,
    val spiritName: String = "👻",
)