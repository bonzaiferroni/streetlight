package streetlight.web.ui

import kampfire.model.CursorStatus
import kampfire.model.Tap
import kampfire.model.tapOf
import koala.SvgFile
import koala.css.Aspect1
import koala.css.DisplayNone
import koala.css.FadeIn
import koala.css.Height5
import koala.css.Margin1
import koala.css.ModifierSet
import koala.css.PositionAbsolute
import koala.css.PrimaryFg
import koala.css.modify
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
) = workSignal(modify(mod, PositionAbsolute, Height5, Aspect1, Margin1, PrimaryFg, FadeIn))
    .flowModifier(state.tapOf { !it.isFetching }, DisplayNone, contentScope)