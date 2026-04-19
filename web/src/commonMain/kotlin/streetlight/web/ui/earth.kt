package streetlight.web.ui

import koala.html.Id

object EarthKey {
    val id = Id("earth")
}

// language="CSS"
val EarthCss get() = """

${EarthKey.id} .maplibregl-ctrl-top-right {
    top: 50%;
    transform: translateY(-50%);
}

.maplibregl-ctrl {
    filter: invert(1);
}
"""