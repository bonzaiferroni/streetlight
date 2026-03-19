package streetlight.web.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.EventInterest
import streetlight.web.io.ApiClient

class UserInterest(
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val _interestFlow = MutableSharedFlow<EventInterest>(1)
    val interestFlow: Flow<EventInterest> = _interestFlow

    fun editEventInterest(interest: EventInterest) {
        scope.launch {
            val isSuccess = api.editEventInterest(interest) ?: return@launch
            if (isSuccess) {
                _interestFlow.emit(interest)
            }
        }
    }
}