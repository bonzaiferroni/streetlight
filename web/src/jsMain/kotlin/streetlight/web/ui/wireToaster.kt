package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.MessageBox
import streetlight.web.model.Toaster
import streetlight.web.pages.AppBody

fun ViewScope.wireToaster() {
    val model = app.get<Toaster>()

    mountChildView(AppBody.ToasterId) {
        column(modify(Padding2)) {
            itemsBlock(model.messagesState, modify(Magic, SlideLeft)) { message ->
                val typeMod = message.messageType.toModifier()
                row {
                    card(modify(MessageBox.Mod, BlurBackdrop, typeMod)) {
                        textBlock(message.text)
                    }
                }
            }
        }
    }
}