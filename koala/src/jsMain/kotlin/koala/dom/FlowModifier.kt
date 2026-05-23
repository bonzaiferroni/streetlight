package koala.dom

import kampfire.model.ValidityCheck
import koala.css.DisplayNone
import koala.css.Valid
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement

fun HTMLElement.flowVisibility(isVisibleFlow: Flow<Boolean>, scope: CoroutineScope) {
    scope.launch {
        isVisibleFlow.collect { isVisible ->
            if (isVisible && !isModified(DisplayNone) || !isVisible && isModified(DisplayNone)) return@collect
            document.startViewTransition {
                when (isVisible) {
                    true -> unmodify(DisplayNone)
                    false -> modify(DisplayNone)
                }
            }
        }
    }
}

fun HTMLElement.flowValid(key: String, check: Flow<ValidityCheck>, scope: CoroutineScope) {
    scope.launch {
        check.collect { check ->
            when (check.invalidParts.contains(key)) {
                true -> unmodify(Valid)
                false -> modify(Valid)
            }
        }
    }
}