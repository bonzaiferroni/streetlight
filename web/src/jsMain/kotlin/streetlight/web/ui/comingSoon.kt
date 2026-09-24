package streetlight.web.ui

import koala.LottieFile
import koala.dom.AppendScope
import koala.dom.lottie
import koala.modifier.*

fun AppendScope.comingSoon(mod: Modifier? = modify(MaxWidth(48), AlignSelfCenter)) =
    lottie(LottieFile.DinoLoad, mod)