package koala.css

import kotlinx.css.*

// :root {
//    --bg: 9, 13, 13;
//    --fg: 245, 246, 246;
//    --void: 24, 31, 31;
//    --primary-bg: 1, 122, 138;
//    --primary: 5, 242, 255;
//    --accent-bg: 209, 43, 181;
//    --accent: 255, 53, 221;
//    --light-1: 255, 99, 132;
//    --light-2: 88, 164, 255;
//    --light-3: 88, 255, 188;
//}

data class KoalaTheme(
    val spacingUnit: LinearDimension = 0.5.rem,
    val bg: Color = rgb(9, 13, 13),
    val fg: Color = rgb(245, 246, 246),
    val void: Color = rgb(24, 31, 31),
    val primaryBg: Color = rgb(1, 122, 138),
    val primary: Color = rgb(5, 242, 255),
    val accentBg: Color = rgb(209, 43, 181),
    val accent: Color = rgb(255, 160, 231),
    val light1: Color = rgb(255, 99, 132),
    val light2: Color = rgb(88, 164, 255),
    val light3: Color = rgb(88, 255, 188),
) {
    companion object {
        const val MAGIC_INTERVAL = 222
    }
}

object KoalaVar {
    val Paper = Property<Rgb>("paper")
    val PaperBg = Property<Color>("paper-bg")
    val BodyBg = Property<Color>("body-bg")
}