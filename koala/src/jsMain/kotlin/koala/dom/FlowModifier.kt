package koala.dom

import kampfire.model.ValidityCheck
import koala.css.DisplayNone
import koala.css.Modifier
import koala.css.Required
import koala.css.Valid
import koala.css.VisibilityHidden
import koala.css.Working
import koala.model.Field
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement

fun HTMLElement.flowModifier(
    initialValue: Boolean,
    isModifiedFlow: Flow<Boolean>,
    modifier: Modifier,
    scope: CoroutineScope,
    viewTransition: Boolean = false,
): HTMLElement {

    fun applyModifier(isModified: Boolean) {
        when (isModified) {
            true -> modify(modifier)
            else -> unmodify(modifier)
        }
    }

    applyModifier(initialValue)

    scope.launch {
        isModifiedFlow.collect { isModified ->
            if (isModified && isModified(modifier) || !isModified && !isModified(modifier)) return@collect

            if (viewTransition) {
                document.startViewTransition {
                    applyModifier(isModified)
                }
            } else {
                applyModifier(isModified)
            }
        }
    }

    return this
}

fun HTMLElement.flowModifier(
    isModified: Field<Boolean>,
    modifier: Modifier,
    scope: CoroutineScope,
    viewTransition: Boolean = false,
) = flowModifier(isModified.now, isModified.flow, modifier, scope, viewTransition)

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

fun HTMLElement.flowValid(
    key: String,
    check: ValidityCheck,
    checkFlow: Flow<ValidityCheck>,
    scope: CoroutineScope
): HTMLElement {
    modify(Required)
    if (check.invalidParts.contains(key)) {
        modify(Valid)
    }
    scope.launch("flowValid") {
        checkFlow.collect { check ->
            when (check.invalidParts.contains(key)) {
                true -> unmodify(Valid)
                false -> modify(Valid)
            }
        }
    }
    return this
}

fun HTMLElement.flowValid(
    key: String,
    checkField: Field<ValidityCheck>,
    scope: CoroutineScope
) = flowValid(key, checkField.now, checkField.flow, scope)

fun HTMLElement.flowIsWorking(isWorkingFlow: Flow<Boolean>, scope: CoroutineScope): HTMLElement {
    scope.launch {
        isWorkingFlow.collect { isWorking ->
            when (isWorking) {
                true -> modify(Working)
                else -> unmodify(Working)
            }
        }
    }
    return this
}