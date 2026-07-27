package streetlight.web.ui

// fun ViewScope.starBlock(
//     redirect: Boolean = false,
//     mod: ModifierSet? = null,
//     block: ViewScope.(Star) -> Unit
// ) {
//     val gate = app.get<StarSession>()
//
//     flowBlock(gate.stateNow.star, gate.starField, mod) { user ->
//         if (user != null) {
//             block(user)
//         } else {
//             div {
//                 +"Must be signed in"
//             }
//         }
//     }
//
//     if (!redirect) return
//
//     val currentRoute = portal.stateNow.route
//
//     contentScope.launch {
//         gate.starField
//             .filterNotNull()
//             .first()
//             .let {
//                 portal.go(currentRoute)
//             }
//     }
//
//     portal.go(StarDashRoute)
// }