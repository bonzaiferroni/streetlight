package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.navigation
import koala.dom.component.textBlock
import koala.dom.layout.subrouter
import streetlight.model.ui.SandboxAccountRoute
import streetlight.model.ui.SandboxIndexRoute
import streetlight.model.ui.SandboxItemRoute
import streetlight.model.ui.SandboxItemsRoute
import streetlight.model.ui.SandboxStripeRoute
import streetlight.web.dom.layout.formSection

fun ViewScope.subrouterSandbox() {
    subrouter(
        label = "Sandbox",
        index = SandboxIndexRoute,
        menu = sandboxMenu,
    ) { route ->
        when (route) {
            SandboxIndexRoute -> sandboxIndex()
            SandboxAccountRoute -> formSection("Account") {
                textBlock("The account settings would go here.")
            }
            SandboxStripeRoute -> formSection("Stripe") {
                textBlock("The Stripe connection would go here.")
            }
            SandboxItemsRoute -> sandboxItems()
            is SandboxItemRoute -> formSection(route.title) {
                textBlock(route.itemId?.let { "The editor of item $it would go here." } ?: "A new item would start here.")
            }
        }
    }
}

private val sandboxMenu = listOf(SandboxAccountRoute, SandboxStripeRoute, SandboxItemsRoute)

/** Links to each route of the menu, which serve as the menu when the panel is hidden. */
private fun ViewScope.sandboxIndex() = formSection("Index") {
    sandboxMenu.forEach { route ->
        navigation(route) { +route.title }
    }
}

private fun ViewScope.sandboxItems() = formSection("Items") {
    navigation(SandboxItemRoute(null)) { +"New item" }
    (1..3).forEach { itemId ->
        val route = SandboxItemRoute(itemId)
        navigation(route) { +route.title }
    }
}