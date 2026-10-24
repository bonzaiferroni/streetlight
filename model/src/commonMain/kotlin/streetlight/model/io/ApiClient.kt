package streetlight.model.io

import kampfire.api.ApiTransport

/** The Streetlight API over [transport], as one client per model, as `api.city`. It mirrors `DaoFacade` on the server. */
class ApiClient(transport: ApiTransport) {
    val accountAction = AccountActionClient(transport)
    val city = CityClient(transport)
    val content = ContentClient(transport)
    val doc = DocClient(transport)
    val earth = EarthClient(transport)
    val entity = EntityClient(transport)
    val event = EventClient(transport)
    val feed = FeedClient(transport)
    val feedback = FeedbackClient(transport)
    val bug = BugClient(transport)
    val galaxy = GalaxyClient(transport)
    val location = LocationClient(transport)
    val media = MediaClient(transport)
    val message = MessageClient(transport)
    val omniLog = OmniLogClient(transport)
    val post = PostClient(transport)
    val seller = SellerClient(transport)
    val offering = OfferingClient(transport)
    val skill = SkillClient(transport)
    val payment = PaymentClient(transport)
    val customer = CustomerClient(transport)
    val song = SongClient(transport)
    val star = StarClient(transport)
    val status = StatusClient(transport)
    val talk = TalkClient(transport)
    val task = TaskClient(transport)
    val user = UserClient(transport)
}
