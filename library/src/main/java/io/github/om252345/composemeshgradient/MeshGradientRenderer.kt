package io.github.om252345.composemeshgradient

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class MeshGradientRenderer(
    private val width: Int,
    private val height: Int,
    initialPoints: Array<Offset>,
    private val colors: Array<Color>
) : GLSurfaceView.Renderer {

    // The points that are actually drawn. This is now the single source of truth for positions.
    private var currentPoints = initialPoints
    private var currentColors: Array<Color> = colors
    private lateinit var mesh: MeshGradientRendererHelper

    init {
        require(initialPoints.size == width * height) {
            "Points array size must equal width * height (${width * height}), but was ${initialPoints.size}"
        }
        require(colors.size == width * height) {
            "Colors array size must equal width * height (${width * height}), but was ${colors.size}"
        }
    }

    /**
     * Updates the control points of the mesh. This method is thread-safe
     * and should be called from the UI thread via `GLSurfaceView.queueEvent`.
     */
    fun updatePoints(newPoints: Array<Offset>, newColors: Array<Color>) {
        // This method will be called on the GL thread, so direct access is safe.
        currentPoints = newPoints
        currentColors = newColors

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set up OpenGL state
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        // Initialize mesh with the initial points and colors
        mesh = MeshGradientRendererHelper(width, height)
        mesh.initBuffers()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // The drawing logic is now very simple.
        // It just clears the screen and draws the mesh with the current points.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        // We pass a dummy time value as it's no longer used for animation here.
        // The shader still has the uniform, so we must provide a value.
        mesh.draw(currentPoints, currentColors)
    }
}
