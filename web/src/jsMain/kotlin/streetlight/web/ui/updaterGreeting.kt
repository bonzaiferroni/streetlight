package streetlight.web.ui

import koala.LottieFile
import koala.modifier.AlignItemsCenter
import koala.modifier.BorderRadius2
import koala.modifier.MoonShadow
import koala.modifier.OverflowClip
import koala.modifier.modify
import koala.dom.ViewScope
import koala.dom.grid
import koala.dom.lottie
import koala.dom.navigation
import koala.dom.section
import koala.dom.textBlock
import koala.html.em
import streetlight.model.data.Star

fun ViewScope.updaterGreeting(star: Star, targetName: String) = grid(
    IntroStyle.Columns, AlignItemsCenter
) {
    section(IntroStyle.SectionMod) {
        textBlock {
            +"Hello "
            em(star.username.value)
            +". "
            // strong("level ${star.scoutLevel} scout. ")
            if (star.scoutLevel in 0..1) {
                +"Please take a moment to become familiar with the "
                navigation { +"content policy" }
                +" if you haven't already. "
            }
        }
        textBlock("Thank you for contributing, what can you tell us about $targetName?")
    }
    lottie(LottieFile.StreetlightNight, modify(BorderRadius2, OverflowClip, MoonShadow))
}