package streetlight.web.ui

import kampfire.api.Markdown
import koala.SvgFile
import koala.css.AlignItemsStart
import koala.css.Flex1
import koala.css.Flex3
import koala.css.Height100Vh
import koala.css.JustifyContentEnd
import koala.css.Magic
import koala.css.Padding1
import koala.css.Scale
import koala.css.modify
import koala.dom.*
import koala.html.heading1
import koala.model.EditorStyle
import koala.model.storeOf
import koala.model.toggle
import streetlight.web.utils.localStoreOf

fun RouteScope.viewSandbox() {
    // val model = Sandbox(contentScope, api, toaster)

    console.log("welcome to sandbox")

    val list = LazyList((0..1000).map { "Foo $it" })

    row(modify(Padding1, AlignItemsStart)) {
        lazyColumn(list, modify(Flex1, Height100Vh)) { item ->
            textBlock(item, modify(Padding1))
        }
        card(modify(Flex3))
    }
}

class SandboxException : Exception("Arrr sandbox exception")

