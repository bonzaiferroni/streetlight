package streetlight.web.ui

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.model.toDataOr
import koala.css.*
import koala.dom.*
import koala.html.Attribute
import koala.html.setAttribute
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import streetlight.model.data.NewMessage

val recipientState = storeOf<Username?>(null)

fun ViewScope.wireMessageDialog() {
    starGate {
        val isOpen = recipientState.mutableTapOf({ it != null }) { if (it) this else null }
        dialog(isOpen) {
            val recipient = recipientState.now ?: error("recipient not found")
            val message = NewMessage(recipient, null, Markdown.Empty)
            val messenger = MessageStore()
            val messageState = storeOf(message)
            val subjectState = messageState.mutableTapOf({ it.subject ?: ""}) { subject -> copy(subject = subject.takeIf { it.isNotBlank() })}
            val contentState = messageState.mutableTapOf({ it.content }) { copy(content = it) }
            dialogContent("Message to $recipient") {
                textField(subjectState, "subject", placeholder = "no subject")
                styledMarkdownEditor(contentState, "message", modify(MinHeight32)) {
                    setAttribute(Attribute.Autofocus.to(true))
                }
                formSubmit("send", {
                    println(messageState.now)
                    val message = messageState.now.takeIf { it.isValid } ?: return@formSubmit
                    launchEffect {
                        messenger.deliverSending()
                        api.sendMessage(message).toDataOr(messenger, "message sent", toaster) { return@launchEffect }
                        dismissDialog()
                    }
                }, messenger, modify(Accent), back = MenuAction("cancel", onClick = ::dismissDialog))
            }
        }
    }
}

fun startMessage(recipient: Username) {
    recipientState.set(recipient)
}

private fun dismissDialog() = recipientState.set(null)