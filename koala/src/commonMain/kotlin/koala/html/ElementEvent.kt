package koala.html

open class ElementEvent(
    val label: String
) {

    companion object {
        val onToggle = CustomElementEvent<Boolean>("on-toggle")
        val onClick = ElementEvent("click")
        val onClose = ElementEvent("close")
        // val onEvent = HtmlEvent("on-event")
        // val onClick = HtmlEvent("click")
    }
}

class CustomElementEvent<T>(label: String): ElementEvent(label)