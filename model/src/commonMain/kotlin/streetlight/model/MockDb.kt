package streetlight.model

import kabinet.model.GeoPoint
import kabinet.model.UserRole
import kotlinx.datetime.Clock
import streetlight.model.data.Area
import streetlight.model.data.AreaId
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.EventStatus
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.Song
import streetlight.model.data.SongId
import streetlight.model.data.Spark
import streetlight.model.data.SparkId
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

interface MockDb {
    val sparks: List<Spark>
    val areas: List<Area>
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
        Spark(
            userId = SparkId.random(),
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
    val areas = (0..3).map {
        Area(
            areaId = AreaId.random(),
            name = areaNameBag.draw()
        )
    }
    val locations = areas.flatMap { area ->
        (0..3).map { index ->
            Location(
                locationId = LocationId.random(),
                userId = sparks[index].userId,
                areaId = area.areaId,
                name = locationNameBag.draw(),
                description = null,
                address = null,
                notes = null,
                geoPoint = GeoPoint(0.0, 0.0),
                resources = setOf()
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
            "Redemption Song", "99 Red Balloons", "Mekong", "New Slang",
            "Electric Feel", "No Rain", "Dog Days Are Over", "Take Me Out",
            "Pumped Up Kicks", "Young Folks", "Kids", "Float On", "Tongue Tied",
            "A-Punk", "Oxford Comma", "Sweet Disposition", "Time to Pretend",
            "Reptilia", "Last Nite", "Someday", "All These Things",
        )
    )

    val songs = sparks.flatMap { spark ->
        (0..3).map {
            Song(
                songId = SongId.random(),
                userId = spark.userId,
                title = songTitleBag.draw(),
                artist = null,
                music = null,
            )
        }
    }

    object : MockDb {
        override val sparks = sparks
        override val areas = areas
        override val locations = locations
        override val events = events
        override val songs = songs
    }
}

private class ValueBag<T>(
    private val rng: Random,
    values: List<T>
) {
    private val items: MutableList<T> = values.toMutableList()

    fun draw(): T {
        if (items.isEmpty()) error("${this::class.simpleName} value bag is empty")
        val i = rng.nextInt(items.size)
        return items.removeAt(i)
    }

    val size: Int get() = items.size
    fun isEmpty(): Boolean = items.isEmpty()
}
