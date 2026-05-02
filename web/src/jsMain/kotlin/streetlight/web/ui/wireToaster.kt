package streetlight.web.ui

import koala.css.*
import koala.dom.*
import streetlight.web.model.Streetlight
import streetlight.web.pages.AppBodyKey

fun wireToaster(app: Streetlight) {
    val model = app.toaster

    AppBodyKey.ToasterId.replaceRender(app.appScope) {
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