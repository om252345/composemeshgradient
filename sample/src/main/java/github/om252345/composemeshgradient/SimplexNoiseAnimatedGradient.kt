package github.om252345.composemeshgradient

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.om252345.composemeshgradient.MeshGradient
import io.github.om252345.composemeshgradient.rememberMeshGradientState
import io.github.om252345.composemeshgradient.utils.SimplexNoise
import kotlinx.coroutines.launch

/**
 * A composable that demonstrates a fluid, continuous animation of a mesh gradient
 * using Simplex Noise, similar to the original implementation.
 */
@Composable
fun SimplexNoiseMeshExample(modifier: Modifier) {
    val width = 4
    val height = 4

    // 1. Define the colors and initial grid layout
    val colors = remember {
        listOf(
            Color(0xffF36E21), Color(0xffF0A92A), Color(0xffE7D043), Color(0xffE7E95D),
            Color(0xffDFF168), Color(0xffB1E192), Color(0xff7FD3A9), Color(0xff5EC6B8),
            Color(0xff4AB8C2), Color(0xff4099B9), Color(0xff3B79A8), Color(0xff395B93),
            Color(0xff36447C), Color(0xff332E66), Color(0xff301E4E), Color(0xff2D1137)
        )
    }

    val initialPoints = remember {
        Array(width * height) { i ->
            val col = i % width
            val row = i / width
            Offset(x = col / (width - 1f), y = row / (height - 1f))
        }
    }

    // A stable copy of the original points to base noise calculations on
    val basePoints = remember { initialPoints.toList() }

    // 2. Create and remember the state that will drive the animation
    val meshState =
        rememberMeshGradientState(points = initialPoints, colors = colors.toTypedArray())

    // 3. Use a LaunchedEffect to run the continuous animation loop
    LaunchedEffect(Unit) {
        var time = 0f
        var currentPoints = initialPoints.toMutableList()
        val targetPoints = initialPoints.toMutableList()

        var lastFrameTime = 0L

        // This loop runs for every frame, creating a smooth animation
        while (true) {
            withFrameNanos { frameTime ->
                if (lastFrameTime == 0L) {
                    lastFrameTime = frameTime
                }
                val deltaTime = (frameTime - lastFrameTime) / 1_000_000_000.0f
                lastFrameTime = frameTime
                time += deltaTime

                // a. Calculate new target positions with Simplex Noise
                val animationSpeed = 0.3f
                for (i in targetPoints.indices) {
                    val col = i % width
                    val row = i / width
                    // Keep the border points fixed for a cleaner look
                    val isBorder = row == 0 || row == height - 1 || col == 0 || col == width - 1

                    if (isBorder) {
                        targetPoints[i] = basePoints[i]
                    } else {
                        val bp = basePoints[i]
                        val noiseX =
                            SimplexNoise.noise(bp.x * 1.5f, time * animationSpeed + i) * 0.2f
                        val noiseY =
                            SimplexNoise.noise(bp.y * 1.5f, time * animationSpeed + i + 100f) * 0.2f
                        targetPoints[i] = Offset(bp.x + noiseX, bp.y + noiseY)
                    }
                }

                // b. Smoothly interpolate the current points towards the moving target
                val smoothingFactor = 8f // Higher value means faster following
                for (i in currentPoints.indices) {
                    currentPoints[i] = lerp(
                        currentPoints[i],
                        targetPoints[i],
                        (smoothingFactor * deltaTime).coerceIn(0f, 1f)
                    )
                }

                // c. Snap the mesh state to the newly calculated points.
                // This is a suspend function, so it's called within the coroutine scope.
                // It efficiently updates the state, triggering a redraw.
                launch {
                    meshState.snapAllPoints(currentPoints.toList())
                }
            }
        }
    }

    // 4. Render the MeshGradient, feeding it the animated points from the state
    MeshGradient(
        modifier = modifier,
        width = width,
        height = height,
        state = meshState,
    )
}

/**
 * Helper function for linear interpolation between two Offsets.
 */
private fun lerp(start: Offset, stop: Offset, fraction: Float): Offset {
    return start + (stop - start) * fraction.coerceIn(0f, 1f)
}