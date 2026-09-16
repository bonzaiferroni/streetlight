package streetlight.web.ui

import kampfire.model.CursorStatus
import kampfire.model.Tap
import kampfire.model.tapOf
import koala.SvgFile
import koala.modifier.DisplayNone
import koala.modifier.ModifierSet
import koala.modifier.append
import koala.modifier.modify
import koala.dom.ViewScope
import koala.dom.flowModifier
import koala.dom.icon
import koala.html.IconStyle

fun ViewScope.workSignal(
    mod: ModifierSet? = IconStyle.DefaultMod
) = icon(SvgFile.CircleLoop, modify(mod, IconStyle.Signal))

fun ViewScope.workSignal(
    state: Tap<CursorStatus>,
    mod: ModifierSet? = null
) = workSignal(WorkSignalStyle.AbsolutePositioned.append(mod))
    .flowModifier(state.tapOf { !it.isFetching }, DisplayNone, contentScope)