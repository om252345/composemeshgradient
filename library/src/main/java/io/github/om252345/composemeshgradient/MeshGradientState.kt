package io.github.om252345.composemeshgradient

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * A state holder for the [MeshGradient] that allows for programmatic animation of control points.
 *
 * @param initialPoints The starting positions of the mesh's control points.
 */
@Stable
class MeshGradientState(
    initialPoints: Array<Offset>,
) {
    // Each point is an Animatable, allowing it to be animated independently.
    private val _points =
        initialPoints.map { Animatable(it, Offset.VectorConverter) }.toMutableStateList()

    /**
     * The current positions of the control points.
     * Observing this value will trigger recomposition when any point's position changes.
     */
    val points: List<Offset>
        get() = _points.map { it.value }

    /**
     * Animate a single control point to a new target offset.
     *
     * @param index The index of the control point to animate (in a row-major order).
     * @param targetOffset The destination [Offset] for the point.
     * @param animationSpec The [AnimationSpec] to use for the animation (e.g., tween, spring).
     */
    suspend fun animatePoint(
        index: Int,
        targetOffset: Offset,
        animationSpec: AnimationSpec<Offset>
    ) {
        coroutineScope {
            launch {
                _points[index].animateTo(targetOffset, animationSpec)
            }
        }
    }

    /**
     * Instantly updates all control points to new positions without animation.
     * This is ideal for per-frame updates from a continuous animation loop.
     *
     * @param newOffsets The new list of offsets for all control points.
     */
    suspend fun snapAllPoints(newOffsets: List<Offset>) {
        coroutineScope {
            newOffsets.forEachIndexed { index, offset ->
                if (index in _points.indices) {
                    launch {
                        _points[index].snapTo(offset)
                    }
                }
            }
        }
    }
}

/**
 * Creates and remembers a [MeshGradientState].
 *
 * @param points The initial list of control points for the mesh.
 */
@Composable
fun rememberMeshGradientState(
    points: Array<Offset>,
): MeshGradientState {
    return remember(points) {
        MeshGradientState(points)
    }
}
