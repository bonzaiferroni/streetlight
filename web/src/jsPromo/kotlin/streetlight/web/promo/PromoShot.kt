package streetlight.web.promo

import koala.dom.ViewScope
import streetlight.model.io.ApiClient
import streetlight.model.testing.TestApiClient

/** The content of a promo shot: the [view] it mounts, over the fakes that [api] returns. */
class PromoShot(
    val api: () -> ApiClient = { TestApiClient() },
    val view: ViewScope.() -> Unit,
)
