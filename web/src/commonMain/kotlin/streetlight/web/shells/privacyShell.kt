package streetlight.web.shells

import koala.LottieFile
import koala.css.AlignItemsCenter
import koala.css.Flex1
import koala.css.Flex4
import koala.css.FlexMd2
import koala.css.Gap8
import koala.css.JustifyContentCenter
import koala.css.MaxHeight32
import koala.css.PaddingLeft3
import koala.css.Shrinkable
import koala.css.modify
import koala.html.column
import koala.html.filigree
import koala.html.heading1
import koala.html.lottie
import koala.html.row
import koala.html.section
import koala.html.textBlock
import kotlinx.html.FlowContent

fun FlowContent.privacyShell() {
    column(AboutKey.id, modify(Gap8)) {
        section {
            filigree {
                heading1("Privacy on Streetlight", modify(Shrinkable))
            }
            row(modify(AlignItemsCenter)) {
                column(modify(Flex4, PaddingLeft3)) {
//                    textBlock(introText1)
//                    textBlock(introText2)
                }
                row(modify(Flex1, JustifyContentCenter)) {
                    lottie(LottieFile.AstronautReading, modify(MaxHeight32))
                }
            }
        }
    }
}