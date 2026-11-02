package koala.dom.component

import kampfire.model.MutableTap
import kampfire.model.Url
import koala.SvgFile
import koala.dom.AppendScope
import koala.dom.ViewScope
import koala.dom.layout.flowBlock
import koala.modifier.*
import kotlinx.html.js.audio

/**
 * An audio picker bound to [field]: a drop area while it is empty, and a player for the audio with a button to clear
 * it once set.
 */
fun ViewScope.audioDrop(
    field: MutableTap<Url?>,
    mod: Modifier? = null,
) = flowBlock(field, modify(mod)) { url ->
    if (url != null) {
        audioPlayer(url) { field.set(null) }
    } else {
        filePicker(MimeType.Audio, modify(Size100P, BorderRadius2)) {
            field.set(it)
        }
    }
}

/** A player for the audio at [url], with a button that calls [onClear]. */
internal fun AppendScope.audioPlayer(url: Url, onClear: () -> Unit) = row(modify(AlignItemsCenter, Gap(1))) {
    audio {
        addModifiers(Flex1)
        src = url.value
        controls = true
    }
    button(
        mod = modify(SystemBg, Outline, BorderRadius50P, Aspect1, Padding(1), OpacityHigh),
        onClick = onClear
    ) {
        icon(SvgFile.X.small)
    }
}
