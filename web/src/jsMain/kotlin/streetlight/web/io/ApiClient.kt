package streetlight.web.io

interface ApiClient {
    val accountAction: AccountActionClient
    val city: CityClient
    val content: ContentClient
    val doc: DocClient
    val event: EventClient
    val feedback: FeedbackClient
    val bug: BugClient
    val galaxy: GalaxyClient
    val location: LocationClient
    val media: MediaClient
    val message: MessageClient
    val omniLog: OmniLogClient
    val post: PostClient
    val song: SongClient
    val star: StarClient
    val status: StatusClient
    val talk: TalkClient
    val task: TaskClient
    val user: UserClient
}

class BrowserApiClient(private val client: FetchClient): ApiClient {
    override val accountAction: AccountActionClient = BrowserAccountActionClient(client)
    override val city: CityClient = BrowserCityClient(client)
    override val content: ContentClient = BrowserContentClient(client)
    override val doc: DocClient = BrowserDocClient(client)
    override val event: EventClient = BrowserEventClient(client)
    override val feedback: FeedbackClient = BrowserFeedbackClient(client)
    override val bug: BugClient = BrowserBugClient(client)
    override val galaxy: GalaxyClient = BrowserGalaxyClient(client)
    override val location: LocationClient = BrowserLocationClient(client)
    override val media: MediaClient = BrowserMediaClient(client)
    override val message: MessageClient = BrowserMessageClient(client)
    override val omniLog: OmniLogClient = BrowserOmniLogClient(client)
    override val post: PostClient = BrowserPostClient(client)
    override val song: SongClient = BrowserSongClient(client)
    override val star: StarClient = BrowserStarClient(client)
    override val status: StatusClient = BrowserStatusClient(client)
    override val talk: TalkClient = BrowserTalkClient(client)
    override val task: TaskClient = BrowserTaskClient(client)
    override val user: UserClient = BrowserUserClient(client)
}
