package streetlight.web.view

import kampfire.model.storeOf
import kampfire.model.toggle
import koala.SvgFile
import koala.dom.ViewScope
import koala.dom.component.box
import koala.dom.component.button
import koala.dom.component.card
import koala.dom.component.column
import koala.dom.component.icon
import koala.dom.component.row
import koala.dom.component.textBlock
import koala.dom.layout.flowBlock
import koala.modifier.AlignItemsCenter
import koala.modifier.Css
import koala.modifier.FlexColumn
import koala.modifier.FlexRow
import koala.modifier.Gap
import koala.modifier.Height
import koala.modifier.Height2Px
import koala.modifier.JustifyContentSpaceBetween
import koala.modifier.Presto
import koala.modifier.PrestoItems
import koala.modifier.Transitioner
import koala.modifier.Width
import koala.modifier.invoke
import koala.modifier.modify
import koala.modifier.transition

fun ViewScope.prestoShowcase() {
    column(PrestoItems) {
        val passages = listOf(
            "A short line.",
            "A longer passage that runs on for a while, so the block grows taller and the transition has some ground to cover.",
            "A middling passage, a line or two.",
        )
        val indexState = storeOf(0)
        button("Next", { indexState.set { (this + 1) % passages.size } })
        flowBlock(indexState, modify(Transitioner, Presto.Height, Width(32))) { index ->
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

        val hands = listOf("Bosun", "Cook", "Lookout", "Gunner", "Carpenter", "Surgeon", "Swabbie")
        val deckState = storeOf(hands.take(3))
        button("Hire", { deckState.set { this + (hands.firstOrNull { it !in this } ?: return@set this) } })
        flowBlock(deckState, modify(Transitioner)) { deck ->
            column {
                deck.forEach { hand ->
                    card(modify(FlexRow, AlignItemsCenter, JustifyContentSpaceBetween, transition("deck-$hand"), Presto.Pop)) {
                        +hand
                        button("Dismiss", { deckState.set { this - hand } })
                    }
                }
            }
        }
    }
}