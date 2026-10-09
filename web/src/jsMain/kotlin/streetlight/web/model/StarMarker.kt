package streetlight.web.model

import kampfire.model.GeoPoint
import koala.SvgFile
import koala.model.TravelMarker

/** The marker of the viewer's own location. */
data class StarMarker(
    override val geoPoint: GeoPoint,
): TravelMarker {
    override val markerId get() = "star"
    override val icon get() = SvgFile.CurrentLocation
}
