package io.github.om252345.composemeshgradient.utils

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.om252345.composemeshgradient.rememberMeshGradientState
import kotlinx.coroutines.launch

@Composable
fun InfiniteTransition.animateOffset(
    initialValue: Offset,
    targetValue: Offset,
    animationSpec: InfiniteRepeatableSpec<Float>
): State<Offset> {
    val xAnim = animateFloat(
        initialValue = initialValue.x,
        targetValue = targetValue.x,
        animationSpec = animationSpec
    )
    val yAnim = animateFloat(
        initialValue = initialValue.y,
        targetValue = targetValue.y,
        animationSpec = animationSpec
    )
    return derivedStateOf { Offset(xAnim.value, yAnim.value) }
}


/**
 * A state holder that manages the animated state of mesh data.
 */
interface AnimatedMeshState {
    val meshData: MeshData
}

/**
 * A composable utility that creates and remembers an animated mesh state,
 * applying a continuous Simplex Noise animation.
 *
 * This simplifies creating fluid, "lava lamp" style animations without boilerplate.
 *
 * @param width The width of the mesh grid.
 * @param height The height of the mesh grid.
 * @param colors The list of colors to apply to the mesh points.
 * @param animationSpeed A factor to control the speed of the animation.
 * @param noiseIntensity A factor to control the amplitude/intensity of the point movements.
 * @return An [AnimatedMeshState] whose `meshData` can be passed to the `MeshGradient`.
 */
@Composable
fun rememberSimplexAnimatedMesh(
    width: Int,
    height: Int,
    colors: List<Color>,
    animationSpeed: Float = 0.3f,
    noiseIntensity: Float = 0.2f
): AnimatedMeshState {

    val initialPoints = remember(width, height) {
        generateMeshPoints(width, height, randomness = 0f)
    }
    val meshState = rememberMeshGradientState(points = initialPoints)

    LaunchedEffect(Unit) {
        var time = 0f
        var lastFrameTime = 0L
        val basePoints = initialPoints.toList()
        val currentPoints = initialPoints.toMutableList()
        val targetPoints = initialPoints.toMutableList()

        while (true) {
            withFrameNanos { frameTime ->
                if (lastFrameTime == 0L) lastFrameTime = frameTime
                val deltaTime = (frameTime - lastFrameTime) / 1_000_000_000.0f
                lastFrameTime = frameTime
                time += deltaTime

                for (i in targetPoints.indices) {
                    val col = i % width
                    val row = i / height
                    val isBorder = row == 0 || row == height - 1 || col == 0 || col == width - 1

                    if (isBorder) {
                        targetPoints[i] = basePoints[i]
                    } else {
                        val bp = basePoints[i]
                        val noiseX = SimplexNoise.noise(
                            bp.x * 1.5f,
                            time * animationSpeed + i
                        ) * noiseIntensity
                        val noiseY = SimplexNoise.noise(
                            bp.y * 1.5f,
                            time * animationSpeed + i + 100f
                        ) * noiseIntensity
                        targetPoints[i] = bp.copy(x = bp.x + noiseX, y = bp.y + noiseY)
                    }
                }

                val smoothingFactor = 8f
                for (i in currentPoints.indices) {
                    currentPoints[i] = lerp(
                        currentPoints[i],
                        targetPoints[i],
                        (smoothingFactor * deltaTime).coerceIn(0f, 1f)
                    )
                }

                launch {
                    meshState.snapAllPoints(currentPoints.toList())
                }
            }
        }
    }

    return remember(meshState, colors) {
        object : AnimatedMeshState {
            override val meshData: MeshData by derivedStateOf {
                MeshData(
                    points = meshState.points.toTypedArray(),
                    colors = colors.toTypedArray()
                )
            }
        }
    }
}
