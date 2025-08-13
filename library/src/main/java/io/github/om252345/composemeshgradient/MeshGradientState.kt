package io.github.om252345.composemeshgradient

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
    initialColors: Array<Color>
) {

    // Original API data
    private val _points = mutableStateListOf<Animatable<Offset, AnimationVector2D>>().apply {
        initialPoints.forEach { add(Animatable(it, Offset.VectorConverter)) }
    }
    private val _colors = mutableStateListOf<Color>().apply {
        addAll(initialColors)
    }

    // -------- Backward-compatible properties (will allocate when accessed) --------
    val points: List<Offset> get() = _points.map { it.value }   // legacy-friendly but allocates
    val colors: List<Color> get() = _colors                     // existing callers OK

    // -------- New no-allocation snapshot arrays for renderer --------
    private val snapshotPoints = FloatArray(_points.size * 2)   // x,y pairs
    private val snapshotColors = FloatArray(_colors.size * 4)   // r,g,b,a

    /** Returns a reusable float array of points: [x0,y0, x1,y1, ...]. Do not store beyond this frame. */
    fun pointsArray(): FloatArray {
        var i = 0
        for (p in _points) {
            snapshotPoints[i++] = p.value.x
            snapshotPoints[i++] = p.value.y
        }
        return snapshotPoints
    }

    /** Returns a reusable float array of colors: [r,g,b,a, r,g,b,a, ...]. Do not store beyond this frame. */
    fun colorsArray(): FloatArray {
        var i = 0
        for (c in _colors) {
            snapshotColors[i++] = c.red
            snapshotColors[i++] = c.green
            snapshotColors[i++] = c.blue
            snapshotColors[i++] = c.alpha
        }
        return snapshotColors
    }

    // -------- State mutation helpers --------
    suspend fun animatePoint(index: Int, target: Offset, spec: AnimationSpec<Offset>) {
        _points[index].animateTo(target, spec)
    }

    fun setColor(index: Int, color: Color) {
        _colors[index] = color
    }

    suspend fun snapAllPoints(newOffsets: List<Offset>) {
        newOffsets.forEachIndexed { i, o ->
            if (i in _points.indices) _points[i].snapTo(o)
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
    colors: Array<Color>
): MeshGradientState {
    return remember(points) {
        MeshGradientState(points, colors)
    }
}
