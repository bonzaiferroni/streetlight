package streetlight.model.data

import kampfire.model.GeoPoint

sealed interface DesignContent {
    val design: PageDesign?
    val geoPoint: GeoPoint? get() = null
}