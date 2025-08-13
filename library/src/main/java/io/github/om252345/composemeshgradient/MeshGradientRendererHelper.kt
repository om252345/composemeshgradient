package io.github.om252345.composemeshgradient

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal class MeshGradientRendererHelper(
    private val gridWidth: Int,          // Number of mesh vertices horizontally (e.g. 3 for 3x3)
    private val gridHeight: Int,         // Number of mesh vertices vertically  (e.g. 3 for 3x3)
    private val globalSubdivisions: Int = 64   // Number of SUBDIVISIONS PER PATCH ROW/COL
) {
    private lateinit var indexBuffer: ShortBuffer
    private lateinit var uvBuffer: FloatBuffer
    private lateinit var posArray: FloatArray
    private lateinit var colorArray: FloatArray
    private var indexCount: Int = 0
    private var program: Int = 0

    private var aGridUv = -1
    private var uGridWidth = -1
    private var uGridHeight = -1
    private var uBezierPositions = -1
    private var uBezierColors = -1

    private fun cacheLocations() {
        aGridUv = GLES20.glGetAttribLocation(program, "a_Grid_UV")
        uGridWidth = GLES20.glGetUniformLocation(program, "u_GridWidth")
        uGridHeight = GLES20.glGetUniformLocation(program, "u_GridHeight")
        uBezierPositions = GLES20.glGetUniformLocation(program, "u_BezierPositions")
        uBezierColors = GLES20.glGetUniformLocation(program, "u_BezierColors")
    }

    fun initBuffers() {
        // UVs
        val totalX = (gridWidth - 1) * globalSubdivisions + 1
        val totalY = (gridHeight - 1) * globalSubdivisions + 1
        val uvList = FloatArray(totalX * totalY * 2)
        var uvi = 0
        for (y in 0 until totalY) {
            val v = y.toFloat() / (totalY - 1).toFloat()
            for (x in 0 until totalX) {
                val u = x.toFloat() / (totalX - 1).toFloat()
                uvList[uvi++] = u
                uvList[uvi++] = v
            }
        }
        uvBuffer = ByteBuffer.allocateDirect(uvList.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().put(uvList)
        uvBuffer.position(0)

        // Step 3: pre-sized indices
        indexCount = (totalX - 1) * (totalY - 1) * 6
        val indices = ShortArray(indexCount)
        var k = 0
        for (y in 0 until totalY - 1) {
            val row = y * totalX
            val next = (y + 1) * totalX
            for (x in 0 until totalX - 1) {
                val tl = (row + x).toShort()
                val tr = (row + x + 1).toShort()
                val bl = (next + x).toShort()
                val br = (next + x + 1).toShort()

                // With 1f - p.y flip, swap winding to make CCW in NDC
                // Triangle 1: TL, TR, BL  (instead of TL, BL, TR)
                indices[k++] = tl
                indices[k++] = tr
                indices[k++] = bl

                // Triangle 2: TR, BR, BL (instead of TR, BL, BR)
                indices[k++] = tr
                indices[k++] = br
                indices[k++] = bl
            }
        }
        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).order(ByteOrder.nativeOrder())
            .asShortBuffer().put(indices)
        indexBuffer.position(0)

        // Create shader program (your existing compile logic)
        program = compileShaders()

        // Step 1: cache attribute/uniform locations once
        aGridUv = GLES20.glGetAttribLocation(program, "a_Grid_UV")
        uGridWidth = GLES20.glGetUniformLocation(program, "u_GridWidth")
        uGridHeight = GLES20.glGetUniformLocation(program, "u_GridHeight")
        uBezierPositions = GLES20.glGetUniformLocation(program, "u_BezierPositions")
        uBezierColors = GLES20.glGetUniformLocation(program, "u_BezierColors")

        // Allocate primitive uniform arrays (size fixed to number of control points)
        val controlCount = gridWidth * gridHeight
        posArray = FloatArray(controlCount * 2)
        colorArray = FloatArray(controlCount * 4)
    }

    /** Draw with primitive arrays to avoid boxing */
    fun draw(points: FloatArray, colors: FloatArray) {
        GLES20.glUseProgram(program)

        GLES20.glEnableVertexAttribArray(aGridUv)
        GLES20.glVertexAttribPointer(aGridUv, 2, GLES20.GL_FLOAT, false, 0, uvBuffer)

        GLES20.glUniform1i(uGridWidth, gridWidth)
        GLES20.glUniform1i(uGridHeight, gridHeight)

        // Copy incoming points/colors into stable arrays
        System.arraycopy(points, 0, posArray, 0, points.size)
        System.arraycopy(colors, 0, colorArray, 0, colors.size)

        GLES20.glUniform2fv(uBezierPositions, gridWidth * gridHeight, posArray, 0)
        GLES20.glUniform4fv(uBezierColors, gridWidth * gridHeight, colorArray, 0)

        indexBuffer.position(0)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indexCount,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        GLES20.glDisableVertexAttribArray(aGridUv)
    }

    private fun compileShaders(): Int {
        val vertexShader = """
            #version 300 es

// IN: Static vertex attributes (created once)
in vec2 a_Grid_UV; // The (u,v) coordinate of this vertex within the logical grid [0,1]

// UNIFORMS: Data that changes per frame
uniform vec2 u_BezierPositions[${gridWidth * gridHeight}]; // 4x4 = 16 control points
uniform vec4 u_BezierColors[${gridWidth * gridHeight}];    // 4x4 = 16 control colors
uniform int u_GridWidth;            // Will be 4
uniform int u_GridHeight;           // Will be 4

// OUT: Data passed to the fragment shader
out vec4 v_Color;

// GLSL version of your Catmull-Rom interpolation for a single float component
float catmullRom(float p0, float p1, float p2, float p3, float t) {
    float t2 = t * t;
    float t3 = t2 * t;
    return 0.5 * (
        (2.0 * p1) +
        (-p0 + p2) * t +
        (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2 +
        (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3
    );
}

// Helper to get a point from the 1D uniform array using 2D coordinates
vec2 getPosition(int x, int y) {
    int clampedX = clamp(x, 0, u_GridWidth - 1);
    int clampedY = clamp(y, 0, u_GridHeight - 1);
    return u_BezierPositions[clampedY * u_GridWidth + clampedX];
}

// Helper to get a color from the 1D uniform array using 2D coordinates
vec4 getColor(int x, int y) {
    int clampedX = clamp(x, 0, u_GridWidth - 1);
    int clampedY = clamp(y, 0, u_GridHeight - 1);
    return u_BezierColors[clampedY * u_GridWidth + clampedX];
}

void main() {
    // 1. Determine which grid cell this vertex is in
    float cellX = floor(a_Grid_UV.x * float(u_GridWidth - 1));
    float cellY = floor(a_Grid_UV.y * float(u_GridHeight - 1));

    // 2. Determine the local (u,v) within that cell [0,1]
    float localU = fract(a_Grid_UV.x * float(u_GridWidth - 1));
    float localV = fract(a_Grid_UV.y * float(u_GridHeight - 1));

    // 3. Get the 4x4 patch of control points surrounding the current cell
    int cx = int(cellX);
    int cy = int(cellY);

    // 4. Bicubic Interpolation: Pass 1 (Horizontal)
    vec2 interp_row[4];
    vec4 interp_color_row[4];
    for (int i = 0; i < 4; i++) {
        vec2 p0 = getPosition(cx - 1, cy - 1 + i);
        vec2 p1 = getPosition(cx,     cy - 1 + i);
        vec2 p2 = getPosition(cx + 1, cy - 1 + i);
        vec2 p3 = getPosition(cx + 2, cy - 1 + i);

        vec4 c0 = getColor(cx - 1, cy - 1 + i);
        vec4 c1 = getColor(cx,     cy - 1 + i);
        vec4 c2 = getColor(cx + 1, cy - 1 + i);
        vec4 c3 = getColor(cx + 2, cy - 1 + i);

        interp_row[i].x = catmullRom(p0.x, p1.x, p2.x, p3.x, localU);
        interp_row[i].y = catmullRom(p0.y, p1.y, p2.y, p3.y, localU);
        interp_color_row[i].r = catmullRom(c0.r, c1.r, c2.r, c3.r, localU);
        interp_color_row[i].g = catmullRom(c0.g, c1.g, c2.g, c3.g, localU);
        interp_color_row[i].b = catmullRom(c0.b, c1.b, c2.b, c3.b, localU);
        interp_color_row[i].a = catmullRom(c0.a, c1.a, c2.a, c3.a, localU);
    }

    // 5. Bicubic Interpolation: Pass 2 (Vertical)
    vec2 finalPos;
    finalPos.x = catmullRom(interp_row[0].x, interp_row[1].x, interp_row[2].x, interp_row[3].x, localV);
    finalPos.y = catmullRom(interp_row[0].y, interp_row[1].y, interp_row[2].y, interp_row[3].y, localV);

    vec4 finalColor;
    // --- THIS BLOCK IS CORRECTED ---
    finalColor.r = catmullRom(interp_color_row[0].r, interp_color_row[1].r, interp_color_row[2].r, interp_color_row[3].r, localV);
    finalColor.g = catmullRom(interp_color_row[0].g, interp_color_row[1].g, interp_color_row[2].g, interp_color_row[3].g, localV);
    finalColor.b = catmullRom(interp_color_row[0].b, interp_color_row[1].b, interp_color_row[2].b, interp_color_row[3].b, localV);
    finalColor.a = catmullRom(interp_color_row[0].a, interp_color_row[1].a, interp_color_row[2].a, interp_color_row[3].a, localV);

    v_Color = finalColor;

    // 6. Convert from normalized [0,1] space to OpenGL's clip space [-1,1]
    gl_Position = vec4(finalPos * 2.0 - 1.0, 0.0, 1.0);
} """.trimIndent()

        val fragmentShader = """
#version 300 es
precision mediump float;
in vec4 v_Color;
out vec4 fragColor;
void main() {
    fragColor = v_Color;
}
        """.trimIndent()

        val vs = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)

        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vs)
            GLES20.glAttachShader(it, fs)
            GLES20.glLinkProgram(it)
        }
    }

    private fun loadShader(type: Int, code: String): Int {
        return GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, code)
            GLES20.glCompileShader(it)
        }
    }
}