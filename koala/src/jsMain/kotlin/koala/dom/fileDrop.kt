package koala.dom

import kampfire.model.Url
import koala.css.Blur
import koala.css.Magic
import koala.css.SlideDown
import koala.css.modify
import koala.model.MutableField

fun ViewScope.fileDrop(field: MutableField<Url?>) {
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