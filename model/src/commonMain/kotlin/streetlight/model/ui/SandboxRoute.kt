package streetlight.model.ui

import koala.model.PathParse
import koala.model.SubRoute
import koala.model.buildRelativePath

/** A route of the sandbox, where the subrouter is tried out before the config screens take it up. */
sealed interface SandboxRoute: StreetlightRoute, SubRoute<SandboxRoute> {
    override val screen get() = Screen.Sandbox
    // the sandbox view handles each of its routes itself
    override val screenKey: Any get() = Screen.Sandbox
    override val parent: SandboxRoute? get() = SandboxIndexRoute
}

data object SandboxIndexRoute: SandboxRoute {
    override val title get() = "Index"
    override val parent get() = null
    override fun toRelativePath() = buildRelativePath(listOf(SANDBOX_NODE))
}

data object SandboxAccountRoute: SandboxRoute {
    override val title get() = "Account"
    override fun toRelativePath() = buildRelativePath(listOf(SANDBOX_NODE, ACCOUNT_NODE))
}

data object SandboxStripeRoute: SandboxRoute {
    override val title get() = "Stripe"
    override fun toRelativePath() = buildRelativePath(listOf(SANDBOX_NODE, STRIPE_NODE))
}

data object SandboxItemsRoute: SandboxRoute {
    override val title get() = "Items"
    override fun toRelativePath() = buildRelativePath(listOf(SANDBOX_NODE, ITEMS_NODE))
}

/** The item [itemId], or a new item when it is `null`. */
data class SandboxItemRoute(val itemId: Int?): SandboxRoute {
    override val title get() = itemId?.let { "Item $it" } ?: "New Item"
    override val parent get() = SandboxItemsRoute
    override fun toRelativePath() = buildRelativePath(listOf(SANDBOX_NODE, ITEMS_NODE), itemId ?: NEW_NODE)
}

/** Reads a sandbox route from its path: `/sandbox`, `/sandbox/account`, `/sandbox/stripe`, or `/sandbox/items/{id}`. */
val parseSandboxRoute = PathParse(listOf(ACCOUNT_NODE, STRIPE_NODE, ITEMS_NODE)) { nodes, _ ->
    when (nodes.getOrNull(1)) {
        ACCOUNT_NODE -> SandboxAccountRoute
        STRIPE_NODE -> SandboxStripeRoute
        ITEMS_NODE -> when (val id = nodes.takeSegment(2)) {
            null -> SandboxItemsRoute
            NEW_NODE -> SandboxItemRoute(null)
            else -> id.toIntOrNull()?.let { SandboxItemRoute(it) } ?: SandboxItemsRoute
        }
        else -> SandboxIndexRoute
    }
}

private const val SANDBOX_NODE = "sandbox"
private const val ACCOUNT_NODE = "account"
private const val STRIPE_NODE = "stripe"
private const val ITEMS_NODE = "items"
private const val NEW_NODE = "new"
