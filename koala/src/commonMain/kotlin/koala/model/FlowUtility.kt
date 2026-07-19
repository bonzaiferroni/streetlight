package koala.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

fun <T1, T2> Flow<T1>.tap(block: suspend (T1) -> T2): Flow<T2> = map(block).distinctUntilChanged()
fun <T1, T2> Flow<T1>.tapNotNull(block: suspend (T1) -> T2?): Flow<T2> = mapNotNull(block).distinctUntilChanged()

fun <T1, T2, K> Flow<T1>.tapBy(provideKey: (T1) -> K, block: suspend (T1) -> T2): Flow<T2> =
    distinctUntilChangedBy(provideKey).map(block)