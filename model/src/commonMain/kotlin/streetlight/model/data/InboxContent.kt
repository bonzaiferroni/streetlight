package streetlight.model.data

import koala.model.RouteContent
import kotlinx.serialization.Serializable

/** The content of the inbox: previews of the user's chats. */
@Serializable
data class InboxContent(
    val chats: List<ChatPreview>
): RouteContent {
}