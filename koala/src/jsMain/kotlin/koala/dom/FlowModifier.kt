package koala.dom

import kampfire.model.ValidityCheck
import koala.css.DisplayNone
import koala.css.Required
import koala.css.Valid
import koala.css.VisibilityHidden
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement

fun HTMLElement.flowDisplay(isDisplayedFlow: Flow<Boolean>, scope: CoroutineScope): HTMLElement {
    scope.launch {
        isDisplayedFlow.collect { isDisplayed ->
            if (isDisplayed && !isModified(DisplayNone) || !isDisplayed && isModified(DisplayNone)) return@collect
            document.startViewTransition {
                when (isDisplayed) {
                    true -> unmodify(DisplayNone)
                    false -> modify(DisplayNone)
                }
            }
        }
    }
    return this
}

fun HTMLElement.flowVisibility(isVisibleFlow: Flow<Boolean>, scope: CoroutineScope): HTMLElement {
    scope.launch {
        isVisibleFlow.collect { isVisible ->
            if (isVisible && !isModified(VisibilityHidden) || !isVisible && isModified(VisibilityHidden)) return@collect
            document.startViewTransition {
                when (isVisible) {
                    true -> unmodify(VisibilityHidden)
                    false -> modify(VisibilityHidden)
                }
            }
        }
    }
    return this
}

fun HTMLElement.flowValid(key: String, check: Flow<ValidityCheck>, scope: CoroutineScope): HTMLElement {
    modify(Required)
    scope.launch {
        check.collect { check ->
            when (check.invalidParts.contains(key)) {
                true -> unmodify(Valid)
                false -> modify(Valid)
            }
        }
    }
    return this
}