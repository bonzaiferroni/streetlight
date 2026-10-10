package streetlight.web.layouts

import koala.html.*
import koala.interop.JsSignature
import koala.interop.ThisElement
import koala.interop.ThisEvent
import koala.modifier.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.Entity
import streetlight.model.data.EntityRef
import streetlight.model.data.EntityType

/**
 * All of [entity]: its image, heading and cells joined edge to edge as one part, then its whole body and links, as
 * a page header shows them.
 */
fun FlowContent.entityDialog(entity: Entity) {
    div(modify(EntityDialogStyle.Base, CardBg)) {
        setStyle(Css.ColorScheme.of(entity.toThemeColor().cssValue))
        containImage(entity.image, modify(EntityDialogStyle.Image, CardBg), isNative = true)
        div(modify(CardBg, TextAlignCenter, PaddingX2, PaddingY2)) {
            navigationIfNotNull(entity.url?.value ?: entity.toRoute()?.toRelativePath()) {
                heading2(entity.label, modify(LineHeight115, Bold))
            }
        }
        cellGrid(entity.toCells(), entityButtonsOf(entity, false))
        entityBody(entity.body, entity.links)
    }
}

/** Makes this element open the [EntityDialog] for [ref] on a click that lands outside a link. */
fun DIV.isDialogEntity(ref: EntityRef) {
    setAttribute(EntityDialog.Type.to(ref.type))
    setAttribute(EntityDialog.Slug.to(ref.slug))
    onClick = EntityDialog.Open.invokeJs(ThisElement, ThisEvent)
}

/** The dialog that reads and shows the whole entity of a record, and the attributes that name the record. */
object EntityDialog {
    val Open = JsSignature("openEntityDialog")
    val Type = enumAttributeOf<EntityType>("entity-type")
    val Slug = slugAttributeOf("entity-slug")
}

object EntityDialogStyle {
    val Frame = Class("entity-dialog-frame")
    val Base = Class("entity-dialog")
    val Image = Base.withBemElement("image")
}

// language="CSS"
val EntityDialogCss get() = with(EntityDialogStyle) { """
$Base {
    display: grid;
    container-type: inline-size;
    max-height: 80vh;
    overflow-y: auto;
    border: var(--outline-low);
    border-radius: var(--unit-2);
    /* its own blur, stacked on the dialog backdrop's, so the page behind the content stays out of the text */
    backdrop-filter: blur(24px);
    -webkit-backdrop-filter: blur(24px);
}

/* the dialog scales in with a bounce as it opens, and back without one as it closes */
${DialogStyle.Class}$Frame[open] {
    transform: scale(.94);

    &$Reveal {
        transform: scale(1);
        transition: var(--transition-opacity), var(--transition-transform-bounce);
    }
}

/* tall enough to show its backdrop, and short enough that more content shows below it */
$Image { min-height: var(--unit-32); }
$Image ${ContainImage.NativeClass} { max-height: 50vh; }
""" }
