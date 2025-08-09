package io.github.om252345.composemeshgradient.utils

import kotlin.math.floor
import kotlin.math.sqrt

/**
 * Simplex noise implementation for generating smooth noise patterns.
 * This is a 2D version of the Simplex noise algorithm, which is often used in procedural generation.
 * Based on the algorithm by Ken Perlin.
 */
object SimplexNoise {
    private val grad3 = arrayOf(
        intArrayOf(1, 1, 0), intArrayOf(-1, 1, 0), intArrayOf(1, -1, 0), intArrayOf(-1, -1, 0),
        intArrayOf(1, 0, 1), intArrayOf(-1, 0, 1), intArrayOf(1, 0, -1), intArrayOf(-1, 0, -1),
        intArrayOf(0, 1, 1), intArrayOf(0, -1, 1), intArrayOf(0, 1, -1), intArrayOf(0, -1, -1)
    )

    private val p = IntArray(256) { it }
    private val perm = IntArray(512)
    private val permMod12 = IntArray(512)

    init {
        p.shuffle()
        for (i in 0 until 512) {
            perm[i] = p[i and 255]
            permMod12[i] = perm[i] % 12
        }
    }

    /**
     * Generates 2D simplex noise value at the given coordinates.
     *
     * @param xin X coordinate
     * @param yin Y coordinate
     * @return Noise value in the range [-1, 1]
     */
    fun noise(xin: Float, yin: Float): Float {
        val s = (xin + yin) * 0.5f * (sqrt(3.0) - 1).toFloat()
        val i = floor(xin + s).toInt()
        val j = floor(yin + s).toInt()
        val t = (i + j) * (3.0 - sqrt(3.0)).toFloat() / 6f
        val X0 = i - t
        val Y0 = j - t
        val x0 = xin - X0
        val y0 = yin - Y0

        val (i1, j1) = if (x0 > y0) 1 to 0 else 0 to 1

        val x1 = x0 - i1 + 1f / 6f
        val y1 = y0 - j1 + 1f / 6f
        val x2 = x0 - 1f + 2f / 6f
        val y2 = y0 - 1f + 2f / 6f

        val ii = i and 255
        val jj = j and 255

        var n0 = 0f
        var n1 = 0f
        var n2 = 0f

        var t0 = 0.5f - x0 * x0 - y0 * y0
        if (t0 >= 0) {
            val gi0 = permMod12[ii + perm[jj]]
            t0 *= t0
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0)
        }

        var t1 = 0.5f - x1 * x1 - y1 * y1
        if (t1 >= 0) {
            val gi1 = permMod12[ii + i1 + perm[jj + j1]]
            t1 *= t1
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1)
        }

        var t2 = 0.5f - x2 * x2 - y2 * y2
        if (t2 >= 0) {
            val gi2 = permMod12[ii + 1 + perm[jj + 1]]
            t2 *= t2
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2)
        }

        return 70f * (n0 + n1 + n2)
    }

    private fun dot(g: IntArray, x: Float, y: Float): Float {
        return g[0] * x + g[1] * y
    }
}