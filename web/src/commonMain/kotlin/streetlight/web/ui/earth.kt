package streetlight.web.ui

import koala.css.AlignSelfStart
import koala.css.DayTheme
import koala.css.JustifySelfCenter
import koala.css.PositionSticky
import koala.css.TopSpacing1
import koala.css.ZIndex1
import koala.css.modify
import koala.html.Id

object EarthKey {
    val Id = Id("earth")
    val ViewMapButtonMod = modify(PositionSticky, TopSpacing1, JustifySelfCenter, AlignSelfStart, ZIndex1)
}

// language="CSS"
val EarthCss get() = """

${EarthKey.Id} .maplibregl-ctrl-top-right {
    top: 50%;
    transform: translateY(-50%);
}

:root:not($DayTheme) .maplibregl-ctrl {
    filter: invert(1);
}

.maplibregl-canvas {
    transition: filter var(--magic-interval) var(--magic-easing);
}

$DayTheme .maplibregl-canvas {
    filter: invert(1) hue-rotate(180deg);
}
"""