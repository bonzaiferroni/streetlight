package streetlight.web.pages

import kampfire.api.PostEndpoint
import kampfire.model.Token
import koala.PageResource
import koala.modifier.AlignItemsCenter
import koala.modifier.AlignSelfEnd
import koala.modifier.AlignSelfStretch
import koala.modifier.MaxWidth64
import koala.modifier.Padding1
import koala.modifier.addModifiers
import koala.modifier.modify
import koala.html.BtnKey
import koala.html.card
import koala.html.column
import koala.html.filigree
import koala.html.heading1
import koala.html.textBlock
import koala.html.topLogo
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.HTML
import kotlinx.html.form
import kotlinx.html.hiddenInput
import kotlinx.html.submitInput
import streetlight.web.ui.BodyStyle

fun HTML.messagePage(
    title: String,
    message: FlowContent.() -> Unit,
    resource: PageResource,
    block: FlowContent.() -> Unit = { }
) {
    staticPage("$title | Streetlight", resource) {
        column(modify(BodyStyle.MainColumn, AlignItemsCenter)) {
            topLogo()
            filigree(modify(AlignSelfStretch)) { heading1(title) }
            card(modify(MaxWidth64, Padding1)) {
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

fun FlowContent.formSubmit(
    text: String,
    token: Token,
    endpoint: PostEndpoint<*, *>
) = form(action = endpoint.path, method = FormMethod.post) {
    addModifiers(AlignSelfEnd)
    hiddenInput(name = "token") { value = token.value }
    submitInput {
        addModifiers(BtnKey.Class)
        value = text
    }
}