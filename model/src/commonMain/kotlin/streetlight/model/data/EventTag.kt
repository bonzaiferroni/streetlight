package streetlight.model.data

import kampfire.model.Labeled
import koala.SvgFile
import koala.SvgPack


/** A kind of event, at a [specificity] of 0 for a broad category, rising as the tag names an event more narrowly. */
enum class EventTag(val specificity: Int, val svg: SvgPack, label: String? = null): Labeled {
    // Broad
    Meetup(0, SvgFile.Coffee),
    Music(0, SvgFile.Music),
    Church(0, SvgFile.BuildingChurch),
    HealthAndFitness(0, SvgFile.Heartbeat, "Health & Fitness"),
    Education(0, SvgFile.School),
    Sports(0, SvgFile.BallVolleyball),
    Volunteer(0, SvgFile.HeartHandshake),
    KidsAndFamily(0, SvgFile.Family, "Kids & Family"),
    Arts(0, SvgFile.Violin),
    FoodAndDrink(0, SvgFile.ToolsKitchen, "Food & Drink"),

    // Social
    Picnic(1, SvgFile.Basket),
    Potluck(2, SvgFile.Soup),
    Networking(1, SvgFile.Affiliate),
    BooksAndWriting(1, SvgFile.Book, "Books & Writing"),
    GamesAndTrivia(1, SvgFile.Dice, "Games & Trivia"),
    Singles(1, SvgFile.Hearts),
    LGBTQ(1, SvgFile.Rainbow),
    Tech(1, SvgFile.DeviceLaptop),
    Pets(1, SvgFile.Paw),
    Party(1, SvgFile.DeviceTv, "Watch Party"), // replace with Party

    // Music
    Concert(1, SvgFile.Ticket),
    OpenMic(2, SvgFile.Microphone, "Open Mic"),
    StreetPerformance(2, SvgFile.AcousticGuitar, "Street Performance"),
    DJSet(2, SvgFile.Vinyl, "DJ Set"),
    Karaoke(2, SvgFile.MicrophoneHandheld),
    Dance(1, SvgFile.DiscoBall),

    // Health & Fitness
    Hike(1, SvgFile.Trekking),
    Nature(1, SvgFile.Leaf),
    Exercise(1, SvgFile.Barbell),
    Wellness(1, SvgFile.Yoga),

    // Education
    Class(1, SvgFile.School),
    Crafting(1, SvgFile.NeedleThread),
    Lecture(1, SvgFile.Presentation),

    // Sports
    SportsMatch(1, SvgFile.Scoreboard, "Sports Match"),
    PickupGame(2, SvgFile.BallBasketball, "Pickup Game"),

    // Arts
    Theater(1, SvgFile.MasksTheater),
    Film(1, SvgFile.Movie),
    ArtExhibition(1, SvgFile.Palette, "Art Exhibition"),
    Comedy(1, SvgFile.MoodCrazyHappy),

    // Volunteer
    Fundraiser(1, SvgFile.PigMoney),
    Cleanup(1, SvgFile.Recycle),
    CommunityOutreach(1, SvgFile.Speakerphone, "Community Outreach"),

    // Food & Drink
    FoodTruck(1, SvgFile.Truck, "Food Truck"),
    Tasting(1, SvgFile.GlassFull),

    // General
    Festival(1, SvgFile.Confetti),
    Holiday(0, SvgFile.Fireworks),
    Market(1, SvgFile.BuildingStore),
    Politics(1, SvgFile.BuildingBank),

    // Seasonal
    Halloween(2, SvgFile.Pumpkin);

    override val label = label ?: name
}
/** The tag of these with the highest [EventTag.specificity], the first among equals, or `null` when there are none. */
fun List<EventTag>.mostSpecific(): EventTag? = maxByOrNull { it.specificity }

/** A group of [tags] shown together in a menu. A tag may belong to more than one category. */
enum class EventCategory(val tags: List<EventTag>, label: String? = null): Labeled {
    General(listOf(
        EventTag.Meetup, EventTag.Music, EventTag.Church, EventTag.HealthAndFitness, EventTag.Education,
        EventTag.Sports, EventTag.Volunteer, EventTag.KidsAndFamily, EventTag.Arts, EventTag.FoodAndDrink,
        EventTag.Festival, EventTag.Holiday, EventTag.Market, EventTag.Politics, EventTag.Halloween,
    )),
    Social(listOf(
        EventTag.Meetup, EventTag.Picnic, EventTag.Potluck, EventTag.Networking, EventTag.BooksAndWriting,
        EventTag.GamesAndTrivia, EventTag.Singles, EventTag.LGBTQ, EventTag.Tech, EventTag.Pets, EventTag.Party,
    )),
    Music(listOf(
        EventTag.Music, EventTag.Concert, EventTag.OpenMic, EventTag.StreetPerformance, EventTag.DJSet,
        EventTag.Karaoke, EventTag.Dance,
    )),
    HealthAndFitness(listOf(
        EventTag.HealthAndFitness, EventTag.Hike, EventTag.Nature, EventTag.Exercise, EventTag.Wellness,
    ), "Health & Fitness"),
    Education(listOf(
        EventTag.Education, EventTag.Class, EventTag.Crafting, EventTag.Lecture, EventTag.BooksAndWriting,
    )),
    Sports(listOf(
        EventTag.Sports, EventTag.SportsMatch, EventTag.PickupGame,
    )),
    Arts(listOf(
        EventTag.Arts, EventTag.Theater, EventTag.Film, EventTag.ArtExhibition, EventTag.Comedy, EventTag.Crafting,
    )),
    Volunteer(listOf(
        EventTag.Volunteer, EventTag.Fundraiser, EventTag.Cleanup, EventTag.CommunityOutreach,
    )),
    KidsAndFamily(listOf(
        EventTag.KidsAndFamily, EventTag.Picnic, EventTag.Pets, EventTag.Crafting, EventTag.Festival,
    ), "Kids & Family"),
    FoodAndDrink(listOf(
        EventTag.FoodAndDrink, EventTag.FoodTruck, EventTag.Tasting, EventTag.Potluck, EventTag.Market,
    ), "Food & Drink"),
    Seasonal(listOf(
        EventTag.Halloween,
    ));

    override val label = label ?: name
}
