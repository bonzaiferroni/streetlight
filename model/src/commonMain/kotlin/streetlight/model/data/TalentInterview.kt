package streetlight.model.data

/** The questions asked of a star who tags a talent with this, shared within its category. */
val TalentTag.interviewQuestions: List<String> get() = when (this) {
    // Music
    TalentTag.Singer, TalentTag.Rapper, TalentTag.Choir, TalentTag.Busker, TalentTag.Beatboxer ->
        listOf(MusicStyle, MusicVenues)
    TalentTag.Songwriter, TalentTag.Composer ->
        listOf(MusicStyle, "What have you written that you're proud of?", "Do you collaborate or write for others?")
    TalentTag.Band ->
        listOf(MusicStyle, "Who is in the band, and what do they play?", MusicVenues)
    TalentTag.Guitarist, TalentTag.Pianist, TalentTag.Drummer, TalentTag.Violinist, TalentTag.Bassist ->
        listOf(MusicStyle, "Do you play solo, in a group, or both?", GearQuestion)
    TalentTag.Dj, TalentTag.Producer ->
        listOf(MusicStyle, GearQuestion, MusicVenues)
    TalentTag.SoundEngineer, TalentTag.Conductor ->
        listOf(ExperienceQuestion, GearQuestion)

    // Performance
    TalentTag.Actor, TalentTag.Comedian, TalentTag.Improviser, TalentTag.Dancer, TalentTag.Magician,
    TalentTag.Juggler, TalentTag.Acrobat, TalentTag.Clown, TalentTag.Mime, TalentTag.FirePerformer,
    TalentTag.DragPerformer, TalentTag.CircusPerformer, TalentTag.Puppeteer ->
        listOf(PerformanceStyle, PerformanceNeeds, AudienceQuestion)
    TalentTag.Poet, TalentTag.Storyteller ->
        listOf(PerformanceStyle, AudienceQuestion)
    TalentTag.Emcee, TalentTag.Speaker ->
        listOf("What topics or events do you speak at?", AudienceQuestion)
    TalentTag.Model ->
        listOf(PortfolioQuestion, ExperienceQuestion)

    // Visual Arts
    TalentTag.Painter, TalentTag.Muralist, TalentTag.Illustrator, TalentTag.Sculptor, TalentTag.GraphicDesigner,
    TalentTag.Calligrapher ->
        listOf(ArtStyle, PortfolioQuestion, CommissionQuestion)
    TalentTag.Photographer, TalentTag.Videographer, TalentTag.Filmmaker, TalentTag.Animator ->
        listOf(ArtStyle, PortfolioQuestion, GearQuestion)
    TalentTag.TattooArtist ->
        listOf(ArtStyle, PortfolioQuestion, "Where do you work?")
    TalentTag.FacePainter, TalentTag.Caricaturist ->
        listOf(ArtStyle, AudienceQuestion, PerformanceNeeds)

    // Crafts
    TalentTag.Potter, TalentTag.Crafter, TalentTag.Woodworker, TalentTag.Jeweler, TalentTag.Florist,
    TalentTag.FashionDesigner ->
        listOf("What do you make?", CommissionQuestion, "Where can people find your work?")
    TalentTag.BalloonArtist ->
        listOf("What do you make?", AudienceQuestion, PerformanceNeeds)

    // Writing
    TalentTag.Writer, TalentTag.Journalist, TalentTag.Blogger ->
        listOf("What do you write about?", PortfolioQuestion)
    TalentTag.Translator ->
        listOf("Which languages do you work in?", "What kinds of text do you translate?")

    // Food
    TalentTag.Chef, TalentTag.Baker, TalentTag.Caterer, TalentTag.Pitmaster, TalentTag.FoodVendor ->
        listOf(FoodStyle, "How many people can you serve?", DietaryQuestion)
    TalentTag.Bartender, TalentTag.Barista, TalentTag.Brewer, TalentTag.Sommelier ->
        listOf(FoodStyle, ExperienceQuestion)

    // Technical
    TalentTag.SoundTech, TalentTag.LightingTech, TalentTag.Projectionist, TalentTag.Streamer ->
        listOf(ExperienceQuestion, GearQuestion)
    TalentTag.Stagehand, TalentTag.Electrician, TalentTag.Carpenter, TalentTag.Mechanic ->
        listOf(ExperienceQuestion, "What kinds of jobs do you take on?")
    TalentTag.VideoEditor ->
        listOf(PortfolioQuestion, "What software do you work in?")
    TalentTag.WebDeveloper, TalentTag.Developer ->
        listOf("What languages and tools do you work with?", PortfolioQuestion, HelpQuestion)
    TalentTag.ItSupport ->
        listOf("What do people usually come to you for?", HelpQuestion)

    // Promotion
    TalentTag.Promoter, TalentTag.Marketer, TalentTag.Publicist, TalentTag.StreetTeam ->
        listOf("What have you promoted?", AudienceQuestion)
    TalentTag.SocialMedia, TalentTag.Influencer ->
        listOf("Which platforms are you on?", AudienceQuestion)
    TalentTag.FlyerDesigner ->
        listOf(ArtStyle, PortfolioQuestion)

    // Hosting
    TalentTag.Host, TalentTag.EventPlanner, TalentTag.Organizer, TalentTag.Coordinator ->
        listOf("What kinds of events do you put together?", "How large an event can you handle?")
    TalentTag.Usher, TalentTag.Security, TalentTag.Volunteer ->
        listOf(ExperienceQuestion, AvailabilityQuestion)
    TalentTag.Fundraiser ->
        listOf("What causes have you raised money for?", ExperienceQuestion)
    TalentTag.TourGuide ->
        listOf("Where do you lead tours?", "What do your tours cover?")
    TalentTag.GameMaster, TalentTag.TriviaHost ->
        listOf("What games do you run?", AudienceQuestion)
    TalentTag.Vendor ->
        listOf("What do you sell?", PerformanceNeeds)

    // Teaching & Care
    TalentTag.Teacher, TalentTag.Instructor, TalentTag.Coach, TalentTag.PersonalTrainer,
    TalentTag.YogaInstructor, TalentTag.DanceInstructor ->
        listOf("What do you teach?", "Who are your students?", ExperienceQuestion)
    TalentTag.Childcare ->
        listOf("What ages do you care for?", ExperienceQuestion, AvailabilityQuestion)
    TalentTag.MassageTherapist, TalentTag.TarotReader ->
        listOf(ExperienceQuestion, PerformanceNeeds)
}

private const val MusicStyle = "What styles or genres do you play?"
private const val MusicVenues = "Where have you performed?"
private const val GearQuestion = "What equipment do you bring?"
private const val ExperienceQuestion = "How did you get started, and what have you worked on?"
private const val PerformanceStyle = "What is your act like?"
private const val PerformanceNeeds = "What do you need from a venue?"
private const val AudienceQuestion = "Who is your audience?"
private const val ArtStyle = "How would you describe your style?"
private const val PortfolioQuestion = "Where can people see your work?"
private const val CommissionQuestion = "Do you take commissions?"
private const val FoodStyle = "What do you make, and what is it known for?"
private const val DietaryQuestion = "Can you accommodate dietary needs?"
private const val HelpQuestion = "How can you help someone get set up on Streetlight?"
private const val AvailabilityQuestion = "When are you available?"
