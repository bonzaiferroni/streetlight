# Travel Marker

`TravelMarker` lost its body when `PointMarker.configureBody` replaced `PointMarkerBody`. `VehicleMarker` renders an empty body, and nothing turns a marker to its `bearing`. `MarkerStyle.Travel`, `Bearing`, `MarkerSvg` and `MarkerBearing` remain for its return.

The bearing handling of the former `IconMarkerBody`, which took the shortest turn to each new bearing so the rotation never spins the long way around:

```kotlin
var lastBearing = 0f

fun setBearing(bearing: Float) {
    val be = this.bearingElement ?: return
    val delta = ((bearing - lastBearing + 540) % 360) - 180;
    lastBearing += delta
    val adjusted = lastBearing - 90
    be.modify(MarkerStyle.MarkerBearing.of(adjusted.deg))
}
```

Its body, built once and updated with `setBearing` on each `update(marker)`:

```kotlin
val body = div {
    addModifiers(modify(MarkerStyle.Travel, MarkerStyle.Body, MarkerStyle.MarkerSvg.of(marker.icon)))
}.asWeb()

val bearingElement = marker.bearing?.let {
    div {
        addModifiers(MarkerStyle.Bearing)
    }.asWeb()
}
```

Bounty:
* Give `TravelMarker` a default `configureBody` drawn from `icon`
* Turn the marker to its `bearing` on update, as `MarkerBearing` set on the marker's root, with `lastBearing` kept on `PointMarkerElement`
* Restore the body of `VehicleMarker`
* Show vehicles only above zoom 14.5 through their reveal zoom, as the retired `Altitude.Raincloud` once did
