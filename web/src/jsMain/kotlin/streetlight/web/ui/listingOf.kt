package streetlight.web.ui

import kampfire.model.Url
import koala.modifier.AlignItemsCenter
import koala.modifier.modify
import koala.dom.AppendScope
import koala.dom.row
import streetlight.web.layouts.configureListing

fun AppendScope.listingOf(
    label: String,
    imageUrl: Url? = null,
    sublabel: String? = null
) = row(modify(AlignItemsCenter)) {
    configureListing(label, imageUrl, sublabel)
}