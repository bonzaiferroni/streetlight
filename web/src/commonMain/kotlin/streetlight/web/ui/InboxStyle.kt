package streetlight.web.ui

import koala.css.Class
import koala.css.Reveal

object InboxStyle {
    val Grid = Class("inbox-grid")
    val ChatList = Class("inbox-chat-list")
    val MessageList = Class("inbox-message-list")
}

//language="CSS"
val InboxCss get() = with(InboxStyle) {"""

$Grid:not($Reveal) {
    
    @media (max-width: 600px) {
        $MessageList {
            display: none;
        }
    }
}

$Grid$Reveal {
    
    @media (max-width: 600px) {
        $ChatList {
            display: none;
        }
    }
}

$ChatList {
    
}

"""}