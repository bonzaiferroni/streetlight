package streetlight.web.ui

import koala.dom.AppendScope
import koala.html.box
import koala.html.column
import koala.html.row
import koala.html.span
import koala.html.textBlock
import koala.model.MarkerStyle
import koala.model.ThumbMarker
import koala.modifier.AlignItemsCenter
import koala.modifier.Bold
import koala.modifier.ColorSchemeFg
import koala.modifier.Css
import koala.modifier.Gap0
import koala.modifier.Gap2Px
import koala.modifier.LineHeight115
import koala.modifier.OpacityHalf
import koala.modifier.OpacityHigh
import koala.modifier.TextShadow
import koala.modifier.TextSmall
import koala.modifier.WhiteSpaceNoWrap
import koala.modifier.modify
import koala.modifier.setStyle
import kotlinx.html.DIV
import kotlinx.html.img
import kotlinx.html.js.img

internal fun DIV.configureEntityMarker(marker: ThumbMarker) {
    with(marker) {
        // td: declare border radius in stylesheet
        row(modify(MarkerStyle.Body, Gap2Px, AlignItemsCenter, TextShadow)) {
            themeColor?.let {
                setStyle(Css.ColorScheme.of(it))
            }

            box(MarkerStyle.Thumb) {
                img {
                    src = thumbUrl.value
                }
            }

            column(modify(Gap0, LineHeight115, WhiteSpaceNoWrap)) {
                label?.let {
                    textBlock(it, MarkerStyle.LabelMod)
                }
                if (sublabel != null || typeLabel != null) {
                    textBlock(mod = TextSmall) {
                        typeLabel?.let {
                            span(it, modify(ColorSchemeFg, Bold))
                        }
                        sublabel?.let {
                            if (typeLabel != null) span(" • ", OpacityHalf)
                            span(it, OpacityHigh)
                        }
                    }
                }
            }
        }
    }
}