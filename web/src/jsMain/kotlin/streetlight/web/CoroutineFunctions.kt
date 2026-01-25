package streetlight.web

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map

fun <T1, T2> Flow<T1>.mapDistinct(block: (T1) -> T2): Flow<T2> = map(block).distinctUntilChanged()

fun <T1, T2, K> Flow<T1>.mapDistinctBy(provideKey: (T1) -> K, block: (T1) -> T2): Flow<T2> =
    distinctUntilChangedBy(provideKey).map(block)