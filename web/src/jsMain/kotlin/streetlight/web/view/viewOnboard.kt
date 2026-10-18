package streetlight.web.view

import kampfire.model.AccountType
import kampfire.model.storeOf
import koala.dom.*
import koala.dom.component.textBlock
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.model.NullContent
import streetlight.model.data.OnboardType
import streetlight.model.ui.OnboardRoute
import streetlight.web.dom.layout.configBody
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.getUserCreator
import streetlight.web.model.session

/** The onboarding of a star as the audience the [route] names. */
context(route: OnboardRoute)
fun ViewScope.viewOnboard() = configBody("hello: ", route.onboardType.label, "viewOnboard.kt") {
    // app.getUserCreator(contentScope)
    // val model = Onboarder(contentScope)
    flowBlock(session.starState) { star ->
        when (star) {
            null -> onboardSignIn()
            else -> {
                context(star) {
                    when (route.onboardType) {
                        OnboardType.BusinessOwner -> locationOnboarder()
                        OnboardType.Performer, OnboardType.Developer, OnboardType.EventHost -> {
                            textBlock("hello Onboarder!")
                        }
                    }
                }
            }
        }
    }
}

fun RouteScope.viewOnboardRoute() {
    routeBlock<OnboardRoute, NullContent>() {
        viewOnboard()
    }
}

fun ViewScope.onboardSignIn() {
    val model = app.getUserCreator(contentScope)
    val isRegisteredStore = storeOf(false)

    context(model) {
        flowBlock(isRegisteredStore) { isRegistered ->
            when (isRegistered) {
                true -> signInForm()
                else -> formColumn {
                    fullRegistrationForm()
                    formSubmit(
                        label = "Sign up",
                        onClick = { model.createAccount(AccountType.Registered) },
                        messenger = model.messages,
                        enabledTap = model.isValidField,
                        back = MenuAction("I'm already registered") { isRegisteredStore.set(true) }
                    )
                }
            }
        }
    }
}