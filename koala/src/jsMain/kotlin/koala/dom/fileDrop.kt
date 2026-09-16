package koala.dom

import kampfire.model.Url
import koala.modifier.Blur
import koala.modifier.Magic
import koala.modifier.SlideDown
import koala.modifier.modify
import kampfire.model.MutableTap

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