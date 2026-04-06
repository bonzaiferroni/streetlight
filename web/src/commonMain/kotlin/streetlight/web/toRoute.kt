package streetlight.web

import streetlight.model.data.Galaxy

fun Galaxy.toRoute() = GalaxySlugRoute(slug)