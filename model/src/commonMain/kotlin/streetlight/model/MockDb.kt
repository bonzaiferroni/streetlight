package streetlight.model

import kabinet.model.GeoPoint
import kabinet.model.User
import kabinet.model.UserId
import kabinet.model.UserRole
import kotlinx.datetime.Clock
import streetlight.model.data.Street
import streetlight.model.data.StreetId
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.EventStatus
import streetlight.model.data.EventType
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.model.data.amazingGrace
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

interface MockDb {
    val sparks: List<User>
    val streets: List<Street>
    val locations: List<Location>
    val events: List<Event>
    val songs: List<Song>
}

val mockDb by lazy {
    val now = Clock.System.now()
    val rng = Random(0)
    val sparkNameBag = ValueBag(
        rng,
        listOf(
            "Luke" to "wombat",
            "LaSheikh" to "TheTwinzMhg",
            "Soleil" to "SolsaX",
            "Amari" to "Indigo",
            "Peter" to "Rock"
        )
    )
    val sparks = (0..3).map {
        val name = sparkNameBag.draw()
        User(
            userId = UserId.random(),
            username = name.first,
            roles = setOf(UserRole.USER),
            avatarUrl = null,
            createdAt = now - 30.days,
            updatedAt = now - 1.days
        )
    }
    val areaNameBag = ValueBag(
        rng,
        listOf(
            "Aurora", "Denver", "Boulder",
            "Colorado Springs", "Fort Collins", "Pueblo",
            "Lakewood", "Thornton", "Arvada", "Westminster",
        )
    )
    val locationNameBag = ValueBag(
        rng,
        listOf(
            "Bob's Bar and Grill", "The Stampede", "Ham's Burgers",
            "The Bluebird", "City Park", "The Red Lion", "Mountain View Cafe",
            "Sunset Diner", "Lakeside Lounge", "The Green Dragon",
            "Riverfront Pub", "The Golden Goose", "Hilltop Tavern",
            "The Cozy Nook", "Downtown Eatery", "The Rustic Cabin",
            "The Jazz Club", "The Sports Bar", "The Wine Cellar",
        )
    )
    val streets = (0..3).map {
        Street(
            streetId = StreetId.random(),
            name = areaNameBag.draw(),
            points = emptyList(),
        )
    }
    val locations = streets.flatMap { area ->
        (0..3).map { index ->
            Location(
                locationId = LocationId.random(),
                userId = sparks[index].userId,
                streetId = area.streetId,
                name = locationNameBag.draw(),
                description = null,
                address = null,
                notes = null,
                geoPoint = GeoPoint(0.0, 0.0),
                resources = setOf(),
                updatedAt = Clock.System.now(),
                createdAt = Clock.System.now(),
            )
        }
    }
    val locationBag = ValueBag(rng, locations)
    val eventTitleBag = ValueBag(
        rng,
        listOf(
            "Jam Sesh", "Open Mic Night", "Monday @ The Pub",
            "Karaoke Fun", "Trivia Night", "Live Music Tonight",
            "Block Party", "Singing Circle", "Poetry Slam",
        )
    )
    val events = sparks.mapIndexed { index, spark ->
        Event(
            eventId = EventId.random(),
            locationId = locationBag.draw().locationId,
            userId = spark.userId,
            currentRequestId = null,
            url = null,
            imageUrl = null,
            streamUrl = null,
            title = eventTitleBag.draw(),
            description = null,
            status = EventStatus.Pending,
            eventType = EventType.StreetPerformance,
            cashTips = null,
            cardTips = null,
            startsAt = now,
            endsAt = now + 1.hours,
            updatedAt = now - 1.days,
            createdAt = now - 2.days
        )
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

    val songs = sparks.flatMap { spark ->
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
        override val sparks = sparks
        override val streets = streets
        override val locations = locations
        override val events = events
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
