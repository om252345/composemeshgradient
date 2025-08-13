package io.github.om252345.composemeshgradient

import android.opengl.GLSurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Composable that displays a mesh gradient using OpenGL ES.
 *
 * This composable is reactive to changes in the `points` list. When a new list is provided,
 * it will update the mesh and redraw. This allows for animations driven by Compose state.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param width The number of control points horizontally.
 * @param height The number of control points vertically.
 * @param points The list of control points that define the mesh's shape. The size must be `width * height`.
 * @param colors The list of colors for each control point. The size must be `width * height`.
 */
@Composable
fun MeshGradient(
    modifier: Modifier = Modifier,
    width: Int,
    height: Int,
    points: Array<Offset>,
    colors: Array<Color>,
    globalSubdivisions: Int = 32,
) {
    // The renderer is remembered with keys that don't change often.
    // The points themselves are handled in the `update` block.
    val renderer = remember(width, height, globalSubdivisions) {
        MeshGradientRenderer(width, height, globalSubdivisions)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            GLSurfaceView(context).apply {
                setEGLContextClientVersion(2)
                preserveEGLContextOnPause = true
                setRenderer(renderer)
                renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            }
        },
        update = { view ->
            // Fallback: convert objects → primitive arrays (allocates each frame)
            val pointFloats = FloatArray(points.size * 2)
            var pi = 0
            for (p in points) {
                pointFloats[pi++] = p.x
                pointFloats[pi++] = p.y
            }

            val colorFloats = FloatArray(colors.size * 4)
            var ci = 0
            for (c in colors) {
                colorFloats[ci++] = c.red
                colorFloats[ci++] = c.green
                colorFloats[ci++] = c.blue
                colorFloats[ci++] = c.alpha
            }

            view.queueEvent {
                renderer.updatePoints(pointFloats, colorFloats)
            }
            view.requestRender()
        }
    )
}

@Composable
fun MeshGradient(
    modifier: Modifier = Modifier,
    width: Int,
    height: Int,
    globalSubdivisions: Int = 32,
    state: MeshGradientState
) {
    // Renderer is tied to grid dimensions, so remember by those keys
    val renderer = remember(width, height, globalSubdivisions) {
        MeshGradientRenderer(width, height, globalSubdivisions)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            GLSurfaceView(context).apply {
                setEGLContextClientVersion(2)
                preserveEGLContextOnPause = true
                setRenderer(renderer)
                renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            }
        },
        update = { view ->
            // Use the state's no-alloc snapshots
            val pts = state.pointsArray()
            val cols = state.colorsArray()

            view.queueEvent {
                renderer.updatePoints(pts, cols)
            }
            view.requestRender()
        }
    )
}