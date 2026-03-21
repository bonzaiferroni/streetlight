package streetlight.web.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.EventStar
import streetlight.web.io.ApiClient

class UserInterest(
    private val scope: CoroutineScope,
    private val api: ApiClient,
) {
    private val _interestFlow = MutableSharedFlow<EventStar>(1)
    val interestFlow: Flow<EventStar> = _interestFlow

    fun editEventInterest(interest: EventStar) {
        scope.launch {
            val isSuccess = api.editEventInterest(interest) ?: return@launch
            if (isSuccess) {
                _interestFlow.emit(interest)
            }
        }
    }
}