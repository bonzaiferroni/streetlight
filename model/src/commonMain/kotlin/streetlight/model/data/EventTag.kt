package streetlight.model.data

import kampfire.model.Labeled
import koala.SvgFile
import koala.SvgPack


enum class EventTag(val svg: SvgPack, label: String? = null): Labeled {
    // Broad
    Meetup(SvgFile.Coffee),
    Music(SvgFile.Music),
    Church(SvgFile.BuildingChurch),
    HealthAndFitness(SvgFile.Heartbeat, "Health & Fitness"),
    Education(SvgFile.School),
    Sports(SvgFile.BallVolleyball),
    Volunteer(SvgFile.HeartHandshake),
    KidsAndFamily(SvgFile.Family, "Kids & Family"),
    Arts(SvgFile.Violin),
    FoodAndDrink(SvgFile.ToolsKitchen, "Food & Drink"),

    // Social
    Picnic(SvgFile.Basket),
    Potluck(SvgFile.Soup),
    Networking(SvgFile.Affiliate),
    BooksAndWriting(SvgFile.Book, "Books & Writing"),
    GamesAndTrivia(SvgFile.Dice, "Games & Trivia"),
    Singles(SvgFile.Hearts),
    LGBTQ(SvgFile.Rainbow),
    Tech(SvgFile.DeviceLaptop),
    Pets(SvgFile.Paw),
    WatchParty(SvgFile.DeviceTv, "Watch Party"), // replace with Party

    // Music
    Concert(SvgFile.Ticket),
    OpenMic(SvgFile.Microphone, "Open Mic"),
    StreetPerformance(SvgFile.AcousticGuitar, "Street Performance"),
    DJSet(SvgFile.Vinyl, "DJ Set"),
    Karaoke(SvgFile.MicrophoneHandheld),
    Dance(SvgFile.DiscoBall),

    // Health & Fitness
    Hike(SvgFile.Trekking),
    Nature(SvgFile.Leaf),
    Exercise(SvgFile.Barbell),
    Wellness(SvgFile.Yoga),

    // Education
    Class(SvgFile.School),
    Crafting(SvgFile.NeedleThread),
    Lecture(SvgFile.Presentation),

    // Sports
    SportsMatch(SvgFile.Scoreboard, "Sports Match"),
    PickupGame(SvgFile.BallBasketball, "Pickup Game"),

    // Arts
    Theater(SvgFile.MasksTheater),
    Film(SvgFile.Movie),
    ArtExhibition(SvgFile.Palette, "Art Exhibition"),
    Comedy(SvgFile.MoodCrazyHappy),

    // Volunteer
    Fundraiser(SvgFile.PigMoney),
    Cleanup(SvgFile.Recycle),
    CommunityOutreach(SvgFile.Speakerphone, "Community Outreach"),

    // Food & Drink
    FoodTruck(SvgFile.Truck, "Food Truck"),
    Tasting(SvgFile.GlassFull),

    // General
    Festival(SvgFile.Confetti),
    Holiday(SvgFile.Fireworks),
    Market(SvgFile.BuildingStore),
    Politics(SvgFile.BuildingBank),

    // Seasonal
    Halloween(SvgFile.Pumpkin);

    override val label = label ?: name
}
/** A group of [tags] shown together in a menu. A tag may belong to more than one category. */
enum class EventCategory(val tags: List<EventTag>, label: String? = null): Labeled {
    General(listOf(
        EventTag.Meetup, EventTag.Music, EventTag.Church, EventTag.HealthAndFitness, EventTag.Education,
        EventTag.Sports, EventTag.Volunteer, EventTag.KidsAndFamily, EventTag.Arts, EventTag.FoodAndDrink,
        EventTag.Festival, EventTag.Holiday, EventTag.Market, EventTag.Politics, EventTag.Halloween,
    )),
    Social(listOf(
        EventTag.Meetup, EventTag.Picnic, EventTag.Potluck, EventTag.Networking, EventTag.BooksAndWriting,
        EventTag.GamesAndTrivia, EventTag.Singles, EventTag.LGBTQ, EventTag.Tech, EventTag.Pets, EventTag.WatchParty,
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
