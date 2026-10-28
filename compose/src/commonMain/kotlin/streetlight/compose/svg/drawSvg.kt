package streetlight.compose.svg

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawTransform
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser

/**
 * Draws [document] as it is at [time] seconds, its view box fit within this scope at its own aspect and centered, as
 * an svg's default `preserveAspectRatio` places it.
 *
 * Every shape is drawn in [color], at its own opacity, as the web's icon mask uses an icon's alpha alone. A `mask`
 * is not applied.
 */
internal fun DrawScope.drawSvg(document: SvgDocument, time: Float, color: Color) {
    val box = document.viewBox
    val scale = minOf(size.width / box.width, size.height / box.height)
    withTransform({
        translate((size.width - box.width * scale) / 2, (size.height - box.height * scale) / 2)
        scale(scale, scale, pivot = Offset.Zero)
        translate(-box.left, -box.top)
    }) {
        drawNode(document.root, document, time, color, SvgStyle())
    }
}

/** The presentation an element inherits from its ancestors, and its accumulated [opacity]. */
private data class SvgStyle(
    val fill: String = "black",
    val stroke: String = "none",
    val strokeWidth: Float = 1f,
    val cap: StrokeCap = StrokeCap.Butt,
    val join: StrokeJoin = StrokeJoin.Miter,
    val fillOpacity: Float = 1f,
    val strokeOpacity: Float = 1f,
    val opacity: Float = 1f,
    val dashArray: String? = null,
    val dashOffset: Float = 0f,
)

private fun DrawScope.drawNode(node: SvgNode, document: SvgDocument, time: Float, color: Color, parentStyle: SvgStyle) {
    if (node.name in unrenderedNames) return
    val style = parentStyle.inheritedBy(node, time)
    val transform = node.transformAt(time)
    val motion = node.motionAt(time)

    // the motion is applied outside the element's own transform, as SMIL specifies
    withTransform({
        motion?.let { translate(it.x, it.y) }
        transform?.let { applyTransform(it) }
    }) {
        withClip(node.attributeAt("clip-path", time), document, time) {
            when (node.name) {
                "svg", "g" -> node.children.forEach { drawNode(it, document, time, color, style) }
                else -> shapeOf(node, time)?.let { drawShape(it, node, time, color, style) }
            }
        }
    }
}

private fun DrawScope.drawShape(path: Path, node: SvgNode, time: Float, color: Color, style: SvgStyle) {
    val alpha = color.alpha * style.opacity
    if (style.fill != "none") {
        drawPath(path, color.copy(alpha = alpha * style.fillOpacity))
    }
    if (style.stroke != "none" && style.strokeWidth > 0f) {
        val stroke = Stroke(style.strokeWidth, cap = style.cap, join = style.join)
        drawPath(dashedPathOf(path, node, time, style), color.copy(alpha = alpha * style.strokeOpacity), style = stroke)
    }
}

/**
 * Runs [block] clipped to the clip path [reference] names, as `url(#id)`, or unclipped. The region is the union of the
 * clip path's shapes, each filled by its own `clip-rule`, so an `evenodd` shape can clip to what lies outside it.
 */
private fun DrawScope.withClip(reference: String?, document: SvgDocument, time: Float, block: DrawScope.() -> Unit) {
    val id = reference?.substringAfter("url(#", "")?.substringBefore(")")
    val clip = id?.let { document.clips[it] }
    if (clip == null) return block()

    val regions = clip.children.mapNotNull { child ->
        val shape = shapeOf(child, time) ?: return@mapNotNull null
        Path().apply {
            addPath(shape)
            fillType = fillTypeOf(child.attributeAt("clip-rule", time))
        }
    }
    val region = regions.reduceOrNull { union, next -> Path().apply { op(union, next, PathOperation.Union) } } ?: Path()
    clipPath(region) { block() }
}

/** Applies the transform functions of [text] in order, such as `translate(2 3) rotate(45 12 12)`. */
private fun DrawTransform.applyTransform(text: String) {
    transformRegex.findAll(text).forEach { match ->
        val args = numbersOf(match.groupValues[2])
        when (match.groupValues[1]) {
            "translate" -> translate(args.getOrElse(0) { 0f }, args.getOrElse(1) { 0f })
            "rotate" -> rotate(args.getOrElse(0) { 0f }, Offset(args.getOrElse(1) { 0f }, args.getOrElse(2) { 0f }))
            "scale" -> {
                val x = args.getOrElse(0) { 1f }
                scale(x, args.getOrElse(1) { x }, pivot = Offset.Zero)
            }
            "matrix" -> if (args.size == 6) transform(Matrix().apply {
                values[Matrix.ScaleX] = args[0]
                values[Matrix.SkewY] = args[1]
                values[Matrix.SkewX] = args[2]
                values[Matrix.ScaleY] = args[3]
                values[Matrix.TranslateX] = args[4]
                values[Matrix.TranslateY] = args[5]
            })
        }
    }
}

/** The shape of [node] at [time], or `null` for an element that is not a shape. */
private fun shapeOf(node: SvgNode, time: Float): Path? {
    node.staticShape?.let { return it }
    fun number(name: String) = node.attributeAt(name, time)?.trim()?.toFloatOrNull() ?: 0f

    val path = when (node.name) {
        "path" -> PathParser().parsePathString(node.attributeAt("d", time) ?: return null).toPath()
        "circle" -> Path().apply { addOval(Rect(Offset(number("cx"), number("cy")), number("r"))) }
        "line" -> Path().apply {
            moveTo(number("x1"), number("y1"))
            lineTo(number("x2"), number("y2"))
        }
        "rect" -> Path().apply {
            val rect = Rect(number("x"), number("y"), number("x") + number("width"), number("y") + number("height"))
            val radius = number("rx")
            addRoundRect(RoundRect(rect, CornerRadius(radius, node.attributeAt("ry", time)?.trim()?.toFloatOrNull() ?: radius)))
        }
        else -> return null
    }
    path.fillType = fillTypeOf(node.attributeAt("fill-rule", time))
    if (!node.isShapeAnimated) node.staticShape = path
    return path
}

/**
 * The dashes of a stroke as their own path, scaled from the element's `pathLength` to the path's own length, or
 * [path] itself when it has none.
 *
 * A dash too short to see is left out, as a browser leaves it. Skia's dash effect would draw it as a dot of the
 * round cap, such as where a trail hidden by its offset ends exactly at a dash.
 */
private fun dashedPathOf(path: Path, node: SvgNode, time: Float, style: SvgStyle): Path {
    val dashes = style.dashArray?.takeIf { it != "none" }?.let { numbersOf(it) } ?: return path
    if (dashes.isEmpty() || dashes.all { it <= 0f }) return path

    val measure = PathMeasure().apply { setPath(path, false) }
    val length = measure.length
    val scale = node.attributeAt("pathLength", time)?.trim()?.toFloatOrNull()?.let { length / it } ?: 1f
    val intervals = (if (dashes.size % 2 == 0) dashes else dashes + dashes).map { it * scale }
    val period = intervals.sum()
    if (period <= 0f) return path

    // walk the pattern from where the offset starts it, taking each dash that shows
    val dashed = Path()
    var position = -(((style.dashOffset * scale) % period + period) % period)
    while (position < length) {
        intervals.forEachIndexed { index, interval ->
            val start = position.coerceAtLeast(0f)
            val end = (position + interval).coerceAtMost(length)
            if (index % 2 == 0 && end - start > MinDashLength) measure.getSegment(start, end, dashed, true)
            position += interval
        }
    }
    return dashed
}

/** The shortest dash drawn, in user units. */
private const val MinDashLength = 0.001f

/** The style [node] sees at [time]: its own presentation attributes over this one. */
private fun SvgStyle.inheritedBy(node: SvgNode, time: Float): SvgStyle {
    fun text(name: String) = node.attributeAt(name, time)?.trim()
    fun number(name: String) = text(name)?.toFloatOrNull()
    return SvgStyle(
        fill = text("fill") ?: fill,
        stroke = text("stroke") ?: stroke,
        strokeWidth = number("stroke-width") ?: strokeWidth,
        cap = when (text("stroke-linecap")) {
            "round" -> StrokeCap.Round
            "square" -> StrokeCap.Square
            "butt" -> StrokeCap.Butt
            else -> cap
        },
        join = when (text("stroke-linejoin")) {
            "round" -> StrokeJoin.Round
            "bevel" -> StrokeJoin.Bevel
            "miter" -> StrokeJoin.Miter
            else -> join
        },
        fillOpacity = number("fill-opacity") ?: fillOpacity,
        strokeOpacity = number("stroke-opacity") ?: strokeOpacity,
        opacity = opacity * (number("opacity") ?: 1f),
        dashArray = text("stroke-dasharray") ?: dashArray,
        dashOffset = number("stroke-dashoffset") ?: dashOffset,
    )
}

/** The fill type a `fill-rule` or `clip-rule` names, `nonzero` unless it is `evenodd`. */
private fun fillTypeOf(rule: String?) = if (rule?.trim() == "evenodd") PathFillType.EvenOdd else PathFillType.NonZero

private val transformRegex = Regex("""(\w+)\s*\(([^)]*)\)""")

/** The elements that are referenced rather than drawn where they stand. */
private val unrenderedNames = setOf("defs", "clipPath", "mask", "title", "desc", "metadata", "style")
