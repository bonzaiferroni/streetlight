package streetlight.web.model

import kampfire.model.GeoPoint
import koala.SvgFile
import koala.model.TravelMarker

/** The marker of the viewer's own location, drawn as an arrow while it has a [bearing]. */
data class StarMarker(
    override val geoPoint: GeoPoint,
    override val bearing: Float? = null,
): TravelMarker {
    override val markerId get() = if (bearing != null) "star-heading" else "star"
    override val icon get() = if (bearing != null) SvgFile.NavigationFilled else SvgFile.CurrentLocation
}
