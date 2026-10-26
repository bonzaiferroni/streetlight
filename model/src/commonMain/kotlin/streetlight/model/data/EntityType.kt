package streetlight.model.data

/**
 * The type of an entity, such as an event or a location. Each ship gives a type its color: the web a custom property,
 * compose with `KoalaColors.colorOf`, and each gives no type the primary color.
 */
enum class EntityType {
    City,
    Event,
    Galaxy,
    Location,
    Media,
    Star,
    Offering,
    Skill,
}

/** The type of this entity, or `null` for a custom entity of a record without a type of its own. */
fun Entity.toEntityType(): EntityType? = when (this) {
    is City -> EntityType.City
    is EventLocation -> EntityType.Event
    is EventPost -> EntityType.Event
    is Event -> EntityType.Event
    is Galaxy -> EntityType.Galaxy
    is LocationPost -> EntityType.Location
    is Location -> EntityType.Location
    is MediaPost -> EntityType.Media
    is Media -> EntityType.Media
    is CustomEntity -> when (recordType) {
        RecordType.Location -> EntityType.Location
        RecordType.Event -> EntityType.Event
        else -> null
    }
    is Star -> EntityType.Star
    is EventGroup -> EntityType.Event
    is Offering -> EntityType.Offering
    is Skill -> EntityType.Skill
}
