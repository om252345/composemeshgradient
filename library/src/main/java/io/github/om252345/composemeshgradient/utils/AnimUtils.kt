package io.github.om252345.composemeshgradient.utils

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.geometry.Offset

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
