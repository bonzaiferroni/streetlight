package streetlight.web.ui

import kampfire.model.GeoPoint
import kampfire.model.distanceTo
import kampfire.model.meters
import koala.css.AlignItemsStretch
import koala.css.Dim
import koala.css.Flex1
import koala.css.Flex2
import koala.css.Blur
import koala.css.Magic
import koala.css.MediaMdRow
import koala.css.Aspect1
import koala.css.Width100P
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.button
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.row
import koala.dom.textBlock
import koala.dom.textField
import koala.dom.wireGeoMap
import koala.html.geoMapMount
import koala.html.textSpan
import koala.model.PanPoint
import koala.model.mapDistinct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import streetlight.model.data.PlaceProto
import streetlight.web.model.Streetlight

fun RenderContext.placeEditor(
    geoPoint: GeoPoint?,
    app: Streetlight,
    model: PlaceEditor
) {
    val geoMap = app.geoMap
    val nameFlow = model.placeFlow.mapDistinct { it?.name }
    val addressFlow = model.placeFlow.mapDistinct { it?.address }
    val pointFlow = model.placeFlow.mapDistinct { it?.geoPoint }

    renderScope.launch {
        launch {
            pointFlow.collect { point ->
                if (point != null && point.distanceTo(geoMap.stateNow.center) > 10.meters) {
                    console.log(point)
                    geoMap.panMap(PanPoint(point = point, zoom = 18f))
                }
            }
        }
        launch {
            geoMap.centerFlow.collect { center ->
                model.setPoint(center)
            }
        }
    }

    val element = column(modify(MediaMdRow, AlignItemsStretch)) {
        geoMapMount(geoPoint, modify(Flex1, Aspect1))
        column(modify(Flex2, AlignItemsStretch)) {
            row {
                textField(
                    label = "location name",
                    placeholder = "Location name",
                    modifiers = modify(Flex1),
                    onValue = model::setPlaceName,
                    flow = nameFlow,
                )
                button("🤖 look up", onClick = model::lookUp)
            }
            textField(
                label = "address",
                placeholder = "Address",
                modifiers = modify(Width100P),
                onValue = model::setAddress,
                flow = addressFlow,
            )
            flowBlock(pointFlow, modifiers = modify(Magic, Blur)) { point ->
                if (point != null) {
                    this.textBlock {
                        textSpan("latitude: ", modify(Dim))
                        textSpan(point.lat.toString())
                        textSpan(" longitude: ", modify(Dim))
                        textSpan(point.lng.toString())
                    }
                }
            }
        }
    }

    wireGeoMap(app.geoMap, app.appScope, element)
}

interface PlaceEditor {
    val placeFlow: Flow<PlaceProto?>

    fun setPlaceName(value: String)
    fun setAddress(value: String)
    fun setPoint(value: GeoPoint)

    fun lookUp()
}