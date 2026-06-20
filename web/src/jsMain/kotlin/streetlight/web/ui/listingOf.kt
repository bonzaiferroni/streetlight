package streetlight.web.ui

import kampfire.model.Url
import koala.css.AlignItemsCenter
import koala.css.modify
import koala.dom.TagScope
import koala.dom.row
import streetlight.web.layouts.configureListing

fun TagScope.listingOf(
    label: String,
    imageUrl: Url? = null,
    sublabel: String? = null
) = row(modify(AlignItemsCenter)) {
    configureListing(label, imageUrl, sublabel)
}