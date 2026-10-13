package streetlight.web.page

import kampfire.api.PostEndpoint
import kampfire.model.Token
import koala.PageResource
import koala.modifier.*
import koala.component.ButtonStyle
import koala.component.card
import koala.component.column
import koala.component.filigree
import koala.component.heading1
import koala.component.textBlock
import koala.modifier.MaxWidth
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.HTML
import kotlinx.html.form
import kotlinx.html.hiddenInput
import kotlinx.html.submitInput
import streetlight.web.layout.BodyStyle
import streetlight.web.component.topLogo

/** A static page that shows [message] under [title], followed by [block]. */
fun HTML.messagePage(
    title: String,
    message: FlowContent.() -> Unit,
    resource: PageResource,
    block: FlowContent.() -> Unit = { }
) {
    staticPage("$title | Streetlight", resource) {
        column(modify(BodyStyle.MainColumn, AlignItemsCenter)) {
            topLogo()
            filigree(AlignSelfStretch) { heading1(title) }
            card(modify(MaxWidth(64), Padding(1))) {
                message()
                block()
            }
            appFooter()
        }
    }
}

fun HTML.messagePage(
    title: String,
    message: String,
    resource: PageResource,
    block: FlowContent.() -> Unit = { }
) {
    messagePage(
        title = title,
        message = { textBlock(message) },
        resource = resource,
        block = block,
    )
}

/** A form that posts [token] to [endpoint] with a submit button labeled [text]. */
fun FlowContent.formSubmit(
    text: String,
    token: Token,
    endpoint: PostEndpoint<*, *>
) = form(action = endpoint.path, method = FormMethod.post) {
    addModifiers(AlignSelfEnd)
    hiddenInput(name = "token") { value = token.value }
    submitInput {
        addModifiers(ButtonStyle.Class)
        value = text
    }
}