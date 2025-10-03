package streetlight.model.data

import kabinet.model.UserId
import kabinet.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Spark(
    val sparkId: SparkId,
    val userId: UserId,
    val venmo: String,
    val stageName: String,
)

@JvmInline @Serializable
value class SparkId(override val value: String): ProjectId {
    companion object { fun random() = SparkId(randomUuidString()) }
}
