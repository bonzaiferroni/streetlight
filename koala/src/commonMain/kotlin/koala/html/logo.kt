package koala.html

import kotlinx.html.*
import koala.css.*

fun FlowContent.logo(
    modifiers: ModifierSet? = null
) {
//    box(modify(GlowShadow, modifiers)) {
//        icon("flame", modify(Height100, GlowBackground))
//    }
    div {
        applyModifiers(ElementClass.logo, modifiers)
        div {
//             applyModifiers(GlowBackground)
        }
    }
}



// .logo {
//    display: inline-flex;
//    align-items: center;
//    height: 1.5rem;
//}
//
//.logo svg {
//    width: 100%;
//    height: 100%;
//    display: block;
//    overflow: visible; /* let the glow spill out */
//}
//
//.logo svg * {
//    animation: glow-shadow 10s infinite linear;
//}