package streetlight.web.ui

import koala.dom.ViewContext

typealias ModelContext = ViewContext<ViewModel>

val ModelContext.portal get() = model.app.portal
val ModelContext.gate get() = model.app.gate