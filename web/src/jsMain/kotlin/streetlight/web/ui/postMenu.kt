package streetlight.web.ui

import koala.css.Danger
import koala.css.Secondary
import koala.css.modify
import koala.dom.button
import koala.dom.column
import koala.dom.dangerButton
import koala.dom.querySelectorAll
import koala.dom.textBlock
import kotlinx.coroutines.launch
import streetlight.model.data.PostId
import streetlight.web.io.getDataOrNull
import streetlight.web.layouts.PostKey
import streetlight.web.layouts.PostMenuData

fun AppContext.postMenu(data: PostMenuData) {
    column {
        button("report", modify(Secondary))
        dangerButton("remove", onClick = {
            renderScope.launch {
                val isRemoved = api.removePost(data.postId).getDataOrNull()
                if (isRemoved == true) {
                    model.stage.galaxy.removePost(data.postId)
                }
            }
        })
    }
}