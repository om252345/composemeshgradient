package github.om252345.composemeshgradient

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.om252345.composemeshgradient.MeshGradient
import io.github.om252345.composemeshgradient.rememberMeshGradientState
import kotlinx.coroutines.delay

@Composable
fun AnimatedMeshExample(modifier: Modifier) {
    val colors = remember {
        // A 3x3 grid of colors
        arrayOf(
            Color(0xFFFFC1CC), Color(0xFFFFE4E1), Color(0xFFFFF0F5), Color(0xFFFFC1CC),
            Color(0xFFFFEFD5), Color(0xFFFFDAB9), Color(0xFFFFDEAD), Color(0xFFFFE4B5),
            Color(0xFFB0E0E6), Color(0xFFAFEEEE), Color(0xFFE0FFFF), Color(0xFFADD8E6),
            Color.Black, Color(0xFFD8BFD8), Color(0xFFEEE8AA), Color(0xFFF5DEB3)
        )
    }

    val initialPoints = remember {
        // A standard 3x3 grid of points
        arrayOf(
            Offset(0f, 0f), Offset(0.33f, 0f), Offset(0.66f, 0f), Offset(1f, 0f),
            Offset(0f, 0.33f), Offset(0.33f, 0.33f), Offset(0.66f, 0.33f), Offset(1f, 0.33f),
            Offset(0f, 0.66f), Offset(0.33f, 0.66f), Offset(0.66f, 0.66f), Offset(1f, 0.66f),
            Offset(0f, 1f), Offset(0.33f, 1f), Offset(0.66f, 1f), Offset(1f, 1f)
        )
    }

    // 1. Create and remember the state that will drive the animation.
    val meshState = rememberMeshGradientState(points = initialPoints)

    // 2. Use a LaunchedEffect to run a suspend animation coroutine.
    LaunchedEffect(Unit) {
        // This will loop forever, animating the center point (index 4)
        while (true) {
            meshState.animatePoint(
                index = 5, // Center point of 3x3 grid
                targetOffset = Offset(.33f, 0.8f),
                animationSpec = tween(1500, easing = LinearEasing)
            )
            meshState.animatePoint(
                index = 6, // Center point of 3x3 grid
                targetOffset = Offset(.33f, 0.8f),
                animationSpec = tween(1500, easing = LinearEasing)
            )
            delay(200)
            meshState.animatePoint(
                index = 5,
                targetOffset = Offset(.33f, 0.15f),
                animationSpec = tween(1500, easing = LinearEasing)
            )
            meshState.animatePoint(
                index = 6, // Center point of 3x3 grid
                targetOffset = Offset(.33f, 0.15f),
                animationSpec = tween(1500, easing = LinearEasing)
            )
            delay(200)
        }
    }

    // 3. Pass the animated points from the state to the composable.
    MeshGradient(
        modifier = modifier,
        width = 4,
        height = 4,
        points = meshState.points.toTypedArray(),
        colors = colors
    )
}