package koala.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/** The flow mapped with [block], emitting only when the result changes. */
fun <T1, T2> Flow<T1>.dedup(block: suspend (T1) -> T2): Flow<T2> = map(block).distinctUntilChanged()
/** The flow mapped with [block], skipping `null` results and emitting only when the result changes. */
fun <T1, T2> Flow<T1>.dedupNotNull(block: suspend (T1) -> T2?): Flow<T2> = mapNotNull(block).distinctUntilChanged()

/** The flow mapped with [block], emitting only when the key from [provideKey] changes. */
fun <T1, T2, K> Flow<T1>.dedupBy(provideKey: (T1) -> K, block: suspend (T1) -> T2): Flow<T2> =
    distinctUntilChangedBy(provideKey).map(block)