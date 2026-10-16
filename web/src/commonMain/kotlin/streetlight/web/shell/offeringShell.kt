package streetlight.web.shell

import koala.component.*
import koala.layout.*
import koala.markdown.*
import koala.modifier.*
import koala.page.*
import kotlinx.html.FlowContent
import streetlight.model.data.Offering
import streetlight.web.layout.mainBody

fun FlowContent.offeringShell(offering: Offering) {
    mainBody("offeringShell.kt") {
        row(AlignItemsCenter) {
            image(offering.image?.small, modify(BorderRadius2, MaxHeight(16)))
            heading2(offering.title)
        }
        offering.description?.let {
            markdown(it)
        }
    }

    dataIsland(OfferingShell.IslandId, offering)
}

object OfferingShell {
    val IslandId = Id("offering-shell__island")
}
