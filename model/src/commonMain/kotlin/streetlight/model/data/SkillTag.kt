package streetlight.model.data

import kampfire.model.Labeled
import koala.SvgFile
import koala.SvgPack

/** A skill a star offers, with its icon. */
enum class SkillTag(val svg: SvgPack, label: String? = null): Labeled {
    // Music
    Singer(SvgFile.MicrophoneHandheld),
    Rapper(SvgFile.Microphone),
    Songwriter(SvgFile.Writing),
    Composer(SvgFile.FileMusic),
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
    Poet(SvgFile.Feather),
    Storyteller(SvgFile.Book),
    Puppeteer(SvgFile.Marionette),
    Emcee(SvgFile.Microphone),
    Speaker(SvgFile.Lectern),
    Model(SvgFile.ModelPose),

    // Visual Arts
    Painter(SvgFile.Brush),
    Muralist(SvgFile.Palette),
    Illustrator(SvgFile.Pencil),
    Sculptor(SvgFile.Hammer),
    Photographer(SvgFile.Camera),
    Videographer(SvgFile.Video),
    Filmmaker(SvgFile.Movie),
    Animator(SvgFile.Movie),
    GraphicDesigner(SvgFile.Vector, "Graphic Designer"),
    TattooArtist(SvgFile.Needle, "Tattoo Artist"),
    FacePainter(SvgFile.MoodSmile, "Face Painter"),
    Caricaturist(SvgFile.Caricature),
    Calligrapher(SvgFile.PenNib),

    // Crafts
    Potter(SvgFile.Bowl),
    Crafter(SvgFile.NeedleThread),
    Woodworker(SvgFile.Axe),
    Jeweler(SvgFile.Diamond),
    Florist(SvgFile.Bouquet),
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

/** A group of [SkillTag]s, as a menu presents them. */
enum class SkillCategory(val tags: List<SkillTag>, label: String? = null): Labeled {
    Music(listOf(
        SkillTag.Singer, SkillTag.Rapper, SkillTag.Songwriter, SkillTag.Composer,
        SkillTag.Choir, SkillTag.Guitarist, SkillTag.Pianist, SkillTag.Drummer, SkillTag.Violinist,
        SkillTag.Bassist, SkillTag.Busker, SkillTag.Dj, SkillTag.Producer, SkillTag.SoundEngineer,
        SkillTag.Beatboxer,
    )),
    Performance(listOf(
        SkillTag.Actor, SkillTag.Comedian, SkillTag.Improviser, SkillTag.Dancer, SkillTag.Magician,
        SkillTag.Juggler, SkillTag.Acrobat, SkillTag.Clown, SkillTag.Mime, SkillTag.FirePerformer,
        SkillTag.Poet, SkillTag.Storyteller,
        SkillTag.Puppeteer, SkillTag.Emcee, SkillTag.Speaker, SkillTag.Model,
    )),
    VisualArts(listOf(
        SkillTag.Painter, SkillTag.Muralist, SkillTag.Illustrator, SkillTag.Sculptor, SkillTag.Photographer,
        SkillTag.Videographer, SkillTag.Filmmaker, SkillTag.Animator, SkillTag.GraphicDesigner,
        SkillTag.TattooArtist, SkillTag.FacePainter, SkillTag.Caricaturist, SkillTag.Calligrapher,
    ), "Visual Arts"),
    Crafts(listOf(
        SkillTag.Potter, SkillTag.Crafter, SkillTag.Woodworker, SkillTag.Jeweler, SkillTag.Florist,
        SkillTag.FashionDesigner, SkillTag.BalloonArtist,
    )),
    Writing(listOf(
        SkillTag.Writer, SkillTag.Journalist, SkillTag.Blogger, SkillTag.Translator,
    )),
    Food(listOf(
        SkillTag.Chef, SkillTag.Baker, SkillTag.Bartender, SkillTag.Barista, SkillTag.Caterer, SkillTag.Brewer,
        SkillTag.Sommelier, SkillTag.Pitmaster, SkillTag.FoodVendor,
    )),
    Technical(listOf(
        SkillTag.SoundTech, SkillTag.LightingTech, SkillTag.Stagehand, SkillTag.Projectionist,
        SkillTag.Streamer, SkillTag.VideoEditor, SkillTag.WebDeveloper, SkillTag.Developer, SkillTag.ItSupport,
        SkillTag.Electrician, SkillTag.Carpenter, SkillTag.Mechanic,
    )),
    Promotion(listOf(
        SkillTag.Promoter, SkillTag.SocialMedia, SkillTag.Marketer, SkillTag.Publicist, SkillTag.FlyerDesigner,
        SkillTag.StreetTeam, SkillTag.Influencer,
    )),
    Hosting(listOf(
        SkillTag.Host, SkillTag.EventPlanner, SkillTag.Organizer, SkillTag.Coordinator, SkillTag.Usher,
        SkillTag.Security, SkillTag.Fundraiser, SkillTag.TourGuide, SkillTag.GameMaster,
        SkillTag.TriviaHost, SkillTag.Vendor,
    )),
    TeachingAndCare(listOf(
        SkillTag.Teacher, SkillTag.Instructor, SkillTag.Coach, SkillTag.PersonalTrainer,
        SkillTag.YogaInstructor, SkillTag.DanceInstructor, SkillTag.Childcare, SkillTag.MassageTherapist,
        SkillTag.TarotReader,
    ), "Teaching & Care");

    override val label = label ?: name
}
