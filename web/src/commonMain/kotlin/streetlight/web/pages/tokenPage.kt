package streetlight.web.pages

import kampfire.api.PostEndpoint
import kampfire.model.Token
import koala.css.AlignSelfCenter
import koala.css.AlignSelfEnd
import koala.css.MaxWidth64
import koala.css.Padding1
import koala.css.addModifiers
import koala.css.modify
import koala.html.BtnKey
import koala.html.card
import koala.html.column
import koala.html.filigree
import koala.html.heading1
import koala.html.textBlock
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.HTML
import kotlinx.html.form
import kotlinx.html.hiddenInput
import kotlinx.html.submitInput
import streetlight.web.ui.BodyStyle

fun HTML.messagePage(
    title: String,
    message: String,
    styles: String,
    block: FlowContent.() -> Unit = { }
) {
    staticPage("$title | Streetlight", styles) {
        column(BodyStyle.Column) {
            filigree { heading1(title) }
            card(modify(MaxWidth64, AlignSelfCenter)) {
                textBlock(message, modify(Padding1))
                block()
            }
        }
    }
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