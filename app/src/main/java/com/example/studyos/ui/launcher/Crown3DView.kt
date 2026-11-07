package com.example.studyos.ui.launcher

import android.content.Context
import android.content.ContextWrapper
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
internal fun Crown3DView(
    modifier: Modifier = Modifier,
    motionMode: Int = 0,
    effectBrightness: Float = 0.75f
) {
    var glView by remember {
        mutableStateOf<CrownGLSurfaceView?>(null)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            CrownGLSurfaceView(context).also {
                glView = it
            }
        },
        update = { view ->
            view.setMotionMode(motionMode)
            view.setEffectBrightness(effectBrightness)
        }
    )

    val lifecycleOwner = remember(glView) {
        glView?.context?.findCrownLifecycleOwner()
    }

    DisposableEffect(glView, lifecycleOwner) {
        val view = glView

        if (view == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> view.onResume()
                    Lifecycle.Event.ON_PAUSE -> view.onPause()
                    else -> Unit
                }
            }

            lifecycleOwner?.lifecycle?.addObserver(observer)

            if (
                lifecycleOwner == null ||
                lifecycleOwner.lifecycle.currentState
                    .isAtLeast(Lifecycle.State.RESUMED)
            ) {
                view.onResume()
            }

            onDispose {
                lifecycleOwner?.lifecycle?.removeObserver(observer)
                view.onPause()
            }
        }
    }
}

private fun Context.findCrownLifecycleOwner(): LifecycleOwner? {
    var current: Context = this

    while (current is ContextWrapper) {
        if (current is LifecycleOwner) {
            return current
        }
        current = current.baseContext
    }

    return current as? LifecycleOwner
}

private class CrownGLSurfaceView(
    context: Context
) : GLSurfaceView(context) {

    private val crownRenderer = Crown3DRenderer()

    init {
        setEGLContextClientVersion(2)

        // RGBA transparency allows the 3D crown to sit over the mascot.
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        holder.setFormat(PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true)
        setBackgroundColor(android.graphics.Color.TRANSPARENT)

        setPreserveEGLContextOnPause(true)

        setRenderer(crownRenderer)

        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun setMotionMode(mode: Int) {
        val safeMode = mode.coerceIn(0, 2)

        crownRenderer.motionMode = safeMode

        renderMode = if (safeMode == 2) {
            RENDERMODE_WHEN_DIRTY
        } else {
            RENDERMODE_CONTINUOUSLY
        }

        requestRender()
    }

    fun setEffectBrightness(value: Float) {
        crownRenderer.effectBrightness = value.coerceIn(0f, 1f)
        requestRender()
    }
}

// =====================================================================
// MATERIAL AND MESH DATA
// =====================================================================

private data class CrownMaterial(
    val color: FloatArray,
    val specular: Float,
    val shininess: Float,
    val emission: Float
)

private data class CrownPart(
val material: CrownMaterial,
val vertices: FloatBuffer,
val vertexCount: Int
)

private data class CrownAuraOrb(
val x: Float,
val y: Float,
val z: Float,
val phase: Float,
val baseScale: Float
)

private val crownAuraOrbs: List<CrownAuraOrb> = listOf(
CrownAuraOrb(-0.76f, 0.14f, 0.42f, 0.15f, 0.085f),
CrownAuraOrb(-0.62f, 0.48f, 0.68f, 1.20f, 0.075f),
CrownAuraOrb(-0.28f, 0.66f, 0.24f, 2.10f, 0.070f),
CrownAuraOrb(0.28f, 0.66f, 0.24f, 3.20f, 0.070f),
CrownAuraOrb(0.62f, 0.48f, 0.68f, 4.30f, 0.075f),
CrownAuraOrb(0.76f, 0.14f, 0.42f, 5.40f, 0.085f)
)

private val crownAuraHazeMaterial = CrownMaterial(
floatArrayOf(0.58f, 0.20f, 1.0f, 0.24f),
0.45f,
28f,
1.25f
)

private val crownAuraCoreMaterial = CrownMaterial(
floatArrayOf(0.79f, 0.53f, 1.0f, 0.96f),
1.20f,
90f,
1.05f
)

private fun buildCrownAuraSphere(material: CrownMaterial): CrownPart? {
val mesh = CrownMeshBuilder(material)
addSphere(
mesh = mesh,
center = V3(0f, 0f, 0f),
radius = 1f,
segments = 20,
rings = 14
)
return mesh.build()
}

private val crownAuraHazePart: CrownPart? by lazy(LazyThreadSafetyMode.NONE) {
buildCrownAuraSphere(crownAuraHazeMaterial)
}

private val crownAuraCorePart: CrownPart? by lazy(LazyThreadSafetyMode.NONE) {
buildCrownAuraSphere(crownAuraCoreMaterial)
}

private data class V3(
    val x: Float,
    val y: Float,
    val z: Float
) {
    operator fun plus(other: V3) = V3(
        x + other.x,
        y + other.y,
        z + other.z
    )

    operator fun minus(other: V3) = V3(
        x - other.x,
        y - other.y,
        z - other.z
    )

    operator fun times(value: Float) = V3(
        x * value,
        y * value,
        z * value
    )

    fun cross(other: V3) = V3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun normalized(): V3 {
        val length = sqrt(x * x + y * y + z * z)

        if (length < 0.000001f) {
            return V3(0f, 1f, 0f)
        }

        return V3(
            x / length,
            y / length,
            z / length
        )
    }
}

private class CrownMeshBuilder(
    private val material: CrownMaterial
) {
    private val values = ArrayList<Float>()

    private fun vertex(position: V3, normal: V3) {
        values.add(position.x)
        values.add(position.y)
        values.add(position.z)

        values.add(normal.x)
        values.add(normal.y)
        values.add(normal.z)
    }

    fun triangle(
        a: V3,
        b: V3,
        c: V3,
        normalA: V3? = null,
        normalB: V3? = null,
        normalC: V3? = null
    ) {
        val faceNormal = (b - a).cross(c - a).normalized()

        vertex(a, normalA ?: faceNormal)
        vertex(b, normalB ?: faceNormal)
        vertex(c, normalC ?: faceNormal)
    }

    fun build(): CrownPart? {
        if (values.isEmpty()) return null

        val array = FloatArray(values.size) {
            values[it]
        }

        val buffer = ByteBuffer
            .allocateDirect(array.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

        buffer.put(array)
        buffer.position(0)

        return CrownPart(
            material = material,
            vertices = buffer,
            vertexCount = array.size / 6
        )
    }
}

private class CrownModelBuilder {

    private val meshes =
        LinkedHashMap<String, CrownMeshBuilder>()

    fun mesh(
        name: String,
        material: CrownMaterial
    ): CrownMeshBuilder {
        return meshes.getOrPut(name) {
            CrownMeshBuilder(material)
        }
    }

    fun build(): List<CrownPart> {
        return meshes.values.mapNotNull {
            it.build()
        }
    }
}

// =====================================================================
// ROYAL PURPLE METALLIC MATERIALS
// =====================================================================

private object CrownMaterials {

    val BODY = CrownMaterial(
        floatArrayOf(0.30f, 0.105f, 0.54f, 1f),
        1.15f,
        72f,
        0.045f
    )

    val BODY_LIGHT = CrownMaterial(
        floatArrayOf(0.49f, 0.245f, 0.72f, 1f),
        1.25f,
        92f,
        0.055f
    )

    val BODY_DARK = CrownMaterial(
        floatArrayOf(0.075f, 0.025f, 0.135f, 1f),
        0.85f,
        40f,
        0.015f
    )

    val BODY_MID = CrownMaterial(
        floatArrayOf(0.235f, 0.075f, 0.42f, 1f),
        1.10f,
        64f,
        0.035f
    )

    val PLATINUM = CrownMaterial(
        floatArrayOf(0.77f, 0.68f, 0.91f, 1f),
        1.45f,
        112f,
        0.025f
    )

    val PLATINUM_BRIGHT = CrownMaterial(
        floatArrayOf(0.96f, 0.89f, 1f, 1f),
        1.55f,
        128f,
        0.035f
    )

    val PLATINUM_DARK = CrownMaterial(
        floatArrayOf(0.31f, 0.205f, 0.43f, 1f),
        1.10f,
        82f,
        0.015f
    )

    val GEM_TABLE = CrownMaterial(
        floatArrayOf(0.77f, 0.48f, 0.98f, 1f),
        1.65f,
        145f,
        0.11f
    )

    val GEM_LIGHT = CrownMaterial(
        floatArrayOf(0.73f, 0.40f, 0.96f, 1f),
        1.55f,
        120f,
        0.10f
    )

    val GEM_MID = CrownMaterial(
        floatArrayOf(0.41f, 0.105f, 0.72f, 1f),
        1.45f,
        104f,
        0.055f
    )

    val GEM_DARK = CrownMaterial(
        floatArrayOf(0.17f, 0.035f, 0.35f, 1f),
        1.35f,
        90f,
        0.025f
    )

    val GEM_VIOLET = CrownMaterial(
        floatArrayOf(0.55f, 0.20f, 0.86f, 1f),
        1.50f,
        116f,
        0.075f
    )
}

// =====================================================================
// OPENGL ES 2.0 RENDERER
// =====================================================================

private class Crown3DRenderer : GLSurfaceView.Renderer {

    @Volatile
    var motionMode: Int = 0

    @Volatile
    var effectBrightness: Float = 0.75f

    private var program = 0
    private var parts: List<CrownPart> = emptyList()

    private var positionHandle = 0
    private var normalHandle = 0

    private var mvpHandle = 0
    private var modelViewHandle = 0
    private var colorHandle = 0
    private var specularHandle = 0
    private var shininessHandle = 0
    private var emissionHandle = 0
    private var brightnessHandle = 0
    private var lightHandle = 0
    private var fillLightHandle = 0

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var startTime = 0L

    private val vertexShader = """
        uniform mat4 uMvpMatrix;
        uniform mat4 uModelViewMatrix;

        attribute vec3 aPosition;
        attribute vec3 aNormal;

        varying vec3 vNormal;
        varying vec3 vEyePosition;

        void main() {
            vec4 eyePosition =
                uModelViewMatrix * vec4(aPosition, 1.0);

            vEyePosition = eyePosition.xyz;

            vNormal = normalize(
                mat3(uModelViewMatrix) * aNormal
            );

            gl_Position =
                uMvpMatrix * vec4(aPosition, 1.0);
        }
    """.trimIndent()

    private val fragmentShader = """
        precision mediump float;

        uniform vec4 uBaseColor;
        uniform float uSpecular;
        uniform float uShininess;
        uniform float uEmission;
        uniform float uBrightness;

        uniform vec3 uLightDirection;
        uniform vec3 uFillDirection;

        varying vec3 vNormal;
        varying vec3 vEyePosition;

        void main() {

            vec3 N = normalize(vNormal);
            vec3 L = normalize(uLightDirection);
            vec3 F = normalize(uFillDirection);
            vec3 V = normalize(-vEyePosition);

            vec3 H = normalize(L + V);

            float diffuse = max(dot(N, L), 0.0);
            float fill = max(dot(N, F), 0.0);

            float specular = pow(
                max(dot(N, H), 0.0),
                max(uShininess, 1.0)
            );

            float fresnel = pow(
                1.0 - max(dot(N, V), 0.0),
                3.0
            );

            vec3 base = uBaseColor.rgb;

            vec3 surface =
                base * (
                    0.23 +
                    diffuse * 0.72 +
                    fill * 0.16
                );

            vec3 reflections =
                vec3(1.0) *
                specular *
                uSpecular *
                (0.20 + uBrightness * 0.95);

            vec3 innerGlow =
                base *
                uEmission *
                (0.40 + uBrightness);

            vec3 edgeLight =
                vec3(0.23, 0.11, 0.39) *
                fresnel *
                (0.08 + uBrightness * 0.25);

            vec3 finalColor =
                surface +
                reflections +
                innerGlow +
                edgeLight;

            gl_FragColor = vec4(
                clamp(finalColor, 0.0, 1.0),
                uBaseColor.a
            );
        }
    """.trimIndent()

    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?
    ) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(
            GLES20.GL_SRC_ALPHA,
            GLES20.GL_ONE_MINUS_SRC_ALPHA
        )

        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_DITHER)

        program = createProgram(
            vertexShader,
            fragmentShader
        )

        GLES20.glUseProgram(program)

        positionHandle =
            GLES20.glGetAttribLocation(program, "aPosition")

        normalHandle =
            GLES20.glGetAttribLocation(program, "aNormal")

        mvpHandle =
            GLES20.glGetUniformLocation(program, "uMvpMatrix")

        modelViewHandle =
            GLES20.glGetUniformLocation(program, "uModelViewMatrix")

        colorHandle =
            GLES20.glGetUniformLocation(program, "uBaseColor")

        specularHandle =
            GLES20.glGetUniformLocation(program, "uSpecular")

        shininessHandle =
            GLES20.glGetUniformLocation(program, "uShininess")

        emissionHandle =
            GLES20.glGetUniformLocation(program, "uEmission")

        brightnessHandle =
            GLES20.glGetUniformLocation(program, "uBrightness")

        lightHandle =
            GLES20.glGetUniformLocation(program, "uLightDirection")

        fillLightHandle =
            GLES20.glGetUniformLocation(program, "uFillDirection")

        parts = buildCrownModel()

startTime = System.nanoTime()
    }

    override fun onSurfaceChanged(
        gl: GL10?,
        width: Int,
        height: Int
    ) {
        val safeWidth = max(1, width)
        val safeHeight = max(1, height)

        GLES20.glViewport(
            0,
            0,
            safeWidth,
            safeHeight
        )

        Matrix.perspectiveM(
            projectionMatrix,
            0,
            36f,
            safeWidth.toFloat() / safeHeight.toFloat(),
            0.1f,
            20f
        )

        Matrix.setLookAtM(
            viewMatrix,
            0,
            0f, 0f, 2.10f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(
            GLES20.GL_COLOR_BUFFER_BIT or
                GLES20.GL_DEPTH_BUFFER_BIT
        )

        if (program == 0 || parts.isEmpty()) return

        GLES20.glUseProgram(program)

        val mode = motionMode.coerceIn(0, 2)
        val seconds =
            (System.nanoTime() - startTime).toFloat() /
                1_000_000_000f

        val yaw: Float
        val tilt: Float
        val bob: Float

        when (mode) {
            2 -> {
                yaw = 0f
                tilt = -7f
                bob = 0f
            }

            1 -> {
                yaw = sin(seconds * 0.38f) * 4f
                tilt = -7f + sin(seconds * 0.35f) * 0.7f
                bob = sin(seconds * 0.65f) * 0.008f
            }

            else -> {
                yaw = sin(seconds * 0.72f) * 13f
                tilt = -7f + sin(seconds * 0.48f) * 2f
                bob = sin(seconds * 1.15f) * 0.018f
            }
        }

        Matrix.setIdentityM(modelMatrix, 0)

        Matrix.translateM(
            modelMatrix,
            0,
            0f,
            bob,
            0f
        )

        Matrix.rotateM(
            modelMatrix,
            0,
            yaw,
            0f,
            1f,
            0f
        )

        Matrix.rotateM(
            modelMatrix,
            0,
            tilt,
            1f,
            0f,
            0f
        )

        Matrix.multiplyMM(
            modelViewMatrix,
            0,
            viewMatrix,
            0,
            modelMatrix,
            0
        )

        Matrix.multiplyMM(
            mvpMatrix,
            0,
            projectionMatrix,
            0,
            modelViewMatrix,
            0
        )

        GLES20.glUniformMatrix4fv(
            mvpHandle,
            1,
            false,
            mvpMatrix,
            0
        )

        GLES20.glUniformMatrix4fv(
            modelViewHandle,
            1,
            false,
            modelViewMatrix,
            0
        )

        // The key light moves slowly to create travelling reflections.
        val lightX = -0.48f + sin(seconds * 0.42f) * 0.18f
        val lightY = 0.78f
        val lightZ = 0.90f

        GLES20.glUniform3f(
            lightHandle,
            lightX,
            lightY,
            lightZ
        )

        GLES20.glUniform3f(
            fillLightHandle,
            0.55f,
            0.20f,
            0.82f
        )

        GLES20.glUniform1f(
            brightnessHandle,
            effectBrightness.coerceIn(0f, 1f)
        )

        parts.forEach { part ->
drawPart(part)
}

// Draw the animated purple aura around the crown.
val haze = crownAuraHazePart
val core = crownAuraCorePart

if (effectBrightness > 0.01f && haze != null && core != null) {
GLES20.glDepthMask(false)
val auraParentMatrix = modelMatrix.copyOf()

val auraClock = when (mode) {
2 -> 0f
1 -> seconds * 0.42f
else -> seconds
}

crownAuraOrbs.forEach { orb ->
val pulse = if (mode == 2) {
0.5f
} else {
(sin(auraClock * 1.45f + orb.phase) + 1f) * 0.5f
}

val growth = 0.72f + pulse * 0.58f

val driftX = if (mode == 2) {
0f
} else {
sin(auraClock * 0.55f + orb.phase) * 0.035f
}

val driftY = if (mode == 2) {
0f
} else {
cos(auraClock * 0.72f + orb.phase) * 0.055f
}

val orbScale = orb.baseScale * growth
val orbX = orb.x + driftX
val orbY = orb.y + driftY

drawAuraPart(
part = haze,
parentMatrix = auraParentMatrix,
x = orbX,
y = orbY,
z = orb.z,
scale = orbScale * 1.70f,
alphaMultiplier = effectBrightness * 0.88f
)

drawAuraPart(
part = core,
parentMatrix = auraParentMatrix,
x = orbX,
y = orbY,
z = orb.z + 0.012f,
scale = orbScale * 0.58f,
alphaMultiplier = effectBrightness
)
}

GLES20.glDepthMask(true)
}

GLES20.glDisableVertexAttribArray(positionHandle)
GLES20.glDisableVertexAttribArray(normalHandle)
    }

    private fun buildAuraSphere(material: CrownMaterial): CrownPart? {
val mesh = CrownMeshBuilder(material)

addSphere(
mesh = mesh,
center = V3(0f, 0f, 0f),
radius = 1f,
segments = 20,
rings = 14
)

return mesh.build()
}

private fun drawAuraPart(
part: CrownPart,
parentMatrix: FloatArray,
x: Float,
y: Float,
z: Float,
scale: Float,
alphaMultiplier: Float
) {
// Reuse the renderer's existing matrices. The original crown transform
// is copied once per frame before the orb loop.
System.arraycopy(parentMatrix, 0, modelMatrix, 0, 16)

Matrix.translateM(modelMatrix, 0, x, y, z)
Matrix.scaleM(modelMatrix, 0, scale, scale, scale)

Matrix.multiplyMM(
modelViewMatrix,
0,
viewMatrix,
0,
modelMatrix,
0
)

Matrix.multiplyMM(
mvpMatrix,
0,
projectionMatrix,
0,
modelViewMatrix,
0
)

GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
GLES20.glUniformMatrix4fv(modelViewHandle, 1, false, modelViewMatrix, 0)

drawPart(part = part, alphaMultiplier = alphaMultiplier)
}

private fun drawPart(
part: CrownPart,
alphaMultiplier: Float = 1f
) {
val materialColor = part.material.color

GLES20.glUniform4f(
colorHandle,
materialColor[0],
materialColor[1],
materialColor[2],
materialColor[3] * alphaMultiplier.coerceIn(0f, 1f)
)

        GLES20.glUniform1f(
            specularHandle,
            part.material.specular
        )

        GLES20.glUniform1f(
            shininessHandle,
            part.material.shininess
        )

        GLES20.glUniform1f(
            emissionHandle,
            part.material.emission
        )

        val buffer = part.vertices

        buffer.position(0)

        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glVertexAttribPointer(
            positionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            24,
            buffer
        )

        buffer.position(3)

        GLES20.glEnableVertexAttribArray(normalHandle)

        GLES20.glVertexAttribPointer(
            normalHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            24,
            buffer
        )

        buffer.position(0)

        GLES20.glDrawArrays(
            GLES20.GL_TRIANGLES,
            0,
            part.vertexCount
        )
    }

    private fun createProgram(
        vertexSource: String,
        fragmentSource: String
    ): Int {
        val vertex = compileShader(
            GLES20.GL_VERTEX_SHADER,
            vertexSource
        )

        val fragment = compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            fragmentSource
        )

        val result = GLES20.glCreateProgram()

        GLES20.glAttachShader(result, vertex)
        GLES20.glAttachShader(result, fragment)

        GLES20.glLinkProgram(result)

        val status = IntArray(1)

        GLES20.glGetProgramiv(
            result,
            GLES20.GL_LINK_STATUS,
            status,
            0
        )

        if (status[0] == 0) {
            val message = GLES20.glGetProgramInfoLog(result)
            GLES20.glDeleteProgram(result)

            throw IllegalStateException(
                "Crown shader linking failed: $message"
            )
        }

        GLES20.glDeleteShader(vertex)
        GLES20.glDeleteShader(fragment)

        return result
    }

    private fun compileShader(
        type: Int,
        source: String
    ): Int {
        val shader = GLES20.glCreateShader(type)

        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        val status = IntArray(1)

        GLES20.glGetShaderiv(
            shader,
            GLES20.GL_COMPILE_STATUS,
            status,
            0
        )

        if (status[0] == 0) {
            val message = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)

            throw IllegalStateException(
                "Crown shader compilation failed: $message"
            )
        }

        return shader
    }
}

// =====================================================================
// PROCEDURAL 3D CROWN MODEL
// =====================================================================

private fun buildCrownModel(): List<CrownPart> {

    val model = CrownModelBuilder()

    buildCrownShell(model)
    buildCrownRims(model)
    buildCrownPeaks(model)
    buildCrownStuds(model)
    buildCrownFiligree(model)
    buildCrownGemstones(model)

    return model.build()
}

// =====================================================================
// 1. HOLLOW FIVE-PEAK CROWN SHELL
// =====================================================================

private fun crownTop(angle: Float): Float {
    val wave = max(
        0f,
        cos(angle * 5f)
    )

    return 0.12f + 0.50f * wave.pow(3.2f)
}

private fun buildCrownShell(model: CrownModelBuilder) {

    val outer = model.mesh(
        "crown_outer_shell",
        CrownMaterials.BODY
    )

    val inner = model.mesh(
        "crown_inner_shell",
        CrownMaterials.BODY_DARK
    )

    val topTrim = model.mesh(
        "crown_top_edge",
        CrownMaterials.PLATINUM
    )

    val bottomTrim = model.mesh(
        "crown_bottom_edge",
        CrownMaterials.PLATINUM_DARK
    )

    val segments = 120
    val outerRadius = 0.82f
    val innerRadius = 0.70f
    val bottomY = -0.35f
    val tau = 2f * PI.toFloat()

    for (i in 0 until segments) {

        val angle0 = tau * i / segments
        val angle1 = tau * (i + 1) / segments

        val top0 = crownTop(angle0)
        val top1 = crownTop(angle1)

        val n0 = V3(
            sin(angle0),
            0f,
            cos(angle0)
        )

        val n1 = V3(
            sin(angle1),
            0f,
            cos(angle1)
        )

        val ob0 = V3(
            outerRadius * sin(angle0),
            bottomY,
            outerRadius * cos(angle0)
        )

        val ob1 = V3(
            outerRadius * sin(angle1),
            bottomY,
            outerRadius * cos(angle1)
        )

        val ot0 = V3(
            outerRadius * sin(angle0),
            top0,
            outerRadius * cos(angle0)
        )

        val ot1 = V3(
            outerRadius * sin(angle1),
            top1,
            outerRadius * cos(angle1)
        )

        // Outer metal surface.
        outer.triangle(ob0, ob1, ot1, n0, n1, n1)
        outer.triangle(ob0, ot1, ot0, n0, n1, n0)

        val ib0 = V3(
            innerRadius * sin(angle0),
            bottomY,
            innerRadius * cos(angle0)
        )

        val ib1 = V3(
            innerRadius * sin(angle1),
            bottomY,
            innerRadius * cos(angle1)
        )

        val it0 = V3(
            innerRadius * sin(angle0),
            top0,
            innerRadius * cos(angle0)
        )

        val it1 = V3(
            innerRadius * sin(angle1),
            top1,
            innerRadius * cos(angle1)
        )

        // Dark inner surface makes the crown visibly hollow.
        inner.triangle(
            ib0,
            ib1,
            it1,
            n0 * -1f,
            n1 * -1f,
            n1 * -1f
        )

        inner.triangle(
            ib0,
            it1,
            it0,
            n0 * -1f,
            n1 * -1f,
            n0 * -1f
        )

        // Actual thickness along the top edge.
        val up = V3(0f, 1f, 0f)

        topTrim.triangle(ot0, ot1, it1, up, up, up)
        topTrim.triangle(ot0, it1, it0, up, up, up)

        // Close the bottom edge of the metal shell.
        val down = V3(0f, -1f, 0f)

        bottomTrim.triangle(ib0, ob1, ob0, down, down, down)
        bottomTrim.triangle(ib0, ib1, ob1, down, down, down)
    }

    // Rounded upper metal contour follows the five real peaks.
    val crownContour = (0 until segments).map { i ->
        val angle = tau * i / segments

        V3(
            outerRadius * sin(angle),
            crownTop(angle),
            outerRadius * cos(angle)
        )
    }

    addTube(
        model.mesh(
            "upper_platinum_contour",
            CrownMaterials.PLATINUM_BRIGHT
        ),
        crownContour,
        radius = 0.021f,
        sides = 10,
        closed = true
    )
}

// =====================================================================
// 2. TORUS-BASED METALLIC RIMS
// =====================================================================

private fun addTorusY(
    mesh: CrownMeshBuilder,
    majorRadius: Float,
    tubeRadius: Float,
    centerY: Float,
    majorSegments: Int = 100,
    minorSegments: Int = 14
) {
    val tau = 2f * PI.toFloat()

    for (i in 0 until majorSegments) {
        val u0 = tau * i / majorSegments
        val u1 = tau * (i + 1) / majorSegments

        for (j in 0 until minorSegments) {
            val v0 = tau * j / minorSegments
            val v1 = tau * (j + 1) / minorSegments

            fun point(u: Float, v: Float): V3 {
                val radial =
                    majorRadius + tubeRadius * cos(v)

                return V3(
                    radial * sin(u),
                    centerY + tubeRadius * sin(v),
                    radial * cos(u)
                )
            }

            fun normal(u: Float, v: Float): V3 {
                return V3(
                    cos(v) * sin(u),
                    sin(v),
                    cos(v) * cos(u)
                ).normalized()
            }

            val a = point(u0, v0)
            val b = point(u1, v0)
            val c = point(u1, v1)
            val d = point(u0, v1)

            val na = normal(u0, v0)
            val nb = normal(u1, v0)
            val nc = normal(u1, v1)
            val nd = normal(u0, v1)

            mesh.triangle(a, b, c, na, nb, nc)
            mesh.triangle(a, c, d, na, nc, nd)
        }
    }
}

private fun buildCrownRims(model: CrownModelBuilder) {

    addTorusY(
        model.mesh(
            "heavy_lower_platinum_rim",
            CrownMaterials.PLATINUM
        ),
        majorRadius = 0.755f,
        tubeRadius = 0.052f,
        centerY = -0.315f
    )

    addTorusY(
        model.mesh(
            "upper_rim_bevel",
            CrownMaterials.PLATINUM_BRIGHT
        ),
        majorRadius = 0.795f,
        tubeRadius = 0.012f,
        centerY = -0.235f
    )

    addTorusY(
        model.mesh(
            "lower_violet_bevel",
            CrownMaterials.BODY_LIGHT
        ),
        majorRadius = 0.775f,
        tubeRadius = 0.012f,
        centerY = -0.365f
    )

    addTorusY(
        model.mesh(
            "inner_band",
            CrownMaterials.BODY_MID
        ),
        majorRadius = 0.695f,
        tubeRadius = 0.025f,
        centerY = -0.305f,
        majorSegments = 80,
        minorSegments = 12
    )
}

// =====================================================================
// 3. TUBE MESHES FOR RAISED METAL ORNAMENTS
// =====================================================================

private fun addTube(
    mesh: CrownMeshBuilder,
    points: List<V3>,
    radius: Float,
    sides: Int = 8,
    closed: Boolean = false
) {
    if (points.size < 2) return

    val rings = ArrayList<List<Pair<V3, V3>>>()

    for (i in points.indices) {

        val previous = if (closed) {
            points[(i - 1 + points.size) % points.size]
        } else {
            points[max(0, i - 1)]
        }

        val next = if (closed) {
            points[(i + 1) % points.size]
        } else {
            points[min(points.lastIndex, i + 1)]
        }

        val tangent = (next - previous).normalized()

        val reference = if (abs(tangent.y) < 0.92f) {
            V3(0f, 1f, 0f)
        } else {
            V3(0f, 0f, 1f)
        }

        val side = tangent.cross(reference).normalized()
        val up = tangent.cross(side).normalized()

        val ring = ArrayList<Pair<V3, V3>>()

        for (j in 0 until sides) {
            val angle = 2f * PI.toFloat() * j / sides

            val normal = (
                side * cos(angle) +
                    up * sin(angle)
                ).normalized()

            ring.add(
                Pair(
                    points[i] + normal * radius,
                    normal
                )
            )
        }

        rings.add(ring)
    }

    val segmentCount = if (closed) {
        points.size
    } else {
        points.size - 1
    }

    for (i in 0 until segmentCount) {

        val nextIndex = (i + 1) % points.size

        for (j in 0 until sides) {

            val jNext = (j + 1) % sides

            val a = rings[i][j]
            val b = rings[i][jNext]
            val c = rings[nextIndex][jNext]
            val d = rings[nextIndex][j]

            mesh.triangle(
                a.first,
                b.first,
                c.first,
                a.second,
                b.second,
                c.second
            )

            mesh.triangle(
                a.first,
                c.first,
                d.first,
                a.second,
                c.second,
                d.second
            )
        }
    }
}

// =====================================================================
// 4. SPHERE GEOMETRY FOR FINIALS, STUDS AND CLAWS
// =====================================================================

private fun addSphere(
    mesh: CrownMeshBuilder,
    center: V3,
    radius: Float,
    segments: Int = 14,
    rings: Int = 10
) {
    val grid = ArrayList<List<Pair<V3, V3>>>()

    for (i in 0..rings) {

        val latitude =
            -PI.toFloat() / 2f +
                PI.toFloat() * i / rings

        val ringRadius = cos(latitude)
        val y = sin(latitude)

        val row = ArrayList<Pair<V3, V3>>()

        for (j in 0..segments) {

            val longitude =
                2f * PI.toFloat() * j / segments

            val normal = V3(
                ringRadius * sin(longitude),
                y,
                ringRadius * cos(longitude)
            ).normalized()

            row.add(
                Pair(
                    center + normal * radius,
                    normal
                )
            )
        }

        grid.add(row)
    }

    for (i in 0 until rings) {
        for (j in 0 until segments) {

            val a = grid[i][j]
            val b = grid[i + 1][j]
            val c = grid[i + 1][j + 1]
            val d = grid[i][j + 1]

            mesh.triangle(
                a.first,
                b.first,
                c.first,
                a.second,
                b.second,
                c.second
            )

            mesh.triangle(
                a.first,
                c.first,
                d.first,
                a.second,
                c.second,
                d.second
            )
        }
    }
}

// =====================================================================
// 5. FIVE SCULPTED PEAKS AND VERTICAL METAL RIBS
// =====================================================================

private fun buildCrownPeaks(model: CrownModelBuilder) {

    val ribMesh = model.mesh(
        "five_raised_royal_ribs",
        CrownMaterials.PLATINUM
    )

    val shadowRib = model.mesh(
        "inner_raised_ribs",
        CrownMaterials.BODY_MID
    )

    val finials = model.mesh(
        "royal_peak_finials",
        CrownMaterials.PLATINUM_BRIGHT
    )

    val tau = 2f * PI.toFloat()

    for (i in 0 until 5) {

        val angle = tau * i / 5f
        val top = crownTop(angle)

        val x = sin(angle) * 0.827f
        val z = cos(angle) * 0.827f

        val points = (0..24).map { step ->
            val fraction = step / 24f

            V3(
                x,
                -0.27f + (top - 0.055f + 0.27f) * fraction,
                z
            )
        }

        addTube(
            ribMesh,
            points,
            radius = 0.010f,
            sides = 7
        )

        val innerPoints = (0..20).map { step ->
            val fraction = step / 20f

            V3(
                sin(angle) * 0.703f,
                -0.25f + (top - 0.08f + 0.25f) * fraction,
                cos(angle) * 0.703f
            )
        }

        addTube(
            shadowRib,
            innerPoints,
            radius = 0.009f,
            sides = 6
        )

        addSphere(
            finials,
            V3(
                sin(angle) * 0.827f,
                top + 0.005f,
                cos(angle) * 0.827f
            ),
            radius = if (i == 0) 0.043f else 0.035f,
            segments = 16,
            rings = 10
        )
    }
}

// =====================================================================
// 6. INDIVIDUAL METAL STUDS
// =====================================================================

private fun buildCrownStuds(model: CrownModelBuilder) {

    val studs = model.mesh(
        "silver_crown_studs",
        CrownMaterials.PLATINUM_BRIGHT
    )

    val darkStuds = model.mesh(
        "dark_stud_sockets",
        CrownMaterials.PLATINUM_DARK
    )

    val count = 30
    val tau = 2f * PI.toFloat()

    for (i in 0 until count) {

        val angle = tau * i / count

        val center = V3(
            sin(angle) * 0.828f,
            -0.305f,
            cos(angle) * 0.828f
        )

        addSphere(
            darkStuds,
            center,
            radius = 0.020f,
            segments = 8,
            rings = 6
        )

        addSphere(
            studs,
            V3(
                center.x,
                center.y + 0.004f,
                center.z
            ),
            radius = 0.013f,
            segments = 10,
            rings = 7
        )
    }
}

// =====================================================================
// 7. RAISED FRONT FILIGREE
// =====================================================================

private fun buildCrownFiligree(model: CrownModelBuilder) {

    val bright = model.mesh(
        "platinum_front_filigree",
        CrownMaterials.PLATINUM
    )

    val dark = model.mesh(
        "shadowed_front_filigree",
        CrownMaterials.PLATINUM_DARK
    )

    for (side in listOf(-1f, 1f)) {

        val mainScroll = (0..32).map { i ->

            val t = i / 32f
            val x = side * (0.15f + 0.45f * t)

            val y =
                -0.15f +
                    0.075f * sin(t * PI.toFloat()) +
                    0.015f * sin(t * PI.toFloat() * 3f)

            val z = sqrt(
                max(0.0001f, 0.82f * 0.82f - x * x)
            ) + 0.014f

            V3(x, y, z)
        }

        addTube(
            bright,
            mainScroll,
            radius = 0.0065f,
            sides = 7
        )

        val secondaryScroll = (0..28).map { i ->

            val t = i / 28f
            val x = side * (0.22f + 0.34f * t)

            val y =
                -0.225f +
                    0.035f * sin(t * 2f * PI.toFloat())

            val z = sqrt(
                max(0.0001f, 0.82f * 0.82f - x * x)
            ) + 0.017f

            V3(x, y, z)
        }

        addTube(
            dark,
            secondaryScroll,
            radius = 0.008f,
            sides = 7
        )

        addTube(
            bright,
            secondaryScroll,
            radius = 0.0035f,
            sides = 6
        )
    }

    // Small central decorative ridge above the principal gemstone.
    val centralRidge = (0..20).map { i ->
        val t = i / 20f

        V3(
            0f,
            -0.015f + t * 0.145f,
            0.838f + 0.008f * sin(t * PI.toFloat())
        )
    }

    addTube(
        bright,
        centralRidge,
        radius = 0.006f,
        sides = 7
    )
}

// =====================================================================
// 8. FACETED AMETHYST GEMSTONES
// =====================================================================

private fun addCrownGem(
    model: CrownModelBuilder,
    angle: Float,
    centerY: Float,
    scale: Float,
    index: Int
) {

    val radialDistance = 0.846f

    val center = V3(
        sin(angle) * radialDistance,
        centerY,
        cos(angle) * radialDistance
    )

    val cosine = cos(angle)
    val sine = sin(angle)

    // Local gemstone coordinates are rotated to follow the crown surface.
    fun transform(local: V3): V3 {
        return V3(
            center.x + local.x * cosine + local.z * sine,
            center.y + local.y,
            center.z - local.x * sine + local.z * cosine
        )
    }

    val bezel = model.mesh(
        "gemstone_bezels",
        CrownMaterials.PLATINUM
    )

    val bezelPoints = (0 until 36).map { i ->

        val a = 2f * PI.toFloat() * i / 36f

        transform(
            V3(
                scale * 1.19f * cos(a),
                scale * 1.19f * sin(a) * 0.80f,
                0.012f
            )
        )
    }

    addTube(
        bezel,
        bezelPoints,
        radius = scale * 0.105f,
        sides = 7,
        closed = true
    )

    val outerRing = ArrayList<V3>()
    val upperRing = ArrayList<V3>()

    for (i in 0 until 8) {

        val a = 2f * PI.toFloat() * i / 8f

        outerRing.add(
            transform(
                V3(
                    scale * cos(a),
                    scale * sin(a) * 0.80f,
                    0f
                )
            )
        )

        upperRing.add(
            transform(
                V3(
                    scale * 0.43f * cos(a),
                    scale * 0.34f * sin(a),
                    scale * 0.72f
                )
            )
        )
    }

    val tableCenter = transform(
        V3(0f, 0f, scale * 0.76f)
    )

    val bottomPoint = transform(
        V3(0f, 0f, -scale * 0.58f)
    )

    val tableMesh = model.mesh(
        "amethyst_tables",
        CrownMaterials.GEM_TABLE
    )

    val pavilion = model.mesh(
        "amethyst_pavilion",
        CrownMaterials.GEM_DARK
    )

    val facetMaterials = listOf(
        CrownMaterials.GEM_LIGHT,
        CrownMaterials.GEM_MID,
        CrownMaterials.GEM_VIOLET,
        CrownMaterials.GEM_DARK
    )

    for (i in 0 until 8) {

        val next = (i + 1) % 8

        val topA = upperRing[i]
        val topB = upperRing[next]

        val outerA = outerRing[i]
        val outerB = outerRing[next]

        tableMesh.triangle(
            tableCenter,
            topA,
            topB
        )

        val facet = model.mesh(
            "amethyst_upper_facet_${(index * 3 + i) % 4}",
            facetMaterials[(index + i) % facetMaterials.size]
        )

        facet.triangle(
            topA,
            outerA,
            outerB
        )

        facet.triangle(
            topA,
            outerB,
            topB
        )

        pavilion.triangle(
            bottomPoint,
            outerB,
            outerA
        )
    }

    // Four tiny raised claws hold each stone in its platinum setting.
    val claws = model.mesh(
        "gemstone_claws",
        CrownMaterials.PLATINUM_BRIGHT
    )

    for (sideX in listOf(-1f, 1f)) {
        for (sideY in listOf(-1f, 1f)) {

            val local = V3(
                sideX * scale * 0.83f,
                sideY * scale * 0.62f,
                scale * 0.035f
            )

            addSphere(
                claws,
                transform(local),
                radius = scale * 0.095f,
                segments = 8,
                rings = 6
            )
        }
    }
}

private fun buildCrownGemstones(model: CrownModelBuilder) {

    val stones = listOf(
        Triple(-1.04f, -0.075f, 0.057f),
        Triple(-0.52f, -0.085f, 0.078f),
        Triple(0f, -0.080f, 0.108f),
        Triple(0.52f, -0.085f, 0.078f),
        Triple(1.04f, -0.075f, 0.057f)
    )

    stones.forEachIndexed { index, stone ->

        addCrownGem(
            model = model,
            angle = stone.first,
            centerY = stone.second,
            scale = stone.third,
            index = index
        )
    }
}
