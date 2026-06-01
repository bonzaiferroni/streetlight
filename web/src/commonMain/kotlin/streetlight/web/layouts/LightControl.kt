package streetlight.web.layouts

import koala.css.Class
import koala.css.JsFun
import koala.html.enumAttributeOf
import koala.html.intAttributeOf
import streetlight.model.data.LightType

object LightControl {
    val Class = Class("light-control")
    val Counter = Class("light-counter")
    val Lit = Class("lit")
    val LitIcon = Class("lit-icon")
    val UnlitIcon = Class("unlit-icon")

    val ToggleFun = JsFun("toggleLight")

    val TypeData = enumAttributeOf<LightType>("light-type")
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