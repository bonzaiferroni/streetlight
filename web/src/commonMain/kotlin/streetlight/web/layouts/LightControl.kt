package streetlight.web.layouts

import kabinet.utils.toMetricString
import koala.SvgFile
import koala.css.*
import koala.html.*
import kotlinx.html.FlowContent
import kotlinx.html.onClick
import streetlight.model.data.GalaxyPost
import streetlight.model.data.LightType

object LightControl {
    val Class = Class("light-control")
    val Counter = Class("light-counter")
    val Lit = Class("lit")
    val LitIcon = Class("lit-icon")
    val UnlitIcon = Class("unlit-icon")

    val ToggleFun = JsFun("toggleLight")

    val TypeData = enumAttributeOf<LightType>("light-type")

    fun getLitMod(isLit: Boolean) = if (isLit) Lit else null
}

fun FlowContent.postLight(post: GalaxyPost) {
    row(modify(LightControl.Class, AlignItemsCenter, GapTiny, LightControl.getLitMod(post.base.isLit))) {
        setAttribute(LightControl.TypeData.to(LightType.Post))
        onClick = LightControl.ToggleFun.invoke(ThisElement, post.base.postId)

        box(modify(OpacityHigh, Height3, Aspect1)) {
            icon(SvgFile.Boost, modify(LightControl.UnlitIcon))
            icon(SvgFile.BoostFilled, modify(LightControl.LitIcon))
        }
        textBlock(
            post.base.lightCount.toMetricString(),
            mod = modify(LightControl.Counter, TextAlignCenter, TextSmall, LineHeight1)
        )
    }
}

// language="CSS"
val LightControlCss get() = with(LightControl) {"""
    
$Class {
    
    $LitIcon {
        visibility: hidden;
    }
    
    &$Lit {
        color: var(--lamp-fg);
        
        $UnlitIcon {
            visibility: hidden;
        }
        
        $LitIcon {
            visibility: visible;
        }
    }
}
""" }