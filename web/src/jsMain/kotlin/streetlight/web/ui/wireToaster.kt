package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.Toaster
import streetlight.web.pages.AppBodyKey

fun ScopedDOM.wireToaster() {
    val model = app.get<Toaster>()

    replaceRender(AppBodyKey.ToasterId) {
        column(modify(Padding2)) {
            itemsBlock(model.messagesFlow, modify(Magic, SlideLeft)) {
                val text = it.text ?: return@itemsBlock
                row {
                    card(modify(PrimaryCardBg, BlurBackdrop, BorderRadius1)) {
                        textBlock(text)
                    }
                }
            }
        }
    }
}