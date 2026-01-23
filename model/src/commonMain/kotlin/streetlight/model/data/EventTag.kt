package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class EventTag(
    val eventTagId: EventTagId,
    val name: String,
    val description: String? = null
)

@Serializable
@JvmInline
value class EventTagId(val value: String) {
    companion object {
        fun fromName(name: String) = EventTagId(name.lowercase().replace(" ", "-"))
    }
}

fun eventTagOf(name: String) = EventTag(
    eventTagId = EventTagId.fromName(name),
    name = name
)

object DefaultEventTag {
    val streetPerformance = eventTagOf("Street Performance")
    val liveMusic = eventTagOf("Live Music")
    val openMic = eventTagOf("Open Mic")
    val potluck = eventTagOf("Potluck")
    val jamCircle = eventTagOf("Jam Circle")
    val foodStand = eventTagOf("Food Stand")
    val foodTruck = eventTagOf("Food Truck")
    val dineIn = eventTagOf("Dine In")
    val hangout = eventTagOf("Hangout")
    val karaoke = eventTagOf("Karaoke")
    val gaming = eventTagOf("Gaming")
    val party = eventTagOf("Party")
    val special = eventTagOf("Special")
    val dinner = eventTagOf("Dinner")
    val lunch = eventTagOf("Lunch")
    val breakfast = eventTagOf("Breakfast")
    val appetizers = eventTagOf("Appetizers")
    val political = eventTagOf("Political")

    val list = listOf(
        streetPerformance,
        liveMusic,
        openMic,
        potluck,
        jamCircle,
        foodStand,
        hangout,
        karaoke,
        gaming,
        party,
        special,
        dinner,
        lunch,
        breakfast,
        appetizers,
        political
    )
}