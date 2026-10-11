package streetlight.web.dom.component

import kampfire.model.CursorStatus
import kampfire.model.Tap
import kampfire.model.tapOf
import koala.SvgFile
import koala.modifier.*
import koala.dom.ViewScope
import koala.dom.flowModifier
import koala.dom.component.icon
import koala.component.IconStyle
import kotlinx.coroutines.flow.map
import streetlight.web.component.WorkSignalStyle
import streetlight.web.component.workSignal

fun ViewScope.workSignal(
    mod: Modifier? = IconStyle.DefaultMod
) = icon(SvgFile.CircleLoop, modify(mod, IconStyle.Signal))

fun ViewScope.workSignal(
    state: Tap<Boolean>,
    mod: Modifier? = null
) = workSignal(WorkSignalStyle.AbsolutePositioned.append(mod))
    .flowModifier(state.tapOf { !it }, DisplayNone, contentScope)

fun ViewScope.workSignal(
    state: Tap<CursorStatus>,
    mod: Modifier? = null
) = workSignal(state.tapOf { it.isFetching }, mod)