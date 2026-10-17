package streetlight.web.view

import koala.modifier.Class
import streetlight.model.data.OnboardType
import streetlight.web.page.AppAttribute

object ConnectStyle {
    val Panel = Class("connect__panel")
}

// language="CSS"
val ConnectCss get() = with(ConnectStyle) { """
$Panel { display: none; }

/* the panel of the type chosen on the root */
${OnboardType.entries.joinToString(",\n") { "${AppAttribute.OnboardType.selector(it)} $Panel${AppAttribute.ConnectOption.selector(it)}" }} {
    display: block;
}
""" }
