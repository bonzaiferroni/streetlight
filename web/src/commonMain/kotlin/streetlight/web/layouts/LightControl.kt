package streetlight.web.layouts

import koala.css.*
import koala.html.*
import koala.interop.JsSignature
import streetlight.model.data.StarType

object LightControl {
    val Class = Class("light-control")
    val Counter = Class("light-counter")
    val Lit = Class("lit")
    val LitIcon = Class("lit-icon")
    val UnlitIcon = Class("unlit-icon")

    val ToggleFun = JsSignature("toggleLight")

    val TypeData = enumAttributeOf<StarType>("light-type")

    fun getLitMod(isLit: Boolean) = if (isLit) Lit else null
}

//fun FlowContent.postLight(post: GalaxyPost) {
//    row(modify(LightControl.Class, AlignItemsCenter, Gap2Px, LightControl.getLitMod(post.base.lean))) {
//        setAttribute(LightControl.TypeData.to(LightType.Post))
//        onClick = LightControl.ToggleFun.invokeJs(ThisElement, post.base.postId)
//
//        box(modify(OpacityHigh, Height3, Aspect1)) {
//            icon(SvgFile.Boost, modify(LightControl.UnlitIcon))
//            icon(SvgFile.BoostFilled, modify(LightControl.LitIcon))
//        }
//        textBlock(
//            post.base.lightCount.toMetricString(),
//            mod = modify(LightControl.Counter, TextAlignCenter, TextSmall, LineHeight1)
//        )
//    }
//}

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