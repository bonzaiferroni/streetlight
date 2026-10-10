package streetlight.model.data

import koala.SvgFile
import koala.SvgPack

/** An icon for each kind of talent. */
enum class TalentSvg(val svg: SvgPack) {
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
    Dj(SvgFile.Vinyl),
    Producer(SvgFile.Headphones),
    SoundEngineer(SvgFile.AdjustmentsHorizontal),
    Conductor(SvgFile.Wand),
    Beatboxer(SvgFile.WaveSine),

    // Performance
    Actor(SvgFile.MasksTheater),
    Comedian(SvgFile.MoodCrazyHappy),
    Improviser(SvgFile.MoodWink),
    Dancer(SvgFile.Dance),
    Magician(SvgFile.Wand),
    Juggler(SvgFile.Juggling),
    Acrobat(SvgFile.Stretching),
    Clown(SvgFile.MoodTongue),
    Mime(SvgFile.HandStop),
    FirePerformer(SvgFile.Fire),
    DragPerformer(SvgFile.Crown),
    CircusPerformer(SvgFile.BuildingCircus),
    Poet(SvgFile.Feather),
    Storyteller(SvgFile.Book),
    Puppeteer(SvgFile.Ghost),
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
    GraphicDesigner(SvgFile.Vector),
    TattooArtist(SvgFile.Needle),
    FacePainter(SvgFile.MoodSmile),
    Caricaturist(SvgFile.Signature),
    Calligrapher(SvgFile.Signature),

    // Crafts
    Potter(SvgFile.Bowl),
    Crafter(SvgFile.NeedleThread),
    Woodworker(SvgFile.Axe),
    Jeweler(SvgFile.Diamond),
    Florist(SvgFile.Flower),
    FashionDesigner(SvgFile.Hanger),
    BalloonArtist(SvgFile.Balloon),

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
    FoodVendor(SvgFile.Truck),

    // Technical
    SoundTech(SvgFile.Volume),
    LightingTech(SvgFile.Bulb),
    Stagehand(SvgFile.Tool),
    Projectionist(SvgFile.DeviceProjector),
    Streamer(SvgFile.Broadcast),
    VideoEditor(SvgFile.Cut),
    WebDeveloper(SvgFile.Code),
    Developer(SvgFile.Terminal),
    ItSupport(SvgFile.DeviceDesktop),
    Electrician(SvgFile.Bolt),
    Carpenter(SvgFile.Hammer),
    Mechanic(SvgFile.Tool),

    // Promotion
    Promoter(SvgFile.Speakerphone),
    SocialMedia(SvgFile.Share),
    Marketer(SvgFile.ChartLine),
    Publicist(SvgFile.News),
    FlyerDesigner(SvgFile.FileText),
    StreetTeam(SvgFile.Walk),
    Influencer(SvgFile.BrandInstagram),

    // Hosting
    Host(SvgFile.Home),
    EventPlanner(SvgFile.Calendar),
    Organizer(SvgFile.ClipboardList),
    Coordinator(SvgFile.Social),
    Usher(SvgFile.Ticket),
    Security(SvgFile.Shield),
    Volunteer(SvgFile.HeartHandshake),
    Fundraiser(SvgFile.PigMoney),
    TourGuide(SvgFile.Flag),
    GameMaster(SvgFile.Dice),
    TriviaHost(SvgFile.Question),
    Vendor(SvgFile.BuildingStore),

    // Teaching & Care
    Teacher(SvgFile.School),
    Instructor(SvgFile.Chalkboard),
    Coach(SvgFile.ClipboardText),
    PersonalTrainer(SvgFile.Barbell),
    YogaInstructor(SvgFile.Yoga),
    DanceInstructor(SvgFile.Dance),
    Childcare(SvgFile.ToyBlocks),
    MassageTherapist(SvgFile.Massage),
    TarotReader(SvgFile.Cards)
}
