package streetlight.compose.svg

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class DrawSvgTest {

    @Test
    fun `every animated icon draws at any point in its animation`() {
        val files = File("../www/icon").listFiles { file -> file.name.endsWith("-animated.svg") }.orEmpty()
        assertTrue(files.isNotEmpty(), "the animated icons should be found in www/icon")

        val failures = files.mapNotNull { file ->
            runCatching {
                val document = readSvg(file.readText())
                sampleTimes.forEach { time -> drawToBitmap(document, time) }
            }.exceptionOrNull()?.let { "${file.name}: $it" }
        }

        assertTrue(failures.isEmpty(), "every icon should draw, but:\n${failures.joinToString("\n")}")
    }
}

private fun drawToBitmap(document: SvgDocument, time: Float) {
    val bitmap = ImageBitmap(48, 48)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap), Size(48f, 48f)) {
        drawSvg(document, time, Color.White)
    }
}

/** Times across the icons' durations, including ones between keyframes. */
private val sampleTimes = listOf(0f, 0.37f, 1.1f, 2.9f, 7.3f)
