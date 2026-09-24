package streetlight.model.ui

import streetlight.model.data.Galaxy

/** The route of this galaxy's page. */
fun Galaxy.toRoute() = GalaxyRoute(slug)
/** The route of this galaxy on the earth view. */
fun Galaxy.toEarthRoute() = GalaxyMapRoute(slug)
/** The route of this galaxy's config page. */
fun Galaxy.toConfigRoute() = GalaxyConfigRoute(slug)