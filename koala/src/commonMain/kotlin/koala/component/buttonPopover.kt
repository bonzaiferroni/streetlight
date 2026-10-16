package koala.component

import koala.Svg
import koala.SvgFile
import koala.SvgPack
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import koala.page.Id
import kotlin.uuid.Uuid

/** A button labeled [label] that opens a popover holding what [block] builds. */
fun FlowContent.buttonPopoverRaw(
    label: String,
    mod: Modifier? = null,
    popoverMod: Modifier? = null,
    flair: SvgPack? = SvgFile.Menu,
    id: Id = label.toPopoverId(),
    block: DIV.() -> Unit = {}
) {
    popover(id, mod = popoverMod) {
        block()
    }
    button(label, mod, flair) {
        setPopoverTarget(id, true)
    }
}

fun FlowContent.buttonPopoverRaw(
    svg: Svg,
    mod: Modifier? = null,
    popoverMod: Modifier? = null,
    id: Id = Id(Uuid.random().toString()),
    block: DIV.() -> Unit = {}
) {
    popover(id, mod = popoverMod) {
        block()
    }
    button(svg, mod) {
        setPopoverTarget(id, true)
    }
}

fun FlowContent.buttonPopover(
    label: String,
    mod: Modifier? = null,
    popoverMod: Modifier? = null,
    flair: SvgPack? = SvgFile.Menu,
    id: Id = label.toPopoverId(),
    block: DIV.() -> Unit = {}
) {
    buttonPopoverRaw(label, mod, null, flair, id) {
        popoverCard(popoverMod, block)
    }
}

fun FlowContent.buttonPopover(
    svg: Svg,
    mod: Modifier? = null,
    popoverMod: Modifier? = null,
    id: Id = Id(Uuid.random().toString()),
    block: DIV.() -> Unit = {}
) {
    buttonPopoverRaw(svg, mod, null, id) {
        popoverCard(popoverMod, block)
    }
}

fun String.toPopoverId() = Id("${lowercase().replace(" ", "-")}-popover")