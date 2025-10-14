package streetlight.app.services

//import org.jetbrains.skia.Canvas
//import org.jetbrains.skia.Color
//import org.jetbrains.skia.EncodedImageFormat
//import org.jetbrains.skia.Font
//import org.jetbrains.skia.Image
//import org.jetbrains.skia.Paint
//import org.jetbrains.skia.PaintMode
//import org.jetbrains.skia.RRect
//import org.jetbrains.skia.Rect
//import org.jetbrains.skia.SamplingMode
//import org.jetbrains.skia.Surface
//import org.jetbrains.skia.Typeface
//import kotlin.math.roundToInt
//
//class SkiaOverlay(
//    width: Int,
//    height: Int,
//    scale: Float = 1f,
//    background: Int = Color.TRANSPARENT
//) : AutoCloseable {
//
//    private val pixelW = (width * scale).roundToInt()
//    private val pixelH = (height * scale).roundToInt()
//    private val surface: Surface = Surface.Companion.makeRasterN32Premul(pixelW, pixelH)
//    private val canvas: Canvas = surface.canvas.apply {
//        clear(background)
//        if (scale != 1f) scale(scale, scale)
//    }
//
//    fun clear(color: Int = Color.TRANSPARENT): SkiaOverlay {
//        canvas.clear(color)
//        return this
//    }
//
//    fun addText(
//        text: String,
//        x: Float,
//        y: Float,
//        size: Float = 18f,
//        color: Int = Color.BLACK,
//        typeface: Typeface? = null,
//        antialias: Boolean = true
//    ): SkiaOverlay {
//        val paint = Paint().apply {
//            this.color = color
//            isAntiAlias = antialias
//        }
//        val font = Font(typeface, size)
//        canvas.drawString(text, x, y, font, paint)
//        return this
//    }
//
//    fun addRect(
//        x: Float,
//        y: Float,
//        w: Float,
//        h: Float,
//        fillColor: Int? = Color.makeARGB(0x00, 0, 0, 0),
//        strokeColor: Int? = null,
//        strokeWidth: Float = 2f,
//        radius: Float = 0f,
//        antialias: Boolean = true
//    ): SkiaOverlay {
//        if (fillColor != null) {
//            val p = Paint().apply {
//                color = fillColor
//                mode = PaintMode.FILL
//                isAntiAlias = antialias
//            }
//            if (radius > 0f) {
//                val rr = RRect.Companion.makeXYWH(x, y, w, h, radius, radius)
//                canvas.drawRRect(rr, p)
//            } else {
//                canvas.drawRect(Rect.Companion.makeXYWH(x, y, w, h), p)
//            }
//        }
//        if (strokeColor != null && strokeWidth > 0f) {
//            val p = Paint().apply {
//                color = strokeColor
//                mode = PaintMode.STROKE
//                this.strokeWidth = strokeWidth
//                isAntiAlias = antialias
//            }
//            if (radius > 0f) {
//                val rr = RRect.Companion.makeXYWH(x, y, w, h, radius, radius)
//                canvas.drawRRect(rr, p)
//            } else {
//                canvas.drawRect(Rect.Companion.makeXYWH(x, y, w, h), p)
//            }
//        }
//        return this
//    }
//
//    fun addImage(
//        imageBytes: ByteArray,
//        dstX: Float,
//        dstY: Float,
//        dstW: Float? = null,
//        dstH: Float? = null,
//        antialias: Boolean = true
//    ): SkiaOverlay {
//        val img = Image.Companion.makeFromEncoded(imageBytes)
//        return addImage(img, dstX, dstY, dstW, dstH, antialias)
//    }
//
//    fun addImage(
//        image: Image,
//        dstX: Float,
//        dstY: Float,
//        dstW: Float? = null,
//        dstH: Float? = null,
//        antialias: Boolean = true
//    ): SkiaOverlay {
//        val sampling = if (antialias) SamplingMode.Companion.LINEAR else SamplingMode.Companion.DEFAULT
//        val w = dstW ?: image.width.toFloat()
//        val h = dstH ?: image.height.toFloat()
//        val dst = Rect.Companion.makeXYWH(dstX, dstY, w, h)
//        val src = Rect.Companion.makeWH(image.width.toFloat(), image.height.toFloat())
//        canvas.drawImageRect(image, src, dst, sampling, null, true)
//        return this
//    }
//
//    fun snapshotImage(): Image {
//        return surface.makeImageSnapshot()
//    }
//
//    fun toPNG(quality: Int = 100): ByteArray {
//        val img = snapshotImage()
//        val data = img.encodeToData(EncodedImageFormat.PNG, quality)
//            ?: error("Failed to encode PNG, ye scurvy codec!")
//        return data.bytes
//    }
//
//    fun toJPEG(quality: Int = 95): ByteArray {
//        val img = snapshotImage()
//        val data = img.encodeToData(EncodedImageFormat.JPEG, quality)
//            ?: error("Failed to encode JPEG, ye bilge rat!")
//        return data.bytes
//    }
//
//    override fun close() {
//        // Skia objects are native-backed; help GC by closing where possible
//        surface.close()
//    }
//}