package streetlight.model.data

/** The kind of record an uploaded image is for, which decides the sizes it is stored at. */
enum class ImageTarget {
    Event,
    Location,
    Galaxy,
    City,
    Media,
    Star,
    Offering,
    Block,
    Skill,
}
