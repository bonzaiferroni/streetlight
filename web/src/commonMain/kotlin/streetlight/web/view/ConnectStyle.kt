package streetlight.web.view

import koala.modifier.Class
import streetlight.model.data.ConnectType
import streetlight.web.page.AppAttribute

object ConnectStyle {
    val Panel = Class("connect__panel")
    val Prompt = Class("connect__prompt")
}

// language="CSS"
val ConnectCss get() = with(ConnectStyle) { """
$Panel { display: none; }

/* the panel of the type chosen on the root */
${ConnectType.entries.joinToString(",\n") { "${AppAttribute.ConnectType.selector(it)} $Panel${AppAttribute.ConnectOption.selector(it)}" }} {
    display: block;
}

${AppAttribute.ConnectType} $Prompt { display: none; }
""" }
