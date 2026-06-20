package streetlight.web.layouts

import kampfire.model.Url
import koala.css.*
import koala.html.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.listingOf(
    label: String,
    imageUrl: Url? = null,
    sublabel: String? = null
) {
    row {
        configureListing(label, imageUrl, sublabel)
    }
}

internal fun DIV.configureListing(
    label: String,
    imageUrl: Url? = null,
    sublabel: String? = null
) {
    val heightMod = if (sublabel != null) Height8 else Height5
    image(imageUrl, modify(Aspect1, BorderRadius1, OverflowClip, heightMod))
    heading3(label)
    sublabel?.let {
        textBlock(it)
    }
}