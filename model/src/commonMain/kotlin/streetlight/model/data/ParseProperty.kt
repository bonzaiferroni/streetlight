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

    /** The description a page declares for other platforms to show, as html, kept whole. */
    DeclaredDescription,

    /** The url where tickets for an event are sold. */
    Tickets,

    /** The city, state and postal code of the place an event takes place at, searched with its [Address]. */
    Area,

    /** The url of a location's menu of food and drink. */
    Menu,

    /** The state of the place an event takes place at, searched with its name when its [Address] finds no place. */
    Region,
}

/** The text of each property read from a page, not yet parsed. */
typealias PropertyMap = Map<ParseProperty, String>
