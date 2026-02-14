package streetlight.web

import koala.dom.RenderContext
import koala.dom.button
import koala.dom.column
import koala.dom.row
import koala.dom.textField

fun RenderContext.viewStoryEditor(app: AppContext) {
    val model = app.storyEditor
    console.log("ey")

    column {
        row {
            textField(
                label = "url",
                onChangeValue = model::setUrl,
                binding = model.urlFlow
            )
            button("read", onClick = model::readUrl)
        }
    }
}