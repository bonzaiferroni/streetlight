package streetlight.web.ui

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.model.toDataOr
import koala.modifier.*
import koala.dom.*
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import streetlight.model.data.NewMessage

/** The recipient of the message being written; the dialog is open while it is set. */
val recipientState = storeOf<Username?>(null)

/** The dialog for writing a message, opened with [startMessage]. */
fun ViewScope.wireMessageDialog() {
    val isOpen = recipientState.mutableTapOf({ it != null }) { if (it) this else null }
    dialog(isOpen) {
        val recipient = recipientState.now ?: error("recipient not found")
        val message = NewMessage(recipient, null, Markdown.Empty)
        println("starting message")
        val messenger = MessageStore()
        val messageState = storeOf(message)
        val subjectState = messageState.mutableTapOf({ it.subject ?: ""}) { subject -> copy(subject = subject.takeIf { it.isNotBlank() })}
        val contentState = messageState.mutableTapOf({ it.content }) { copy(content = it) }
        dialogContent("Message to $recipient") {
            textField(subjectState, "subject", placeholder = "no subject")
            markdownEditor(contentState, "message", MinHeight(32)) {
                // setAttribute(Attribute.Autofocus.to(true))
            }
            formSubmit("send", {
                println(messageState.now)
                val message = messageState.now.takeIf { it.isValid } ?: return@formSubmit
                launchEffect {
                    messenger.deliverSending()
                    api.message.sendMessage(message).toDataOr(messenger, "message sent", toaster) { return@launchEffect }
                    dismissDialog()
                }
            }, messenger, Accent, back = MenuAction("cancel", onClick = ::dismissDialog))
        }
    }
}

/** Opens the message dialog to [recipient]. */
fun startMessage(recipient: Username) {
    recipientState.set(recipient)
}

private fun dismissDialog() = recipientState.set(null)