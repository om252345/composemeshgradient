package io.github.om252345.composemeshgradient.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.random.Random

/**
 * A data class to hold the generated mesh points and colors.
 */
data class MeshData(
    val points: Array<Offset>,
    val colors: Array<Color>
)

/**
 * Generates a grid of points for a mesh with customizable randomness applied only to inner points.
 * Boundary points are kept static to ensure the mesh fits its container perfectly.
 *
 * @param width The number of points horizontally.
 * @param height The number of points vertically.
 * @param randomness The amount of random offset to apply to each inner point. 0f is a perfect grid,
 * higher values create more distortion. A good range is 0f to 0.2f.
 * @return An array of [Offset] points for the mesh.
 */
fun generateMeshPoints(width: Int, height: Int, randomness: Float = 0.1f): Array<Offset> {
    require(randomness in 0f..1f) { "Randomness must be between 0.0 and 1.0" }

    return Array(width * height) { i ->
        val col = i % width
        val row = i / height

        val isBorder = row == 0 || row == height - 1 || col == 0 || col == width - 1

        val pointX = col.toFloat() / (width - 1).toFloat()
        val pointY = row.toFloat() / (height - 1).toFloat()

        if (isBorder) {
            // Border points are not randomized
            Offset(pointX, pointY)
        } else {
            // Inner points can be randomized
            val randomX = (Random.nextFloat() * 2f - 1f) * randomness
            val randomY = (Random.nextFloat() * 2f - 1f) * randomness
            Offset(
                x = (pointX + randomX).coerceIn(0f, 1f),
                y = (pointY + randomY).coerceIn(0f, 1f)
            )
        }
    }
}
/**
 * Generates a full color array for a mesh from a list of 1 or 2 base colors.
 *
 * @param baseColors A list containing one or two [Color] values.
 * @param width The number of points horizontally.
 * @param height The number of points vertically.
 * @param colorVariance A value from 0.0 to 1.0 that introduces random variations
 * to the hue and lightness of each color, creating a more textured gradient.
 * @return An array of [Color] values for the mesh.
 */
fun generateMeshColors(
    baseColors: List<Color>,
    width: Int,
    height: Int,
    colorVariance: Float = 0.0f
): Array<Color> {
    require(baseColors.isNotEmpty() && baseColors.size <= 2) { "baseColors list must contain 1 or 2 colors." }
    require(colorVariance in 0f..1f) { "colorVariance must be between 0.0 and 1.0" }


    if (baseColors.size == 1) {
        // If only one color, fill the mesh with it.
        return Array(width * height) { baseColors[0] }
    }

    val startColor = baseColors[0]
    val endColor = baseColors[1]

    return Array(width * height) { i ->
        val row = i / width
        // Interpolate based on the row number to create a vertical gradient
        val fraction = row.toFloat() / (height - 1).toFloat()
        val interpolatedColor = lerp(startColor, endColor, fraction)

        if (colorVariance > 0) {
            applyColorVariance(interpolatedColor, colorVariance)
        } else {
            interpolatedColor
        }
    }
}

/**
 * A composable that remembers mesh data and automatically selects the correct color scheme
 * based on the system's light or dark theme.
 *
 * @param lightThemeColors The list of 1 or 2 base colors for the light theme.
 * @param darkThemeColors The list of 1 or 2 base colors for the dark theme.
 * @param width The width of the mesh (e.g., 2, 3, or 4).
 * @param height The height of the mesh (e.g., 2, 3, or 4).
 * @param pointRandomness The amount of random distortion to apply to the mesh points.
 * @param colorVariance The amount of random variation to apply to the mesh colors.
 * @return A [MeshData] object containing the points and colors for the current theme.
 */
@Composable
fun rememberThemedMeshData(
    lightThemeColors: List<Color>,
    darkThemeColors: List<Color>,
    width: Int,
    height: Int,
    pointRandomness: Float = 0.1f,
    colorVariance: Float = 0.0f
): MeshData {
    val isDark = isSystemInDarkTheme()

    return remember(isDark, width, height, pointRandomness, colorVariance, lightThemeColors, darkThemeColors) {
        val points = generateMeshPoints(width, height, pointRandomness)
        val colors = generateMeshColors(
            baseColors = if (isDark) darkThemeColors else lightThemeColors,
            width = width,
            height = height,
            colorVariance = colorVariance
        )
        MeshData(points, colors)
    }
}



/**
 * Applies random variance to a color's HSL values.
 */
private fun applyColorVariance(color: Color, variance: Float): Color {
    val hsl = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsl)

    // Randomly adjust Hue and Lightness
    val hueShift = (Random.nextFloat() * 2f - 1f) * 10f * variance // +/- 10 degrees
    val lightnessShift = (Random.nextFloat() * 2f - 1f) * 0.2f * variance // +/- 20%

    hsl[0] = (hsl[0] + hueShift).mod(360f)
    hsl[2] = (hsl[2] + lightnessShift).coerceIn(0f, 1f)

    return Color(android.graphics.Color.HSVToColor(hsl))
}
