package streetlight.model.data

/** The [PropertyMap] that [block] fills, holding only the properties it gave a value that is not blank. */
fun buildPropertyMap(block: PropertyMapBuilder.() -> Unit): PropertyMap =
    buildMap { PropertyMapBuilder(this).block() }

/** The builder of a [PropertyMap], keeping a property only when its value is not blank. */
class PropertyMapBuilder internal constructor(private val properties: MutableMap<ParseProperty, String>) {

    /** Sets [property] to [value], unless [value] is null or blank. */
    operator fun set(property: ParseProperty, value: String?) {
        value?.takeIf { it.isNotBlank() }?.let { properties[property] = it }
    }
}
