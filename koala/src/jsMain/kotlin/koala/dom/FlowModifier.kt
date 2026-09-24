package koala.dom

import kampfire.model.ValidityCheck
import koala.modifier.DisplayNone
import koala.modifier.Modifier
import koala.modifier.Required
import koala.modifier.Valid
import koala.modifier.VisibilityHidden
import koala.modifier.Shimmer
import kampfire.model.Tap
import kampfire.model.tapOf
import koala.modifier.isModified
import koala.modifier.modify
import koala.modifier.unmodify
import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import web.dom.document
import web.html.HTMLElement

/**
 * Applies [modifier] while [isModifiedFlow] is `true`, starting from [initialValue], optionally in a view
 * transition.
 */
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
                document.viewTransition {
                    applyModifier(isModified)
                }
            } else {
                applyModifier(isModified)
            }
        }
    }

    return this
}

/** Applies [modifier] while [isModified] is `true`, optionally in a view transition. */
fun HTMLElement.flowModifier(
    isModified: Tap<Boolean>,
    modifier: Modifier,
    scope: CoroutineScope,
    viewTransition: Boolean = false,
) = flowModifier(isModified.now, isModified.flow, modifier, scope, viewTransition)

/** Hides the element while [isDisplayedFlow] is `false`. */
@Deprecated("use field")
fun HTMLElement.flowIsDisplayed(isDisplayedFlow: Flow<Boolean>, scope: CoroutineScope): HTMLElement {
    scope.launch {
        isDisplayedFlow.collect { isDisplayed ->
            if (isDisplayed && !isModified(DisplayNone) || !isDisplayed && isModified(DisplayNone)) return@collect
            document.viewTransition {
                when (isDisplayed) {
                    true -> unmodify(DisplayNone)
                    false -> modify(DisplayNone)
                }
            }
        }
    }
    return this
}

/** Hides the element while [isDisplayedFlow] is `false`. */
fun HTMLElement.flowIsDisplayed(
    isDisplayedFlow: Tap<Boolean>,
    scope: CoroutineScope,
    viewTransition: Boolean = false,
): HTMLElement {
    flowModifier(isDisplayedFlow.tapOf { !it }, DisplayNone, scope, viewTransition)
    return this
}

/** Makes the element invisible, keeping its space, while [isVisibleFlow] is `false`. */
fun HTMLElement.flowVisibility(isVisibleFlow: Flow<Boolean>, scope: CoroutineScope): HTMLElement {
    scope.launch {
        isVisibleFlow.collect { isVisible ->
            if (isVisible && !isModified(VisibilityHidden) || !isVisible && isModified(VisibilityHidden)) return@collect
            document.viewTransition {
                when (isVisible) {
                    true -> unmodify(VisibilityHidden)
                    false -> modify(VisibilityHidden)
                }
            }
        }
    }
    return this
}

/** Marks the element required, and valid while [key] is not among the invalid parts of the check. */
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

/** Marks the element required, and valid while [key] is not among the invalid parts of [checkTap]. */
fun HTMLElement.flowValid(
    key: String,
    checkTap: Tap<ValidityCheck>,
    scope: CoroutineScope
) = flowValid(key, checkTap.now, checkTap.flow, scope)

/** Shimmers the element while [isWorkingFlow] is `true`. */
fun HTMLElement.flowIsWorking(isWorkingFlow: Flow<Boolean>, scope: CoroutineScope): HTMLElement {
    scope.launch {
        isWorkingFlow.collect { isWorking ->
            when (isWorking) {
                true -> modify(Shimmer)
                else -> unmodify(Shimmer)
            }
        }
    }
    return this
}