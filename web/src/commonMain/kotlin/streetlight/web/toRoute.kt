package streetlight.web

import streetlight.model.data.Galaxy

fun Galaxy.toRoute() = GalaxyRoute(slug)
fun Galaxy.toEarthRoute() = GalaxyMapRoute(slug)
fun Galaxy.toConfigRoute() = GalaxyConfigRoute(slug)