package streetlight.web.ui

import koala.dom.ViewContext

typealias ModelContext = ViewContext<ViewModel>

val ModelContext.portal get() = model.app.portal
val ModelContext.gate get() = model.app.gate
val ModelContext.api get() = model.app.client.api
val ModelContext.toaster get() = model.app.toaster
val ModelContext.stage get() = model.app.stage