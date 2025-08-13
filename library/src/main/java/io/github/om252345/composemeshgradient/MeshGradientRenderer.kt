package io.github.om252345.composemeshgradient

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MeshGradientRenderer(
    private val gridWidth: Int,
    private val gridHeight: Int,
    private val globalSubdivisions: Int
) : GLSurfaceView.Renderer {

    private lateinit var helper: MeshGradientRendererHelper

    // Stable internal copies; sized by grid
    private val pointCount = gridWidth * gridHeight
    private var currentPoints: FloatArray = FloatArray(pointCount * 2)
    private var currentColors: FloatArray = FloatArray(pointCount * 4)

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)       // default, cull back faces
        GLES20.glFrontFace(GLES20.GL_CCW)       // now matches our fixed winding

        helper = MeshGradientRendererHelper(gridWidth, gridHeight, globalSubdivisions)
        helper.initBuffers() // compiles/links shaders, builds UV + indices, caches locations, preallocs uniform arrays
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(unused: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        helper.draw(currentPoints, currentColors)
    }

    /**
     * Update points/colors in primitive form.
     * Arrays must match grid size: points = N*2, colors = N*4.
     * We copy to keep renderer-owned stable arrays (caller can reuse theirs).
     */
    fun updatePoints(newPoints: FloatArray, newColors: FloatArray) {
        require(newPoints.size == pointCount * 2) { "points FloatArray must be ${pointCount * 2} floats" }
        require(newColors.size == pointCount * 4) { "colors FloatArray must be ${pointCount * 4} floats" }

        // Copy to avoid retaining external mutable arrays
        System.arraycopy(newPoints, 0, currentPoints, 0, newPoints.size)
        System.arraycopy(newColors, 0, currentColors, 0, newColors.size)
    }
}
