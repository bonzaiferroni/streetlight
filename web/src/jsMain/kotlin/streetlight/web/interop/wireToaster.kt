package streetlight.web.interop

import koala.modifier.*
import koala.dom.*
import koala.component.MessageBox
import streetlight.web.model.Toaster
import streetlight.web.page.AppBody
import koala.dom.component.*
import koala.dom.layout.*

/** Shows the toasts of [Toaster]. */
fun ViewScope.wireToaster() {
    val model = app.get<Toaster>()

    mountChildView(AppBody.ToasterId) {
        column(Padding(2)) {
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