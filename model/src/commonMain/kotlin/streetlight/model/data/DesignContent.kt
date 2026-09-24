package streetlight.model.data

import kampfire.model.GeoPoint

/** Route content with a page design, laid out by its blocks. */
sealed interface DesignContent {
    val design: PageDesign?
    val geoPoint: GeoPoint? get() = null
}