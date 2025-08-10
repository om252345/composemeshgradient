# MeshGrad: SwiftUI-like Mesh Gradients for Android Jetpack Compose

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.om252345/compose-mesh-gradient.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.om252345%22%20AND%20a:%22compose-mesh-gradient%22)

MeshGrad is a lightweight, powerful Jetpack Compose library that brings beautiful, animated, and static mesh gradients to Android, inspired by the stunning visuals of SwiftUI. Create fluid, vibrant, and interactive backgrounds with a simple and intuitive API.

The library uses OpenGL ES for high-performance rendering, ensuring your gradients are smooth and battery-efficient.

---

## ✨ Demos

Here are some examples of what you can create with MeshGrad:


https://github.com/user-attachments/assets/ac740661-0011-4e2f-b1ae-114b35048d03


https://github.com/user-attachments/assets/1152328c-be17-4e8c-a12b-cc0623888d07





---

## 💡 Showcase & Inspiration

MeshGrad provides the core tools to build a wide variety of dynamic and interactive effects. The `MeshGradientState` allows you to update colors and points on every frame, making it possible to connect the gradient to any external state or user input. Here are some ideas to inspire you:

* **Interactive Backgrounds:** Create gradients that respond to user input by capturing events and updating the `MeshGradientState`.
  * **Touch Interaction:** Make the mesh points ripple away from a user's touch.
  * **Sensor-driven:** Use the device's gyroscope or accelerometer to shift the gradient as the user moves their phone, creating a parallax effect.
* **Data Visualization:** Represent data dynamically by updating the `colors` array based on a data source.
  * **Heatmaps:** Use a mesh to visualize data points, where colors change based on intensity.
  * **Live Weather:** Create a background that reflects the current weather—sunny yellows, cloudy grays, or stormy blues that animate gently.
* **Themed Animations:** Design beautiful, ambient animations that match your app's aesthetic, similar to the `SimplexNoise` example.
  * **"Lava Lamp" Effect:** Slow, blob-like animations that morph between colors.
  * **"Aurora Borealis" Effect:** Shimmering, curtain-like waves of color for a calming background.
* **Dynamic UI Elements:** Integrate mesh gradients directly into your UI components.
  * **Reactive Sliders:** Change the gradient's colors or intensity as a slider's value changes.
  * **Animated Progress Bars:** Fill a progress bar with a flowing gradient animation as it completes.

---

## 🗺️ Roadmap

Here are some potential features and helpers we're considering for future releases to make implementing advanced use cases even easier:

* **Built-in Touch Modifiers:** Helpers to automatically translate touch input into mesh point movements.
* **Additional Interpolation Shaders:** Support for different smoothing algorithms beyond the default Catmull-Rom.
* **Performance Optimizations:** Further investigation into reducing GPU load and improving frame rates for extremely complex animations.

---

## 🚀 Installation

Add the dependency to your module's `build.gradle.kts` or `build.gradle` file. The library is hosted on **Maven Central**.

**build.gradle.kts (Kotlin DSL)**
```kotlin
dependencies {
    implementation("io.github.om252345:composemeshgradient:0.1.0") // Replace with the latest version
}
```

**build.gradle (Groovy DSL)**
```groovy
dependencies {
    implementation 'io.github.om252345:composemeshgradient:0.1.0' // Replace with the latest version
}
```

---

## 🎨 Usage

The core of the library is the `MeshGradient` composable. You define a grid size (`width` and `height`), provide the control `points` for the mesh, and assign `colors` to each point.

### Basic Concepts

* **Grid (`width`, `height`):** Defines the number of control points. A 3x3 grid has 9 points.
* **Points (`Array<Offset>`):** An array of `Offset` values that determine the position of each control point on the canvas. The coordinates are normalized, ranging from `0f` (left/top) to `1f` (right/bottom). The array is laid out in row-major order.
* **Colors (`Array<Color>`):** An array of `Color` values, one for each control point. The colors will be smoothly interpolated across the mesh.

### Static Gradients

#### 2x2 Mesh Gradient (Static)

A simple 2x2 grid is the most basic mesh, creating a beautiful blend between four colors.

```
kotlin
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.om252345.composemeshgradient.MeshGradient

@Composable
fun Static2x2Mesh() {
    MeshGradient(
        width = 2,
        height = 2,
        points = arrayOf(
            Offset(0f, 0f), Offset(1f, 0f), // Top-left, Top-right
            Offset(0f, 1f), Offset(1f, 1f)  // Bottom-left, Bottom-right
        ),
        colors = arrayOf(
            Color(0xFFF09819), Color(0xFFEDDE5D),
            Color(0xFF8A2387), Color(0xFFE94057)
        ),
        modifier = Modifier.fillMaxSize()
    )
}
```

#### 3x3 Mesh Gradient (Static)

By increasing the grid size, you can create more complex and nuanced color interactions.

```
kotlin
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.om252345.composemeshgradient.MeshGradient

@Composable
fun Static3x3Mesh() {
    MeshGradient(
        width = 3,
        height = 3,
        points = arrayOf(
            Offset(0f, 0f), Offset(0.5f, 0f), Offset(1f, 0f),
            Offset(0f, 0.5f), Offset(0.5f, 0.5f), Offset(1f, 0.5f),
            Offset(0f, 1f), Offset(0.5f, 1f), Offset(1f, 1f)
        ),
        colors = arrayOf(
            Color.Red, Color.Yellow, Color.Green,
            Color.Cyan, Color.Blue, Color.Magenta,
            Color.Black, Color.Gray, Color.White
        ),
        modifier = Modifier.fillMaxSize()
    )
}
```

#### 4x4 Mesh Gradient (Static)

A 4x4 grid offers even more control for intricate designs.

```
kotlin
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.om252345.composemeshgradient.MeshGradient

@Composable
fun Static4x4Mesh() {
    MeshGradient(
        width = 4,
        height = 4,
        points = Array(16) { i ->
            val col = i % 4
            val row = i / 4
            Offset(x = col / 3f, y = row / 3f)
        },
        colors = arrayOf(
            Color(0xffF36E21), Color(0xffF0A92A), Color(0xffE7D043), Color(0xffE7E95D),
            Color(0xffDFF168), Color(0xffB1E192), Color(0xff7FD3A9), Color(0xff5EC6B8),
            Color(0xff4AB8C2), Color(0xff4099B9), Color(0xff3B79A8), Color(0xff395B93),
            Color(0xff36447C), Color(0xff332E66), Color(0xff301E4E), Color(0xff2D1137)
        ),
        modifier = Modifier.fillMaxSize()
    )
}
```

---

## 🎬 Animated Gradients

MeshGrad truly shines with animations. You can animate both the colors and the positions of the control points.

### 1. Animating Colors

Animate the colors of the mesh points to create shifting, breathing backgrounds. This is great for button backgrounds or subtle UI elements.

```
kotlin
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.om252345.composemeshgradient.MeshGradient

@Composable
fun AnimatedColorMesh() {
    val infiniteTransition = rememberInfiniteTransition(label = "color-transition")
    
    val animatedColor1 = infiniteTransition.animateColor(
        initialValue = Color(0xFF8A2387),
        targetValue = Color(0xFFF09819),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "color1"
    )
    
    val animatedColor2 = infiniteTransition.animateColor(
        initialValue = Color(0xFFEDDE5D),
        targetValue = Color(0xFFE94057),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "color2"
    )

    MeshGradient(
        width = 2,
        height = 2,
        points = arrayOf(
            Offset(0f, 0f), Offset(1f, 0f),
            Offset(0f, 1f), Offset(1f, 1f)
        ),
        colors = arrayOf(
            animatedColor1.value, animatedColor2.value,
            animatedColor2.value, animatedColor1.value
        ),
        modifier = Modifier.fillMaxSize()
    )
}
```

### 2. Animating a Control Point

Animate the `points` array to make the gradient warp and distort dynamically.

```
kotlin
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.om252345.composemeshgradient.MeshGradient
import io.github.om252345.composemeshgradient.utils.animateOffset

@Composable
fun AnimatedPointMesh() {
    val infiniteTransition = rememberInfiniteTransition(label = "point-transition")
    
    // Animate the middle point of a 3x3 grid
    val animatedMiddlePoint = infiniteTransition.animateOffset(
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
            Offset(0f, 0f),    Offset(0.5f, 0f),    Offset(1f, 0f),
            Offset(0f, 0.5f),   animatedMiddlePoint.value, Offset(1f, 0.5f), // Animated point
            Offset(0f, 1f),    Offset(0.5f, 1f),    Offset(1f, 1f)
        ),
        colors = arrayOf(
            Color.Red, Color.Magenta, Color.Blue,
            Color.Yellow, Color.White, Color.Green,
            Color.Cyan, Color.DarkGray, Color.Black
        ),
        modifier = Modifier.fillMaxSize()
    )
}
```

### 3. Advanced: Simplex Noise Animation (Fluid Gradient)

For a truly dynamic and organic feel, you can use `SimplexNoise` to continuously animate all inner control points. This creates a mesmerizing, fluid effect. The library provides `rememberMeshGradientState` and `snapAllPoints` for high-performance, per-frame updates.

```
kotlin
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.om252345.composemeshgradient.MeshGradient
import io.github.om252345.composemeshgradient.rememberMeshGradientState
import io.github.om252345.composemeshgradient.utils.SimplexNoise
import kotlinx.coroutines.launch

@Composable
fun SimplexNoiseMeshExample(modifier: Modifier) {
    val width = 4
    val height = 4

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
    
    val meshState = rememberMeshGradientState(points = initialPoints)

    LaunchedEffect(Unit) {
        var time = 0f
        val basePoints = initialPoints.toList()
        var currentPoints = initialPoints.toMutableList()
        val targetPoints = initialPoints.toMutableList()
        var lastFrameTime = 0L

        while (true) {
            withFrameNanos { frameTime ->
                if (lastFrameTime == 0L) lastFrameTime = frameTime
                val deltaTime = (frameTime - lastFrameTime) / 1_000_000_000.0f
                lastFrameTime = frameTime
                time += deltaTime * 0.3f // Animation speed

                for (i in targetPoints.indices) {
                    val col = i % width
                    val row = i / width
                    val isBorder = row == 0 || row == height - 1 || col == 0 || col == width - 1

                    if (!isBorder) {
                        val bp = basePoints[i]
                        val noiseX = SimplexNoise.noise(bp.x * 1.5f, time + i) * 0.2f
                        val noiseY = SimplexNoise.noise(bp.y * 1.5f, time + i + 100f) * 0.2f
                        targetPoints[i] = Offset(bp.x + noiseX, bp.y + noiseY)
                    }
                }

                for (i in currentPoints.indices) {
                    val lerped = lerp(currentPoints[i], targetPoints[i], (8f * deltaTime))
                    currentPoints[i] = lerped
                }

                launch {
                    meshState.snapAllPoints(currentPoints.toList())
                }
            }
        }
    }

    MeshGradient(
        modifier = modifier,
        width = width,
        height = height,
        points = meshState.points.toTypedArray(),
        colors = colors.toTypedArray()
    )
}

// Linear interpolation helper
private fun lerp(start: Offset, stop: Offset, fraction: Float): Offset {
    return start + (stop - start) * fraction.coerceIn(0f, 1f)
}
```

---

## 📜 License

```
Copyright 2025 Omkar Deshmukh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
