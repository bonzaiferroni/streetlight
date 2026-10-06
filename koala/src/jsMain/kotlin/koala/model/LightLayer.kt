package koala.model

import js.buffer.ArrayBuffer
import js.typedarrays.Float32Array
import kampfire.model.GeoPoint
import koala.external.CustomLayerInterface
import koala.external.CustomRenderMethodInput
import koala.external.maplibregl
import web.gl.WebGL2RenderingContext
import web.gl.WebGL2RenderingContext.Companion.ARRAY_BUFFER
import web.gl.WebGL2RenderingContext.Companion.BLEND
import web.gl.WebGL2RenderingContext.Companion.DYNAMIC_DRAW
import web.gl.WebGL2RenderingContext.Companion.FLOAT
import web.gl.WebGL2RenderingContext.Companion.FRAGMENT_SHADER
import web.gl.WebGL2RenderingContext.Companion.ONE
import web.gl.WebGL2RenderingContext.Companion.ONE_MINUS_SRC_ALPHA
import web.gl.WebGL2RenderingContext.Companion.POINTS
import web.gl.WebGL2RenderingContext.Companion.VERTEX_SHADER
import web.gl.WebGLBuffer
import web.gl.WebGLProgram
import web.gl.WebGLUniformLocation
import web.gl.WebGLVertexArrayObject
import web.performance.performance
import web.device.devicePixelRatio
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.tan

/** A map layer that draws each marker's light as a twinkling point, beneath the HTML markers. */
class LightLayer : CustomLayerInterface {
    override val id = "marker-lights"
    override val type = "custom"
    override val renderingMode = "2d"

    private var data = FloatArray(INITIAL_CAPACITY * FLOATS_PER_LIGHT)
    private var lightCount = 0
    private val freeSlots = ArrayDeque<Int>()
    private var isDirty = false

    private var jsMap: maplibregl.Map? = null
    private var program: WebGLProgram? = null
    private var vertexArray: WebGLVertexArrayObject? = null
    private var buffer: WebGLBuffer? = null
    private var matrixLocation: WebGLUniformLocation? = null
    private var timeLocation: WebGLUniformLocation? = null
    private var pointSizeLocation: WebGLUniformLocation? = null

    private val startTime = performance.now()

    /** The seconds since the layer was created, the clock the shader animates by. */
    internal val time get() = ((performance.now() - startTime) / 1000).toFloat()

    /** Adds a light at [position], [phase] seconds into its twinkle, at [opacity]. */
    internal fun allocate(position: GeoPoint, phase: Float, opacity: Float): LightHandle {
        val slot = freeSlots.removeFirstOrNull() ?: lightCount++.also { ensureCapacity(lightCount) }
        val offset = slot * FLOATS_PER_LIGHT
        data[offset + PHASE] = phase
        data[offset + FROM_OPACITY] = opacity
        data[offset + TO_OPACITY] = opacity
        data[offset + CHANGED_AT] = time
        writePosition(slot, position)
        return LightHandle(this, slot)
    }

    internal fun writePosition(slot: Int, position: GeoPoint) {
        val offset = slot * FLOATS_PER_LIGHT
        data[offset + X] = mercatorXOf(position.lng).toFloat()
        data[offset + Y] = mercatorYOf(position.lat).toFloat()
        isDirty = true
    }

    /** Fades the light in [slot] from its current opacity to [opacity]. */
    internal fun writeOpacity(slot: Int, opacity: Float) {
        val offset = slot * FLOATS_PER_LIGHT
        val now = time
        val progress = ((now - data[offset + CHANGED_AT]) / FADE_SECONDS).coerceIn(0f, 1f)
        val from = data[offset + FROM_OPACITY]
        data[offset + FROM_OPACITY] = from + (data[offset + TO_OPACITY] - from) * progress
        data[offset + TO_OPACITY] = opacity
        data[offset + CHANGED_AT] = now
        isDirty = true
    }

    internal fun release(slot: Int) {
        val offset = slot * FLOATS_PER_LIGHT
        data[offset + FROM_OPACITY] = 0f
        data[offset + TO_OPACITY] = 0f
        freeSlots.addLast(slot)
        isDirty = true
    }

    private fun ensureCapacity(count: Int) {
        if (count * FLOATS_PER_LIGHT <= data.size) return
        data = data.copyOf(data.size * 2)
    }

    override fun onAdd(map: maplibregl.Map, gl: WebGL2RenderingContext) {
        jsMap = map

        val program = gl.createProgram()
        gl.attachShader(program, compileShader(gl, VERTEX_SHADER, LIGHT_VERTEX_SHADER))
        gl.attachShader(program, compileShader(gl, FRAGMENT_SHADER, LIGHT_FRAGMENT_SHADER))
        gl.linkProgram(program)
        gl.getProgramInfoLog(program)?.takeIf { it.isNotEmpty() }?.let { console.error("light program: $it") }
        this.program = program
        matrixLocation = gl.getUniformLocation(program, "u_matrix")
        timeLocation = gl.getUniformLocation(program, "u_time")
        pointSizeLocation = gl.getUniformLocation(program, "u_pointSize")

        // describe the vertex layout
        val buffer = gl.createBuffer()
        val vertexArray = gl.createVertexArray()
        gl.bindVertexArray(vertexArray)
        gl.bindBuffer(ARRAY_BUFFER, buffer)
        val stride = FLOATS_PER_LIGHT * Float.SIZE_BYTES
        gl.enableVertexAttribArray(POSITION_ATTRIBUTE)
        gl.vertexAttribPointer(POSITION_ATTRIBUTE, 2, FLOAT, false, stride, X * Float.SIZE_BYTES)
        gl.enableVertexAttribArray(PHASE_ATTRIBUTE)
        gl.vertexAttribPointer(PHASE_ATTRIBUTE, 1, FLOAT, false, stride, PHASE * Float.SIZE_BYTES)
        gl.enableVertexAttribArray(FADE_ATTRIBUTE)
        gl.vertexAttribPointer(FADE_ATTRIBUTE, 3, FLOAT, false, stride, FROM_OPACITY * Float.SIZE_BYTES)
        gl.bindVertexArray(null)
        this.buffer = buffer
        this.vertexArray = vertexArray

        isDirty = true
    }

    override fun onRemove(map: maplibregl.Map, gl: WebGL2RenderingContext) {
        gl.deleteVertexArray(vertexArray)
        gl.deleteBuffer(buffer)
        gl.deleteProgram(program)
        jsMap = null
    }

    override fun prerender(gl: WebGL2RenderingContext, options: CustomRenderMethodInput) {
        if (!isDirty) return
        isDirty = false
        gl.bindBuffer(ARRAY_BUFFER, buffer)
        gl.bufferData(ARRAY_BUFFER, data.unsafeCast<Float32Array<ArrayBuffer>>(), DYNAMIC_DRAW)
    }

    override fun render(gl: WebGL2RenderingContext, options: CustomRenderMethodInput) {
        if (lightCount == 0) return

        gl.useProgram(program)
        gl.uniformMatrix4fv(matrixLocation, false, options.defaultProjectionData.mainMatrix)
        gl.uniform1f(timeLocation, time)
        gl.uniform1f(pointSizeLocation, (LIGHT_SIZE_PX * devicePixelRatio).toFloat())
        gl.enable(BLEND)
        gl.blendFunc(ONE, ONE_MINUS_SRC_ALPHA)
        gl.bindVertexArray(vertexArray)
        gl.drawArrays(POINTS, 0, lightCount)
        gl.bindVertexArray(null)

        // keep the twinkle running
        jsMap?.triggerRepaint()
    }
}

/** A marker's hold on its light in a [LightLayer]. */
internal class LightHandle(
    private val layer: LightLayer,
    private val slot: Int,
) {
    fun move(position: GeoPoint) = layer.writePosition(slot, position)
    fun setOpacity(opacity: Float) = layer.writeOpacity(slot, opacity)
    fun dispose() = layer.release(slot)
}

private fun compileShader(gl: WebGL2RenderingContext, type: web.gl.GLenum, source: String) =
    gl.createShader(type)!!.also { shader ->
        gl.shaderSource(shader, source)
        gl.compileShader(shader)
        gl.getShaderInfoLog(shader)?.takeIf { it.isNotEmpty() }?.let { console.error("light shader: $it") }
    }

private fun mercatorXOf(lng: Double) = (180 + lng) / 360

private fun mercatorYOf(lat: Double) = (180 - (180 / PI * ln(tan(PI / 4 + lat * PI / 360)))) / 360

private const val INITIAL_CAPACITY = 256
private const val FLOATS_PER_LIGHT = 6
private const val X = 0
private const val Y = 1
private const val PHASE = 2
private const val FROM_OPACITY = 3
private const val TO_OPACITY = 4
private const val CHANGED_AT = 5

private const val POSITION_ATTRIBUTE = 0
private const val PHASE_ATTRIBUTE = 1
private const val FADE_ATTRIBUTE = 2

private const val LIGHT_SIZE_PX = 8.0
private const val FADE_SECONDS = 0.3f

// language="GLSL"
private val LIGHT_VERTEX_SHADER = """#version 300 es
uniform mat4 u_matrix;
uniform float u_time;
uniform float u_pointSize;

layout(location = $POSITION_ATTRIBUTE) in vec2 a_position;
layout(location = $PHASE_ATTRIBUTE) in float a_phase;
layout(location = $FADE_ATTRIBUTE) in vec3 a_fade; // from opacity, to opacity, changed at

out float v_alpha;

const float TWINKLE_SECONDS = 2.4;
const float FADE_SECONDS = $FADE_SECONDS;
const float TAU = 6.2831853;

void main() {
    gl_Position = u_matrix * vec4(a_position, 0.0, 1.0);
    gl_PointSize = u_pointSize;

    float twinkle = 0.4 - 0.2 * cos(TAU * (u_time + a_phase) / TWINKLE_SECONDS);
    float fade = mix(a_fade.x, a_fade.y, clamp((u_time - a_fade.z) / FADE_SECONDS, 0.0, 1.0));
    v_alpha = twinkle * fade;
}
"""

// language="GLSL"
private const val LIGHT_FRAGMENT_SHADER = """#version 300 es
precision mediump float;

in float v_alpha;
out vec4 fragColor;

void main() {
    float edge = 1.0 - smoothstep(0.4, 0.5, length(gl_PointCoord - 0.5));
    float alpha = v_alpha * edge;
    fragColor = vec4(vec3(alpha), alpha);
}
"""
