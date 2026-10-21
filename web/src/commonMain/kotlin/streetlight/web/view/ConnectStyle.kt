package streetlight.web.view

import koala.modifier.Class
import streetlight.model.data.OnboardType
import streetlight.web.page.AppAttribute

object ConnectStyle {
    val Panel = Class("connect__panel")
    val IconBadge = Class("connect__icon-badge")
}

// language="CSS"
val ConnectCss get() = with(ConnectStyle) { """
$Panel { display: none; }

/* the panel of the type chosen on the root */
${OnboardType.entries.joinToString(",\n") { "${AppAttribute.OnboardType.selector(it)} $Panel${AppAttribute.ConnectOption.selector(it)}" }} {
    display: block;
}

/* the glowing ring the icon sits in */
$IconBadge {
    border-radius: 50%;
    background: radial-gradient(closest-side, rgba(var(--accent), .22), transparent);
    box-shadow: inset 0 0 0 1px rgba(var(--accent), .35);
}
""" }
