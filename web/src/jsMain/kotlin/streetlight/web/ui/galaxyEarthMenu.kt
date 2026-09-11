package streetlight.web.ui

//fun ViewScope.galaxyEarthMenu(
//    currentGalaxy: Galaxy?,
//    mod: ModifierSet? = null
//) {
//    val cache = app.get<DataCache>()
//    val galaxies = cache.galaxyStars.stateNow.items
//
//    box(mod) {
//        buttonPopover(currentGalaxy?.name ?: "Galaxies") {
//            card(modify(ButtonPopover.CardMod)) {
//                row(GalaxyMenuKey.RowMods) {
//                    if (currentGalaxy != null) {
//                        btn("Top", GalaxyMapRoute(null))
//                    }
//                    galaxies.forEach { galaxy ->
//                        if (galaxy.name == currentGalaxy?.name) return@forEach
//                        btn(
//                            text = galaxy.name,
//                            route = GalaxyMapRoute(galaxy.slug),
//                            background = galaxy.image?.small,
//                            modifiers = modify(mod)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}