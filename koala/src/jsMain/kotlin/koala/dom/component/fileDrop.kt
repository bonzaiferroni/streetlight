package koala.dom.component

import kampfire.model.Url
import koala.modifier.Blur
import koala.modifier.Magic
import koala.modifier.SlideDown
import koala.modifier.modify
import kampfire.model.MutableTap
import koala.dom.ViewScope
import koala.dom.layout.flowBlock

/** A file picker that sets [field] to the picked file's URL, or shows the URL once one is set. */
fun ViewScope.fileDrop(field: MutableTap<Url?>) {
    flowBlock(field, modify(Magic, Blur, SlideDown)) { url ->
        if (url != null) {
            textBlock("file: $url")
        } else {
            filePicker() {
                console.log(it)
                field.set(it)
            }
        }
    }
}