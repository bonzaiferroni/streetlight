package streetlight.web.ui

import kampfire.model.toDataOr
import koala.SvgFile
import koala.modifier.Height3
import koala.modifier.ModifierSet
import koala.modifier.Property
import koala.modifier.ScaleIn
import koala.modifier.ScaleOut
import koala.modifier.modify
import koala.dom.ViewScope
import koala.dom.icon
import koala.dom.modify
import koala.dom.onClickElement
import koala.dom.setStyle
import koala.dom.unmodify
import streetlight.model.data.GalaxyId
import streetlight.model.data.LightEdit
import streetlight.model.data.ToggleType
import streetlight.model.data.RecordId

fun ViewScope.starToggleProto(
    isLit: Boolean,
    recordId: RecordId,
    mod: ModifierSet? = null
) {
    val toggleType = when (recordId) {
        is GalaxyId -> ToggleType.Galaxy
        else -> error("unsupported light type")
    }
    var litNow = isLit
    fun svg() = if (litNow) SvgFile.StarFilled else SvgFile.Star
    icon(svg(), modify(Height3, mod)).onClickElement { icon ->
        launchEffect {
            icon.unmodify(ScaleIn)
            icon.modify(ScaleOut)
            api.editStarLink(LightEdit(recordId.value, !litNow, toggleType)).toDataOr(toaster) { return@launchEffect }

            litNow = !litNow
            icon.setStyle(Property.MaskUrl.of(svg()))
            icon.modify(ScaleIn)
            icon.unmodify(ScaleOut)
        }
    }
}