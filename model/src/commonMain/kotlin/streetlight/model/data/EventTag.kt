package streetlight.model.data

import kampfire.model.Labeled


enum class EventTag(label: String? = null): Labeled {
    // Broad
    Meetup,
    Music,
    Church,
    HealthAndFitness("Health & Fitness"),
    Education,
    Sports,
    Volunteer,
    KidsAndFamily("Kids & Family"),
    Arts,
    FoodAndDrink("Food & Drink"),

    // Meetup
    Picnic,
    Potluck,
    Networking,
    BooksAndWriting("Books & Writing"),
    GamesAndTrivia("Games & Trivia"),
    Singles,
    LGBTQ,
    Tech,
    Pets,
    WatchParty("Watch Party"),

    // Music
    Concert,
    OpenMic("Open Mic"),
    StreetPerformance("Street Performance"),
    DJSet("DJ Set"),
    Karaoke,
    Dance,

    // Health & Fitness
    Hike,
    Nature,
    Exercise,
    Wellness,

    // Education
    Class,
    Crafting,
    Lecture,

    // Sports
    SportsMatch("Sports Match"),
    PickupGame("Pickup Game"),

    // Arts
    Theater,
    Film,
    ArtExhibition("Art Exhibition"),
    Comedy,

    // Volunteer
    Fundraiser,
    Cleanup,
    CommunityOutreach("Community Outreach"),

    // Food & Drink
    FoodTruck("Food Truck"),
    Tasting,

    // General
    Festival,
    Holiday,
    Market,
    Politics;

    override val label = label ?: name
}