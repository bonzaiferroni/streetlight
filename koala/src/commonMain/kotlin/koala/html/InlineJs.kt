package koala.html

object InlineJs {
    // language="JS"
    fun closePopover(id: Id) = "document.getElementById('${id.identifier}')?.hidePopover()"
}