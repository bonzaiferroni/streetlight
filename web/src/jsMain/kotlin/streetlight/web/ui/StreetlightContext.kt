package streetlight.web.ui

import koala.dom.ViewContext
import streetlight.web.model.Streetlight

val ViewContext<Streetlight>.api get() = model.client.api
val ViewContext<Streetlight>.portal get() = model.portal
val ViewContext<Streetlight>.userCache get() = model.cache

val ViewContext<ViewModel>.portal get() = model.app.portal
val ViewContext<ViewModel>.gate get() = model.app.gate