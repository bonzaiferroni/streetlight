package streetlight.model.data

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** How often a recurring event repeats. */
@Serializable
sealed interface RepeatInterval {

    @Serializable
    @SerialName("daily")
    data class Daily(val every: Int = 1) : RepeatInterval

    @Serializable
    @SerialName("weekly")
    data class Weekly(
        val every: Int = 1,
        val days: Set<DayOfWeek>
    ) : RepeatInterval

    @Serializable
    @SerialName("monthly")
    data class Monthly(
        val every: Int = 1,
        val dayOfMonth: Int
    ) : RepeatInterval

    @Serializable
    @SerialName("monthly_weekday")
    data class MonthlyWeekday(
        val every: Int = 1,
        val ordinal: WeekOrdinal,
        val dayOfWeek: DayOfWeek
    ) : RepeatInterval

//    @Serializable
//    @SerialName("annually")
//    data class Annually(
//        val every: Int = 1,
//        val month: Month,
//        val dayOfMonth: Int
//    ) : Repeat
//
//    @Serializable
//    @SerialName("annual_weekday")
//    data class AnnualWeekday(
//        val every: Int = 1,
//        val month: Month,
//        val ordinal: WeekOrdinal,
//        val dayOfWeek: DayOfWeek
//    ) : Repeat
}

/** Which week of the month a monthly event falls in. */
@Serializable
enum class WeekOrdinal {
    First,
    Second,
    Third,
    Fourth,
    Last
}