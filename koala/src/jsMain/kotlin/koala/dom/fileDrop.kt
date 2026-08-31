package koala.dom

import kampfire.model.Url
import koala.css.Blur
import koala.css.Magic
import koala.css.SlideDown
import koala.css.modify
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