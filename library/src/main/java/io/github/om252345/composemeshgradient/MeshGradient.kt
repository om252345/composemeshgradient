package io.github.om252345.composemeshgradient

import android.opengl.GLSurfaceView
import androidx.compose.foundation.layout.fillMaxSize
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
) {
    // The renderer is remembered with keys that don't change often.
    // The points themselves are handled in the `update` block.
    val renderer = remember(width, height) {
        MeshGradientRenderer(
            width = width,
            height = height,
            initialPoints = points,
            colors = colors
        )
    }

    AndroidView(
        factory = { context ->
            GLSurfaceView(context).apply {
                setEGLContextClientVersion(3)
                setRenderer(renderer)
                // We render only when requested, which saves battery and performance.
                renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            }
        },
        update = { view ->
            // This is the bridge between Compose and OpenGL.
            // When `points` changes, this block is re-executed.
            // We safely update the renderer on its own thread and request a new frame.
            view.queueEvent {
                renderer.updatePoints(points, colors)
            }
            view.requestRender()
        },
        modifier = modifier.fillMaxSize()
    )
}
