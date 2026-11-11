package streetlight.web.view

import koala.dom.*
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.NullContent
import koala.modifier.modify
import kampfire.model.storeOf
import kampfire.model.toggle
import koala.SvgFile
import koala.modifier.*
import streetlight.model.ui.SandboxRoute
import streetlight.web.dom.layout.mainBody

/** The sandbox, where the [subrouter] is tried out on the [SandboxRoute]s. */
fun ViewScope.viewSandbox() = mainBody("Sand", "Box", "viewSandbox.kt") {
    val passages = listOf(
        "A short line.",
        "A longer passage that runs on for a while, so the block grows taller and the transition has some ground to cover.",
        "A middling passage, a line or two.",
    )
    val indexState = storeOf(0)
    button("Next", { indexState.set { (this + 1) % passages.size } })
    flowBlock(indexState, modify(Transitioner, PrestoSelf, Presto.Height, Width(32))) { index ->
        card {
            +passages[index]
        }
    }

    val isOpenState = storeOf(false)
    flowBlock(isOpenState, modify(Transitioner)) { isOpen ->
        when (isOpen) {
            false -> card(modify(FlexRow, AlignItemsCenter, transition("morph-card"), Presto.Card)) {
                icon(SvgFile.Magic.filled, modify(Height(4), transition("morph-icon")))
                textBlock("Presto", transition("morph-title"))
                button("Open", isOpenState::toggle, transition("morph-button"))
            }
            true -> card(modify(FlexColumn, AlignItemsCenter, Width(32), transition("morph-card"), Presto.Card)) {
                icon(SvgFile.Magic.filled, modify(Height(16), transition("morph-icon")))
                textBlock("Presto", transition("morph-title"))
                textBlock("The icon grows, the title moves, and the card stretches to its new shape.")
                button("Close", isOpenState::toggle, transition("morph-button"))
            }
        }
    }

    val crewState = storeOf(listOf("Navigator", "Rustbeard", "Leadline", "Bosun", "Cook", "Lookout"))
    row {
        button("Shuffle", { crewState.set { shuffled() } })
        button("Sort", { crewState.set { sorted() } })
    }
    flowBlock(crewState, modify(Transitioner)) { crew ->
        column {
            crew.forEach { name ->
                card(transition("crew-$name")) {
                    +name
                }
            }
        }
    }

    val tabs = listOf("Deck", "Rigging", "Crow's Nest", "Hold")
    val tabState = storeOf(0)
    flowBlock(tabState, modify(Transitioner)) { selected ->
        row(Gap(0)) {
            tabs.forEachIndexed { index, tab ->
                column(Gap(0)) {
                    button(tab, { tabState.set { index } })
                    if (index == selected) {
                        box(modify(Height2Px, Css.BackgroundColor.of("rgb(var(--primary))"), transition("tab-indicator")))
                    }
                }
            }
        }
    }
}

fun RouteScope.viewSandboxRoute() {
    routeBlock<SandboxRoute, NullContent> {
        viewSandbox()
    }
}

class SandboxException : Exception("There's a snake in my boot!")
