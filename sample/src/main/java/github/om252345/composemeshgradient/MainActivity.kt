package github.om252345.composemeshgradient

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.om252345.composemeshgradient.ui.theme.ComposeMeshGradientTheme
import io.github.om252345.composemeshgradient.MeshGradient
import io.github.om252345.composemeshgradient.utils.animateOffset

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeMeshGradientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            Modifier.padding(innerPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            Card(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .sizeIn(maxHeight = 200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .fillMaxWidth()
                            ) {
                                Box {
                                    SimplexNoiseMeshExample(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                    )
                                    Text(
                                        text = "Simplex Noise Gradient",
                                        modifier = Modifier
                                            .padding(16.dp),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                            }
                            Spacer(Modifier.padding(vertical = 8.dp))
                            Spacer(Modifier.padding(vertical = 8.dp))
                            TextButton(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(200.dp, 100.dp),
                                onClick = {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Button Clicked! 1",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }) {
                                Box(Modifier.size(width = 200.dp, height = 100.dp)) {
                                    BackgroundMeshGradient(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(100.dp))
                                    )
                                    Text(
                                        text = "Click Me 1",
                                        style = androidx.compose.ui.text.TextStyle(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        fontSize = 24.sp,
                                        color = Color.White,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                            Spacer(Modifier.padding(vertical = 8.dp))
                            TextButton(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(200.dp, 100.dp),
                                onClick = {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Button Clicked! 2",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }) {
                                Box(Modifier.size(width = 200.dp, height = 100.dp)) {
                                    Button2BackgroundMeshGradient(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(100.dp))
                                    )
                                    Text(
                                        text = "Click Me 2",
                                        style = androidx.compose.ui.text.TextStyle(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        fontSize = 24.sp,
                                        color = Color.White,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }

                        }
                    }

                }
            }
        }
    }
}

@Composable
fun BackgroundMeshGradient(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedMiddleOffset = infiniteTransition.animateOffset(
        initialValue = Offset(0.2f, 0.2f),
        targetValue = Offset(0.8f, 0.8f),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    MeshGradient(
        width = 3,
        height = 3,
        points = arrayOf(
            Offset(0f, 0f), Offset(0.5f, 0f), Offset(1f, 0f),
            Offset(0f, 0.5f), animatedMiddleOffset.value, Offset(1f, 0.5f),
            Offset(0f, 1f), Offset(0.5f, 1f), Offset(1f, 1f)
        ),
        colors = arrayOf(
            Color.Red, Color.Magenta, Color.Blue,
            Color.Red, Color.Magenta, Color.Blue,
            Color.Red, Color.Magenta, Color.Blue
        ),
        modifier = modifier
    )
}


@Composable
fun Button2BackgroundMeshGradient(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedColorBlue = infiniteTransition.animateColor(
        initialValue = Color.Blue, // Starting color
        targetValue = Color.Red,  // Ending color
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ), // Animation duration and easing
            repeatMode = RepeatMode.Reverse // How the animation repeats (e.g., reverses or restarts)
        )
    )
    val animatedColorRed = infiniteTransition.animateColor(
        initialValue = Color.Red, // Starting color
        targetValue = Color.Blue,  // Ending color
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ), // Animation duration and easing
            repeatMode = RepeatMode.Reverse // How the animation repeats (e.g., reverses or restarts)
        )
    )
    MeshGradient(
        width = 2,
        height = 2,
        points = arrayOf(
            Offset(0f, 0f), Offset(1f, 0f),
            Offset(0f, 1f), Offset(1f, 1f),
        ),
        colors = arrayOf(
            animatedColorRed.value, animatedColorBlue.value,
            animatedColorBlue.value, animatedColorRed.value
        ),
        modifier = modifier
    )
}
   