package koala.component

import koala.Video
import koala.modifier.*
import kotlinx.html.*

/** A video of [file], configured further by [block]. */
fun FlowOrInteractiveOrPhrasingContent.video(
    file: Video,
    mod: Modifier? = null,
    autoPlay: Boolean = true,
    muted: Boolean = true,
    loop: Boolean = true,
    controls: Boolean = false,
    playsInline: Boolean = true,
    block: VIDEO.() -> Unit = {}
) {
    video {
        src = file.url.value
        addModifiers(mod)
        this.autoPlay = autoPlay
        this.loop = loop
        this.controls = controls
        if (muted) {
            setAttribute(Attribute.Muted, Unit)
        }
        if (playsInline) {
            setAttribute(Attribute.PlaysInline, Unit)
        }
        block()
    }
}
