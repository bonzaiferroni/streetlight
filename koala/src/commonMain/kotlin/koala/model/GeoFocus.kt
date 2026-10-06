package koala.model

///** The focus of the map: a single marker or a cluster. */
//sealed interface GeoFocus {
//    val markerId: MarkerId
//
//    fun getMarkers(): List<PointMarker>
//}
//
///** Focus on a single [marker]. */
//data class MarkerFocus(
//    val marker: PointMarker
//): GeoFocus {
//    override val markerId get() = marker.markerId
//
//    override fun getMarkers() = listOf(marker)
//}
//
///** Focus on a cluster, shown at its [principal] marker. */
//data class ClusterFocus(
//    val principal: PointMarker,
//    val members: List<PointMarker>
//): GeoFocus {
//    override val markerId get() = principal.markerId
//
//    override fun getMarkers() = members
//}