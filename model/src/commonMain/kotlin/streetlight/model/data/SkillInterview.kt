package streetlight.model.data

/** The questions asked of a star who tags a skill with this, shared within its category. */
val SkillTag.interviewQuestions: List<String> get() = when (this) {
    // Music
    SkillTag.Singer, SkillTag.Rapper, SkillTag.Choir, SkillTag.Busker, SkillTag.Beatboxer ->
        listOf(MusicStyle, MusicVenues)
    SkillTag.Songwriter, SkillTag.Composer ->
        listOf(MusicStyle, "What have you written that you're proud of?", "Do you collaborate or write for others?")
    SkillTag.Guitarist, SkillTag.Pianist, SkillTag.Drummer, SkillTag.Violinist, SkillTag.Bassist ->
        listOf(MusicStyle, "Do you play solo, in a group, or both?", GearQuestion)
    SkillTag.Dj, SkillTag.Producer ->
        listOf(MusicStyle, GearQuestion, MusicVenues)
    SkillTag.SoundEngineer ->
        listOf(ExperienceQuestion, GearQuestion)

    // Performance
    SkillTag.Actor, SkillTag.Comedian, SkillTag.Improviser, SkillTag.Dancer, SkillTag.Magician,
    SkillTag.Juggler, SkillTag.Acrobat, SkillTag.Clown, SkillTag.Mime, SkillTag.FirePerformer,
    SkillTag.Puppeteer ->
        listOf(PerformanceStyle, PerformanceNeeds, AudienceQuestion)
    SkillTag.Poet, SkillTag.Storyteller ->
        listOf(PerformanceStyle, AudienceQuestion)
    SkillTag.Emcee, SkillTag.Speaker ->
        listOf("What topics or events do you speak at?", AudienceQuestion)
    SkillTag.Model ->
        listOf(PortfolioQuestion, ExperienceQuestion)

    // Visual Arts
    SkillTag.Painter, SkillTag.Muralist, SkillTag.Illustrator, SkillTag.Sculptor, SkillTag.GraphicDesigner,
    SkillTag.Calligrapher ->
        listOf(ArtStyle, PortfolioQuestion, CommissionQuestion)
    SkillTag.Photographer, SkillTag.Videographer, SkillTag.Filmmaker, SkillTag.Animator ->
        listOf(ArtStyle, PortfolioQuestion, GearQuestion)
    SkillTag.TattooArtist ->
        listOf(ArtStyle, PortfolioQuestion, "Where do you work?")
    SkillTag.FacePainter, SkillTag.Caricaturist ->
        listOf(ArtStyle, AudienceQuestion, PerformanceNeeds)

    // Crafts
    SkillTag.Potter, SkillTag.Crafter, SkillTag.Woodworker, SkillTag.Jeweler, SkillTag.Florist,
    SkillTag.FashionDesigner ->
        listOf("What do you make?", CommissionQuestion, "Where can people find your work?")
    SkillTag.BalloonArtist ->
        listOf("What do you make?", AudienceQuestion, PerformanceNeeds)

    // Writing
    SkillTag.Writer, SkillTag.Journalist, SkillTag.Blogger ->
        listOf("What do you write about?", PortfolioQuestion)
    SkillTag.Translator ->
        listOf("Which languages do you work in?", "What kinds of text do you translate?")

    // Food
    SkillTag.Chef, SkillTag.Baker, SkillTag.Caterer, SkillTag.Pitmaster, SkillTag.FoodVendor ->
        listOf(FoodStyle, "How many people can you serve?", DietaryQuestion)
    SkillTag.Bartender, SkillTag.Barista, SkillTag.Brewer, SkillTag.Sommelier ->
        listOf(FoodStyle, ExperienceQuestion)

    // Technical
    SkillTag.SoundTech, SkillTag.LightingTech, SkillTag.Projectionist, SkillTag.Streamer ->
        listOf(ExperienceQuestion, GearQuestion)
    SkillTag.Stagehand, SkillTag.Electrician, SkillTag.Carpenter, SkillTag.Mechanic ->
        listOf(ExperienceQuestion, "What kinds of jobs do you take on?")
    SkillTag.VideoEditor ->
        listOf(PortfolioQuestion, "What software do you work in?")
    SkillTag.WebDeveloper, SkillTag.Developer ->
        listOf("What languages and tools do you work with?", PortfolioQuestion, HelpQuestion)
    SkillTag.ItSupport ->
        listOf("What do people usually come to you for?", HelpQuestion)

    // Promotion
    SkillTag.Promoter, SkillTag.Marketer, SkillTag.Publicist, SkillTag.StreetTeam ->
        listOf("What have you promoted?", AudienceQuestion)
    SkillTag.SocialMedia, SkillTag.Influencer ->
        listOf("Which platforms are you on?", AudienceQuestion)
    SkillTag.FlyerDesigner ->
        listOf(ArtStyle, PortfolioQuestion)

    // Hosting
    SkillTag.Host, SkillTag.EventPlanner, SkillTag.Organizer, SkillTag.Coordinator ->
        listOf("What kinds of events do you put together?", "How large an event can you handle?")
    SkillTag.Usher, SkillTag.Security ->
        listOf(ExperienceQuestion, AvailabilityQuestion)
    SkillTag.Fundraiser ->
        listOf("What causes have you raised money for?", ExperienceQuestion)
    SkillTag.TourGuide ->
        listOf("Where do you lead tours?", "What do your tours cover?")
    SkillTag.GameMaster, SkillTag.TriviaHost ->
        listOf("What games do you run?", AudienceQuestion)
    SkillTag.Vendor ->
        listOf("What do you sell?", PerformanceNeeds)

    // Teaching & Care
    SkillTag.Teacher, SkillTag.Instructor, SkillTag.Coach, SkillTag.PersonalTrainer,
    SkillTag.YogaInstructor, SkillTag.DanceInstructor ->
        listOf("What do you teach?", "Who are your students?", ExperienceQuestion)
    SkillTag.Childcare ->
        listOf("What ages do you care for?", ExperienceQuestion, AvailabilityQuestion)
    SkillTag.MassageTherapist, SkillTag.TarotReader ->
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
