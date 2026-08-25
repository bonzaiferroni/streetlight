package streetlight.web.ui

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.model.toDataOr
import koala.css.*
import koala.dom.*
import koala.model.mutableTapOf
import koala.model.storeOf
import kotlinx.coroutines.delay
import streetlight.model.data.Message
import streetlight.model.data.MessageEdit
import streetlight.model.data.MessageId
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

val recipientState = storeOf<Username?>(null)

fun ViewScope.wireMessageDialog() {
    starGate {
        val isOpen = recipientState.mutableTapOf({ it != null }) { if (it) this else null }
        dialog(isOpen) {
            val recipient = recipientState.now ?: error("recipient not found")
            val messageId = MessageId(Uuid.random())
            val message = MessageEdit(messageId, messageId, null, recipient, null, Markdown.Empty)
            val messenger = MessageStore()
            val messageState = storeOf(message)
            val subjectState = messageState.mutableTapOf({ it.subject ?: ""}) { subject -> copy(subject = subject.takeIf { it.isNotBlank() })}
            val contentState = messageState.mutableTapOf({ it.content }) { copy(content = it) }
            dialogContent("Message to $recipient") {
                textField(subjectState, "subject", placeholder = "no subject")
                styledMarkdownEditor(contentState, "message", modify(MinHeight32))
                formSubmit("send", {
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