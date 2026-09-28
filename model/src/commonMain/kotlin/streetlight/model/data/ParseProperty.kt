package streetlight.model.data

/** A property the crawler reads from a page, shared by every kind of record that has it. */
enum class ParseProperty {
    /** An event's title, or a location's name. */
    Name,
    Url,
    Image,

    /** The description, as html. */
    Description,
    Contact,
    Cost,
    AgeMin,
    Date,
    StartTime,
    EndTime,

    /** The name of the place an event takes place at. */
    Location,
    Address,
    Phone,
    Email,
    Hours,
    EventsLink,
    SocialLinks,
}

/** The text of each property read from a page, not yet parsed. */
typealias PropertyMap = Map<ParseProperty, String>
