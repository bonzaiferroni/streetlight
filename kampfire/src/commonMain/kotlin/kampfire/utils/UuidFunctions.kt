@file:OptIn(ExperimentalUuidApi::class)

package kampfire.utils

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun randomUuidString() = Uuid.random().toStringId()

fun Uuid.toStringId() = this.toString()

fun Uuid.Companion.fromStringId(stringId: String) = Uuid.parse(stringId)

fun String.toUuid() = Uuid.parse(this)

// fun Uuid.toLongPair(): Pair<Long, Long> = toLongs { msb, lsb -> (msb to lsb) }
//
//fun Pair<Long, Long>.toUuid(): Uuid = Uuid.fromLongs(first, second)

// fun Uuid.toStringId() = toLongPair().let { "${it.first.toBase62()}-${it.second.toBase62()}" }

//fun Uuid.Companion.fromStringId(stringId: String) = stringId.split("-")
//    .let { it[0].fromBase62() to it[1].fromBase62() }
//    .toUuid()