package streetlight.model

import kampfire.model.GeoPoint
import kampfire.model.User
import kampfire.model.UserId
import kampfire.model.UserRole
import kotlinx.datetime.Clock
import streetlight.model.data.Community
import streetlight.model.data.CommunityId
import streetlight.model.data.CommunityType
import streetlight.model.data.DefaultEventTag
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.EventStatus
import streetlight.model.data.EventTag
import streetlight.model.data.EventType
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.model.data.amazingGrace
import streetlight.model.utils.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

interface MockDb {
    val users: List<User>
    val communities: List<Community>
    val locations: List<Location>
    val events: List<Event>
    val eventTags: List<EventTag>
    val songs: List<Song>
}

val mockDb by lazy {
    val now = Clock.System.now()
    val rng = Random(0)

    fun userOf(username: String) = User(
        userId = UserId.random(),
        username = username,
        roles = setOf(UserRole.USER),
        avatarUrl = null,
        createdAt = now - 30.days,
        updatedAt = now - 1.days
    )

    val users = object  {
        val Luke = userOf("wombat")
        val Davis = userOf("eyy123")
        val Ryder = userOf("ryderman")
        val Clover = userOf("cloverleaf")

        val list = listOf(Luke, Davis, Ryder, Clover)
        val bag = ValueBag(rng, list)
    }

    fun areaOf(name: String, communityType: CommunityType) = Community(
        communityId = CommunityId.random(),
        name = name,
        points = emptyList(),
        communityType = communityType
    )

    val areas = object {
        val colfax = areaOf("Colfax", CommunityType.Street)

        val list = listOf(colfax)
    }

    fun locationOf(
        name: String,
        point: GeoPoint,
    ) = Location(
        locationId = LocationId.random(),
        name = name,
        description = null,
        address = null,
        geoPoint = point,
        resources = emptySet(),
        website = null,
        eventsUrl = null,
        aboutUrl = null,
        menuUrl = null,
        imageUrl = null,
        thumbUrl = null,
        updatedAt = now - 10.days,
        createdAt = now - 30.days,
    )

    val locations = object {
        // Latitude: 39.739927 | Longitude: -104.956665
        val tatteredCover = locationOf("The Tattered Cover", GeoPoint(-104.956665, 39.739927))
        // Latitude: 39.740384 | Longitude: -104.959337
        val masKitchen = locationOf("Ma's Kitchen", GeoPoint(-104.959337, 39.740384))
        // Latitude: 39.740813 | Longitude: -104.958219
        val carlaMadison = locationOf("Carla Madison Recreation Center", GeoPoint(-104.958219, 39.740813))
        // Latitude: 39.740142 | Longitude: -104.975323
        val ogden = locationOf("The Ogden", GeoPoint(-104.975323, 39.740142))
        // Latitude: 39.739243 | Longitude: -104.985476
        val capital = locationOf("State Capital", GeoPoint(-104.985476, 39.739243))
        // Latitude: 39.739232 | Longitude: -104.988752
        val civicCenterPark = locationOf("Civic Center Park", GeoPoint(-104.988752, 39.739232))

        val list = listOf(
            tatteredCover,
            masKitchen,
            carlaMadison,
            ogden,
            capital,
            civicCenterPark
        )

        val bag = ValueBag(rng, list)
    }

    fun eventOf(title: String, eventType: EventType, tags: List<EventTag>) = Event(
        eventId = EventId.random(),
        locationId = locations.bag.draw().locationId,
        userId = users.bag.draw().userId,
        currentRequestId = null,
        title = title,
        description = null,
        status = EventStatus.Pending,
        eventType = eventType,
        contact = null,
        invitation = null,
        ageMin = null,
        cost = null,
        visibility = null,
        url = null,
        sourceUrl = null,
        sourceImageUrl = null,
        imageUrl = null,
        thumbUrl = null,
        streamUrl = null,
        date = now.toLocalDateTime().date,
        startsAt = now,
        endsAt = now + 1.hours,
        updatedAt = now - 1.days,
        createdAt = now - 2.days
    )
    data class EventDetails(val title: String, val eventType: EventType, val tags: List<EventTag>)
    val events = object {
        val jamSesh = eventOf("Jam Sesh", EventType.Meet, listOf(DefaultEventTag.jamCircle))
        val openMic = eventOf("Open Mic Night", EventType.Meet, listOf(DefaultEventTag.openMic))
        val liveMusic = eventOf("Monday @ The Pub", EventType.Show, listOf(DefaultEventTag.liveMusic))
        val karaoke = eventOf("Sing Your Heart Out", EventType.Meet, listOf(DefaultEventTag.karaoke))
        val gaming = eventOf("Trivia Night", EventType.Meet, listOf(DefaultEventTag.gaming))
        val blockParty = eventOf("Block Party", EventType.Meet, listOf(DefaultEventTag.party, DefaultEventTag.potluck))
        val singingCircle = eventOf("Singing Circle", EventType.Meet, listOf(DefaultEventTag.jamCircle))
        val poetrySlam = eventOf("Poetry Slam", EventType.Meet, listOf(DefaultEventTag.special))
        val tacoNight = eventOf("Taco Night", EventType.Food, listOf(DefaultEventTag.dinner))
        val political = eventOf("School Board Review", EventType.Meet, listOf(DefaultEventTag.political))
        val potluck = eventOf("Afternoon Potluck", EventType.Meet, listOf(DefaultEventTag.potluck, DefaultEventTag.appetizers))

        val list = listOf(
            jamSesh,
            openMic,
            liveMusic,
            karaoke,
            gaming,
            blockParty,
            singingCircle,
            poetrySlam,
            tacoNight,
            political,
            potluck
        )

        val bag = ValueBag(rng, list)
    }

    val songTitleBag = ValueBag(
        rng,
        listOf(
            "Redemption Song" to "Bob Marley",
            "99 Red Balloons" to "Nena",
            "Mekong" to "The Refreshments",
            "New Slang" to "The Shins",
            "Electric Feel" to "MGMT",
            "No Rain" to "Blind Melon",
            "Dog Days Are Over" to "Florence + The Machine",
            "Take Me Out" to "Franz Ferdinand",
            "Pumped Up Kicks" to "Foster The People",
            "Young Folks" to "Peter Bjorn and John",
            "Kids" to "MGMT",
            "Float On" to "Modest Mouse",
            "Tongue Tied" to "Grouplove",
            "A-Punk" to "Vampire Weekend",
            "Oxford Comma" to "Vampire Weekend",
            "Sweet Disposition" to "The Temper Trap",
            "Time to Pretend" to "MGMT",
            "Reptilia" to "The Strokes",
            "Last Nite" to "The Strokes",
            "Someday" to "The Strokes",
            "All These Things" to "The Mamas & The Papas",
        )
    )

    val songs = users.list.flatMap { spark ->
        (0..3).map {
            val (title, artist) = songTitleBag.draw()
            Song(
                songId = SongId.random(),
                userId = spark.userId,
                title = title,
                artist = artist,
                notation = amazingGrace,
                tempo = listOf(90, 100, 110, 120).random(),
                capo = listOf(0, 3, 4, 7, null).random(),
                inRotation = true,
                updatedAt = now,
                createdAt = now,
            )
        }
    }

    object : MockDb {
        override val users = users.list
        override val communities = areas.list
        override val locations = locations.list
        override val events = events.list
        override val eventTags = DefaultEventTag.list
        override val songs = songs
    }
}

class ValueBag<T>(
    private val rng: Random,
    private val values: List<T>
) {
    private val items: MutableList<T> = values.toMutableList()

    fun draw(): T {
        if (items.isEmpty()) {
            items.addAll(values)
        }
        val i = rng.nextInt(items.size)
        return items.removeAt(i)
    }

    val size: Int get() = items.size
    fun isEmpty(): Boolean = items.isEmpty()
}
