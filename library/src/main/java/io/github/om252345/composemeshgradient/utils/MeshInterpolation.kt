package io.github.om252345.composemeshgradient.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Utility class for mesh gradient interpolation calculations
 * This class was used for CPU rendering before, but is now primarily for reference
 * and potential future use cases.
 * Interpolation used here is exactly the same as in the shader.
 */
internal object MeshInterpolation {
    /**
     * Result of mesh generation containing vertices, colors, and optional indices
     */
    data class MeshResult(
        val vertices: FloatArray, // [x, y, x, y, ...]
        val colors: FloatArray,    // [r, g, b, a, r, g, b, a, ...]
        val indices: MutableList<Int> // Optional indices for indexed rendering
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MeshResult

            if (!vertices.contentEquals(other.vertices)) return false
            if (!colors.contentEquals(other.colors)) return false
            if (indices != other.indices) return false

            return true
        }

        override fun hashCode(): Int {
            var result = vertices.contentHashCode()
            result = 31 * result + colors.contentHashCode()
            result = 31 * result + indices.hashCode()
            return result
        }
    }

    /**
     * Generates mesh vertices and colors based on control points and grid dimensions
     *
     * @param controlPoints List of control points defining the mesh
     * @param controlColors List of colors corresponding to each control point
     * @param gridWidth Number of horizontal grid points
     * @param gridHeight Number of vertical grid points
     * @param subdivX Number of subdivisions per horizontal segment
     * @param subdivY Number of subdivisions per vertical segment
     * @return MeshResult containing vertices, colors, and indices
     */
    fun generateMeshVerticesAndColors(
        controlPoints: List<Offset>,
        controlColors: List<Color>,
        gridWidth: Int,
        gridHeight: Int,
        subdivX: Int,
        subdivY: Int
    ): MeshResult {
        // Convert controlPoints list into 2D arrays
        val points2D = Array(gridHeight) { y ->
            Array(gridWidth) { x ->
                val idx = y * gridWidth + x
                controlPoints[idx] to controlColors[idx]
            }
        }

        val totalX = (gridWidth - 1) * subdivX + 1
        val totalY = (gridHeight - 1) * subdivY + 1

        val vertices = FloatArray(totalX * totalY * 2)
        val colors = FloatArray(totalX * totalY * 4)

        // Temporary arrays to store intermediate results after X interpolation
        val interpPositionsX = Array(totalY) { Array(totalX) { Offset.Companion.Zero } }
        val interpColorsX = Array(totalY) { Array(totalX) { Color.Companion.Transparent } }

        // Pass 1: Interpolate along X for each control row
        for (gy in 0 until gridHeight) {
            for (gx in 0 until gridWidth - 1) {
                val p0 = points2D[gy][max(gx - 1, 0)].first
                val p1 = points2D[gy][gx].first
                val p2 = points2D[gy][gx + 1].first
                val p3 = points2D[gy][min(gx + 2, gridWidth - 1)].first

                val c0 = points2D[gy][max(gx - 1, 0)].second
                val c1 = points2D[gy][gx].second
                val c2 = points2D[gy][gx + 1].second
                val c3 = points2D[gy][min(gx + 2, gridWidth - 1)].second

                for (i in 0 until subdivX) {
                    val t = i / subdivX.toFloat()
                    val px = catmullRom(p0.x, p1.x, p2.x, p3.x, t) * 2f - 1f
                    val py = catmullRom(p0.y, p1.y, p2.y, p3.y, t) * 2f - 1f
                    val col = catmullRomColor(c0, c1, c2, c3, t)

                    val ix = gx * subdivX + i
                    interpPositionsX[gy][ix] = Offset(px, py)
                    interpColorsX[gy][ix] = col
                }
            }
            // Last point in row
            interpPositionsX[gy][(gridWidth - 1) * subdivX] = points2D[gy][gridWidth - 1].first
            interpColorsX[gy][(gridWidth - 1) * subdivX] = points2D[gy][gridWidth - 1].second
        }

        // Pass 2: Interpolate along Y for each column from the X-interpolated results
        for (ix in 0 until totalX) {
            for (gy in 0 until gridHeight - 1) {
                val p0 = interpPositionsX[max(gy - 1, 0)][ix]
                val p1 = interpPositionsX[gy][ix]
                val p2 = interpPositionsX[gy + 1][ix]
                val p3 = interpPositionsX[min(gy + 2, gridHeight - 1)][ix]

                val c0 = interpColorsX[max(gy - 1, 0)][ix]
                val c1 = interpColorsX[gy][ix]
                val c2 = interpColorsX[gy + 1][ix]
                val c3 = interpColorsX[min(gy + 2, gridHeight - 1)][ix]

                for (i in 0 until subdivY) {
                    val t = i / subdivY.toFloat()
                    val px = catmullRom(p0.x, p1.x, p2.x, p3.x, t)
                    val py = catmullRom(p0.y, p1.y, p2.y, p3.y, t)
                    val col = catmullRomColor(c0, c1, c2, c3, t)

                    val iy = gy * subdivY + i
                    val idx = (iy * totalX + ix)
                    vertices[idx * 2] = px
                    vertices[idx * 2 + 1] = py
                    colors[idx * 4] = col.red
                    colors[idx * 4 + 1] = col.green
                    colors[idx * 4 + 2] = col.blue
                    colors[idx * 4 + 3] = col.alpha
                }
            }
            // Last point in column
            val lastY = (gridHeight - 1) * subdivY
            val p = interpPositionsX[gridHeight - 1][ix]
            val col = interpColorsX[gridHeight - 1][ix]
            val idx = lastY * totalX + ix
            vertices[idx * 2] = p.x
            vertices[idx * 2 + 1] = p.y
            colors[idx * 4] = col.red
            colors[idx * 4 + 1] = col.green
            colors[idx * 4 + 2] = col.blue
            colors[idx * 4 + 3] = col.alpha
        }
        val indices = mutableListOf<Int>()

        for (y in 0 until totalY - 1) {
            for (x in 0 until totalX - 1) {
                val i0 = y * totalX + x
                val i1 = i0 + 1
                val i2 = (y + 1) * totalX + x
                val i3 = i2 + 1

                indices.add(i0)
                indices.add(i2)
                indices.add(i3)

                indices.add(i0)
                indices.add(i3)
                indices.add(i1)
            }
        }
        return MeshResult(vertices, colors, indices)
    }

    /**
     * Catmull-Rom interpolation for 1D values
     */
    private fun catmullRomColor(c0: Color, c1: Color, c2: Color, c3: Color, t: Float): Color {
        return Color(
            catmullRom(c0.red, c1.red, c2.red, c3.red, t),
            catmullRom(c0.green, c1.green, c2.green, c3.green, t),
            catmullRom(c0.blue, c1.blue, c2.blue, c3.blue, t),
            catmullRom(c0.alpha, c1.alpha, c2.alpha, c3.alpha, t)
        )
    }

    /**
     * Catmull-Rom interpolation for a single float value
     *
     * @param p0 Previous point
     * @param p1 Current point
     * @param p2 Next point
     * @param p3 Next-next point
     * @param t Interpolation parameter [0, 1]
     * @param tension Tension factor (default 0.5)
     * @return Interpolated value
     */
    fun catmullRom(
        p0: Float,
        p1: Float,
        p2: Float,
        p3: Float,
        t: Float,
        tension: Float = .5f
    ): Float {
        val t2 = t * t
        val t3 = t2 * t
        return ((-tension * p0 + (2f - tension) * p1 + (tension - 2f) * p2 + tension * p3) * t3) +
                ((2f * tension) * p0 + (tension - 3f) * p1 + (3f - 2f * tension) * p2 - tension * p3) * t2 +
                ((-tension) * p0 + tension * p2) * t + p1
    }
}

/**
 * Helper function for linear interpolation between two Colors.
 */
fun lerp(start: Color, stop: Color, fraction: Float): Color {
    val r = start.red + (stop.red - start.red) * fraction
    val g = start.green + (stop.green - start.green) * fraction
    val b = start.blue + (stop.blue - start.blue) * fraction
    val a = start.alpha + (stop.alpha - start.alpha) * fraction
    return Color(r, g, b, a)
}

fun lerp(start: Offset, stop: Offset, fraction: Float): Offset {
    val t = fraction.coerceIn(0f, 1f)
    return start + (stop - start) * t
}