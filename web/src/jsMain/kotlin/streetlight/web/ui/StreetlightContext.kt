package streetlight.web.ui

import koala.dom.ViewContext
import streetlight.web.model.Streetlight

val ViewContext<Streetlight>.api get() = model.client.api