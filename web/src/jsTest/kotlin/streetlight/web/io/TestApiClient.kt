package streetlight.web.io

class TestApiClient(
    override val accountAction: AccountActionClient = TestAccountActionClient(),
    override val bug: BugClient = TestBugClient(),
    override val city: CityClient = TestCityClient(),
    override val content: ContentClient = TestContentClient(),
    override val doc: DocClient = TestDocClient(),
    override val event: EventClient = TestEventClient(),
    override val feedback: FeedbackClient = TestFeedbackClient(),
    override val galaxy: GalaxyClient = TestGalaxyClient(),
    override val location: LocationClient = TestLocationClient(),
    override val media: MediaClient = TestMediaClient(),
    override val message: MessageClient = TestMessageClient(),
    override val omniLog: OmniLogClient = TestOmniLogClient(),
    override val post: PostClient = TestPostClient(),
    override val song: SongClient = TestSongClient(),
    override val star: StarClient = TestStarClient(),
    override val status: StatusClient = TestStatusClient(),
    override val talk: TalkClient = TestTalkClient(),
    override val task: TaskClient = TestTaskClient(),
    override val user: UserClient = TestUserClient(),
): ApiClient
