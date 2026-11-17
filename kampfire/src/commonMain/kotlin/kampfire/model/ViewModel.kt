package kampfire.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class ViewModel(protected val scope: CoroutineScope) {

    protected fun launch(block: suspend CoroutineScope.() -> Unit) {
//        scope.launch(this::class.simpleName!!) {
//            block()
//        }
    }
}