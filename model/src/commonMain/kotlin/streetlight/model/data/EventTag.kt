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

    // Meetup
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
    Politics(SvgFile.BuildingBank);

    override val label = label ?: name
}