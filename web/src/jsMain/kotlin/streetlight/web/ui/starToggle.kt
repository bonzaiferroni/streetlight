package streetlight.web.ui

import kampfire.model.handleResponse
import koala.SvgFile
import koala.css.Height3
import koala.css.ModifierSet
import koala.css.Property
import koala.css.ScaleIn
import koala.css.ScaleOut
import koala.css.modify
import koala.dom.AppScope
import koala.dom.icon
import koala.dom.modify
import koala.dom.onClick
import koala.dom.onClickElement
import koala.dom.setStyle
import koala.dom.unmodify
import streetlight.model.data.GalaxyId
import streetlight.model.data.LightEdit
import streetlight.model.data.LightType
import streetlight.model.data.RecordId
import kotlin.uuid.Uuid

fun AppScope.starToggle(
    isLit: Boolean,
    recordId: RecordId,
    modifiers: ModifierSet? = null
) {
    val lightType = when (recordId) {
        is GalaxyId -> LightType.Galaxy
        else -> error("unsupported light type")
    }
    var litNow = isLit
    fun svg() = if (litNow) SvgFile.StarFilled else SvgFile.StarOutline
    icon(svg(), modify(Height3, modifiers)).onClickElement { icon ->
        launchEffect {
            icon.unmodify(ScaleIn)
            icon.modify(ScaleOut)
            api.editLight(LightEdit(recordId.value, !litNow, lightType)).handleResponse(toaster::toast) {
                litNow = !litNow
                icon.setStyle(Property.MaskUrl.to(svg()))
                icon.modify(ScaleIn)
                icon.unmodify(ScaleOut)
            }
        }
    }
}