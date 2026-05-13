package streetlight.web.ui

import koala.css.AlignItemsEnd
import koala.css.AlignItemsStart
import koala.css.Flex1
import koala.css.Width100P
import koala.css.modify
import koala.dom.*
import streetlight.web.model.LocationFinder

fun RenderContext.viewLocationFinder(model: LocationFinder) {
    column {
        row(modify(AlignItemsStart)) {
            column(modify(Flex1, AlignItemsEnd)) {
                textField("search", modify(Width100P), model::setQuery, model.queryFlow, placeholder = "Search by name or address")
            }
            button("Search", onClick = model::searchQuery)
        }
    }
}