package streetlight.web.ui

import koala.LottieFile
import koala.css.AlignItemsCenter
import koala.css.BorderRadius2
import koala.css.MoonShadow
import koala.css.OverflowClip
import koala.css.modify
import koala.dom.AppScope
import koala.dom.grid
import koala.dom.lottie
import koala.dom.navigation
import koala.dom.section
import koala.dom.textBlock
import koala.html.em
import streetlight.model.data.Star

fun AppScope.updaterGreeting(star: Star, targetName: String) = grid(
    IntroStyle.Columns, modify(AlignItemsCenter)
) {
    section(modify(IntroStyle.SectionMod)) {
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