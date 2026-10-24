package streetlight.compose.svg

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.vector.PathParser

/** An SVG ready to draw: its [viewBox], its [root] element, and the clip paths it declares by id. */
internal class SvgDocument(
    val viewBox: Rect,
    val root: SvgNode,
    val clips: Map<String, SvgNode>,
)

/** An element of an [SvgDocument], with its [attributes], its [children] and the [animations] of its attributes. */
internal class SvgNode(
    val name: String,
    val attributes: Map<String, String>,
    val children: List<SvgNode>,
    val animations: List<SvgAnimation>,
) {
    private val animatedAttributes = animations.filterIsInstance<AttributeAnimation>().associateBy { it.attribute }
    private val transformAnimation = animations.filterIsInstance<TransformAnimation>().lastOrNull()
    private val motionAnimation = animations.filterIsInstance<MotionAnimation>().lastOrNull()

    /** The shape of an element whose geometry never animates, built once. */
    var staticShape: Path? = null

    val isShapeAnimated = shapeAttributes.any { it in animatedAttributes }

    /** The value of [attribute] at [time], animated or as written. */
    fun attributeAt(attribute: String, time: Float): String? =
        animatedAttributes[attribute]?.valueAt(time) ?: attributes[attribute]

    /** The transform of the element at [time]: its animated transform, which replaces the written one. */
    fun transformAt(time: Float): String? = transformAnimation?.transformAt(time) ?: attributes["transform"]

    /** The point along its motion path at [time], when it moves along one. */
    fun motionAt(time: Float) = motionAnimation?.pointAt(time)
}

/** The animation of an element's attribute. */
internal sealed interface SvgAnimation

/** An `animate` of [attribute] through [values]. */
internal class AttributeAnimation(
    val attribute: String,
    private val values: List<String>,
    private val timing: SvgTiming,
) : SvgAnimation {
    fun valueAt(time: Float): String = valueOf(values, timing, time)
}

/** An `animateTransform` of [type] through [values], each the arguments of that transform function. */
internal class TransformAnimation(
    private val type: String,
    private val values: List<String>,
    private val timing: SvgTiming,
) : SvgAnimation {
    fun transformAt(time: Float) = "$type(${valueOf(values, timing, time)})"
}

/** An `animateMotion` along [path], through [keyPoints], the fractions of its length at each key time. */
internal class MotionAnimation(
    path: Path,
    private val keyPoints: List<String>,
    private val timing: SvgTiming,
) : SvgAnimation {
    private val measure = PathMeasure().apply { setPath(path, false) }

    fun pointAt(time: Float) = measure.getPosition(measure.length * (valueOf(keyPoints, timing, time).trim().toFloatOrNull() ?: 0f))
}

/** The SVG document of [text], with the animations it declares. */
internal fun readSvg(text: String): SvgDocument {
    val root = readXml(text)
    val clips = mutableMapOf<String, SvgNode>()
    val node = nodeOf(root, clips)
    val box = numbersOf(root.attributes["viewBox"] ?: "0 0 24 24")
    return SvgDocument(Rect(box[0], box[1], box[0] + box[2], box[1] + box[3]), node, clips)
}

private fun nodeOf(element: XmlElement, clips: MutableMap<String, SvgNode>): SvgNode {
    val animations = element.children.mapNotNull { animationOf(it) }
    val children = element.children.filter { it.name !in animationNames }.map { nodeOf(it, clips) }
    val node = SvgNode(element.name, element.attributes, children, animations)
    if (element.name == "clipPath") element.attributes["id"]?.let { clips[it] = node }
    return node
}

private fun animationOf(element: XmlElement): SvgAnimation? {
    val attributes = element.attributes
    val values = attributes["values"]?.split(';')?.filter { it.isNotBlank() }
        ?: listOfNotNull(attributes["from"], attributes["to"]).takeIf { it.size == 2 }
    val timing = timingOf(attributes)
    return when (element.name) {
        "animate" -> AttributeAnimation(attributes["attributeName"] ?: return null, values ?: return null, timing)
        "animateTransform" -> TransformAnimation(attributes["type"] ?: "translate", values ?: return null, timing)
        "animateMotion" -> MotionAnimation(
            path = PathParser().parsePathString(attributes["path"] ?: return null).toPath(),
            keyPoints = attributes["keyPoints"]?.split(';') ?: listOf("0", "1"),
            timing = timing,
        )
        else -> null
    }
}

/** The value of [values] at [time]. */
private fun valueOf(values: List<String>, timing: SvgTiming, time: Float): String {
    val (index, fraction) = timing.keyframeAt(time, values.size)
    return lerpValue(values[index], values[(index + 1).coerceAtMost(values.lastIndex)], fraction)
}

private val animationNames = setOf("animate", "animateTransform", "animateMotion", "set")

/** The attributes that set a shape's geometry. */
private val shapeAttributes = setOf("d", "cx", "cy", "r", "x", "y", "x1", "y1", "x2", "y2", "width", "height", "rx")
