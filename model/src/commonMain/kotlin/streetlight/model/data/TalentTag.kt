package streetlight.model.data

import kampfire.model.Labeled
import koala.SvgFile
import koala.SvgPack

/** A talent a star offers, with its icon. */
enum class TalentTag(val svg: SvgPack, label: String? = null): Labeled {
    // Music
    Singer(SvgFile.MicrophoneHandheld),
    Rapper(SvgFile.Microphone),
    Songwriter(SvgFile.Writing),
    Composer(SvgFile.FileMusic),
    Band(SvgFile.UsersGroup),
    Choir(SvgFile.Social),
    Guitarist(SvgFile.AcousticGuitar),
    Pianist(SvgFile.Piano),
    Drummer(SvgFile.Drum),
    Violinist(SvgFile.Violin),
    Bassist(SvgFile.ElectricGuitar),
    Busker(SvgFile.MusicHeart),
    Dj(SvgFile.Vinyl, "DJ"),
    Producer(SvgFile.Headphones),
    SoundEngineer(SvgFile.AdjustmentsHorizontal, "Sound Engineer"),
    Conductor(SvgFile.Wand),
    Beatboxer(SvgFile.Beatbox),

    // Performance
    Actor(SvgFile.MasksTheater),
    Comedian(SvgFile.MoodCrazyHappy),
    Improviser(SvgFile.MoodWink),
    Dancer(SvgFile.Dance),
    Magician(SvgFile.TopHat),
    Juggler(SvgFile.Juggling),
    Acrobat(SvgFile.Tightrope),
    Clown(SvgFile.Clown),
    Mime(SvgFile.HandStop),
    FirePerformer(SvgFile.Fire, "Fire Performer"),
    DragPerformer(SvgFile.Crown, "Drag Performer"),
    CircusPerformer(SvgFile.BuildingCircus, "Circus Performer"),
    Poet(SvgFile.Feather),
    Storyteller(SvgFile.Book),
    Puppeteer(SvgFile.Marionette),
    Emcee(SvgFile.Microphone),
    Speaker(SvgFile.Presentation),
    Model(SvgFile.CameraSelfie),

    // Visual Arts
    Painter(SvgFile.Brush),
    Muralist(SvgFile.Spray),
    Illustrator(SvgFile.Pencil),
    Sculptor(SvgFile.Hammer),
    Photographer(SvgFile.Camera),
    Videographer(SvgFile.Video),
    Filmmaker(SvgFile.Movie),
    Animator(SvgFile.Movie),
    GraphicDesigner(SvgFile.Vector, "Graphic Designer"),
    TattooArtist(SvgFile.Needle, "Tattoo Artist"),
    FacePainter(SvgFile.MoodSmile, "Face Painter"),
    Caricaturist(SvgFile.Signature),
    Calligrapher(SvgFile.PenNib),

    // Crafts
    Potter(SvgFile.Bowl),
    Crafter(SvgFile.NeedleThread),
    Woodworker(SvgFile.Axe),
    Jeweler(SvgFile.Diamond),
    Florist(SvgFile.Flower),
    FashionDesigner(SvgFile.Hanger, "Fashion Designer"),
    BalloonArtist(SvgFile.Balloon, "Balloon Artist"),

    // Writing
    Writer(SvgFile.Writing),
    Journalist(SvgFile.News),
    Blogger(SvgFile.Article),
    Translator(SvgFile.Language),

    // Food
    Chef(SvgFile.ChefHat),
    Baker(SvgFile.Bread),
    Bartender(SvgFile.GlassCocktail),
    Barista(SvgFile.Coffee),
    Caterer(SvgFile.ToolsKitchen),
    Brewer(SvgFile.Beer),
    Sommelier(SvgFile.Glass),
    Pitmaster(SvgFile.Grill),
    FoodVendor(SvgFile.Truck, "Food Vendor"),

    // Technical
    SoundTech(SvgFile.Volume, "Sound Tech"),
    LightingTech(SvgFile.Bulb, "Lighting Tech"),
    Stagehand(SvgFile.Tool),
    Projectionist(SvgFile.DeviceProjector),
    Streamer(SvgFile.Broadcast),
    VideoEditor(SvgFile.Cut, "Video Editor"),
    WebDeveloper(SvgFile.Code, "Web Developer"),
    Developer(SvgFile.Terminal),
    ItSupport(SvgFile.DeviceDesktop, "IT Support"),
    Electrician(SvgFile.Bolt),
    Carpenter(SvgFile.Hammer),
    Mechanic(SvgFile.Tool),

    // Promotion
    Promoter(SvgFile.Speakerphone),
    SocialMedia(SvgFile.Share, "Social Media"),
    Marketer(SvgFile.ChartLine),
    Publicist(SvgFile.News),
    FlyerDesigner(SvgFile.FileText, "Flyer Designer"),
    StreetTeam(SvgFile.Walk, "Street Team"),
    Influencer(SvgFile.BrandInstagram),

    // Hosting
    Host(SvgFile.Home),
    EventPlanner(SvgFile.Calendar, "Event Planner"),
    Organizer(SvgFile.ClipboardList),
    Coordinator(SvgFile.Social),
    Usher(SvgFile.Ticket),
    Security(SvgFile.Shield),
    Volunteer(SvgFile.HeartHandshake),
    Fundraiser(SvgFile.PigMoney),
    TourGuide(SvgFile.Flag, "Tour Guide"),
    GameMaster(SvgFile.Dice, "Game Master"),
    TriviaHost(SvgFile.Question, "Trivia Host"),
    Vendor(SvgFile.BuildingStore),

    // Teaching & Care
    Teacher(SvgFile.School),
    Instructor(SvgFile.Chalkboard),
    Coach(SvgFile.ClipboardText),
    PersonalTrainer(SvgFile.Barbell, "Personal Trainer"),
    YogaInstructor(SvgFile.Yoga, "Yoga Instructor"),
    DanceInstructor(SvgFile.Dance, "Dance Instructor"),
    Childcare(SvgFile.ToyBlocks),
    MassageTherapist(SvgFile.Massage, "Massage Therapist"),
    TarotReader(SvgFile.Cards, "Tarot Reader");

    override val label = label ?: name
}

/** A group of [TalentTag]s, as a menu presents them. */
enum class TalentCategory(val tags: List<TalentTag>, label: String? = null): Labeled {
    Music(listOf(
        TalentTag.Singer, TalentTag.Rapper, TalentTag.Songwriter, TalentTag.Composer, TalentTag.Band,
        TalentTag.Choir, TalentTag.Guitarist, TalentTag.Pianist, TalentTag.Drummer, TalentTag.Violinist,
        TalentTag.Bassist, TalentTag.Busker, TalentTag.Dj, TalentTag.Producer, TalentTag.SoundEngineer,
        TalentTag.Conductor, TalentTag.Beatboxer,
    )),
    Performance(listOf(
        TalentTag.Actor, TalentTag.Comedian, TalentTag.Improviser, TalentTag.Dancer, TalentTag.Magician,
        TalentTag.Juggler, TalentTag.Acrobat, TalentTag.Clown, TalentTag.Mime, TalentTag.FirePerformer,
        TalentTag.DragPerformer, TalentTag.CircusPerformer, TalentTag.Poet, TalentTag.Storyteller,
        TalentTag.Puppeteer, TalentTag.Emcee, TalentTag.Speaker, TalentTag.Model,
    )),
    VisualArts(listOf(
        TalentTag.Painter, TalentTag.Muralist, TalentTag.Illustrator, TalentTag.Sculptor, TalentTag.Photographer,
        TalentTag.Videographer, TalentTag.Filmmaker, TalentTag.Animator, TalentTag.GraphicDesigner,
        TalentTag.TattooArtist, TalentTag.FacePainter, TalentTag.Caricaturist, TalentTag.Calligrapher,
    ), "Visual Arts"),
    Crafts(listOf(
        TalentTag.Potter, TalentTag.Crafter, TalentTag.Woodworker, TalentTag.Jeweler, TalentTag.Florist,
        TalentTag.FashionDesigner, TalentTag.BalloonArtist,
    )),
    Writing(listOf(
        TalentTag.Writer, TalentTag.Journalist, TalentTag.Blogger, TalentTag.Translator,
    )),
    Food(listOf(
        TalentTag.Chef, TalentTag.Baker, TalentTag.Bartender, TalentTag.Barista, TalentTag.Caterer, TalentTag.Brewer,
        TalentTag.Sommelier, TalentTag.Pitmaster, TalentTag.FoodVendor,
    )),
    Technical(listOf(
        TalentTag.SoundTech, TalentTag.LightingTech, TalentTag.Stagehand, TalentTag.Projectionist,
        TalentTag.Streamer, TalentTag.VideoEditor, TalentTag.WebDeveloper, TalentTag.Developer, TalentTag.ItSupport,
        TalentTag.Electrician, TalentTag.Carpenter, TalentTag.Mechanic,
    )),
    Promotion(listOf(
        TalentTag.Promoter, TalentTag.SocialMedia, TalentTag.Marketer, TalentTag.Publicist, TalentTag.FlyerDesigner,
        TalentTag.StreetTeam, TalentTag.Influencer,
    )),
    Hosting(listOf(
        TalentTag.Host, TalentTag.EventPlanner, TalentTag.Organizer, TalentTag.Coordinator, TalentTag.Usher,
        TalentTag.Security, TalentTag.Volunteer, TalentTag.Fundraiser, TalentTag.TourGuide, TalentTag.GameMaster,
        TalentTag.TriviaHost, TalentTag.Vendor,
    )),
    TeachingAndCare(listOf(
        TalentTag.Teacher, TalentTag.Instructor, TalentTag.Coach, TalentTag.PersonalTrainer,
        TalentTag.YogaInstructor, TalentTag.DanceInstructor, TalentTag.Childcare, TalentTag.MassageTherapist,
        TalentTag.TarotReader,
    ), "Teaching & Care");

    override val label = label ?: name
}
