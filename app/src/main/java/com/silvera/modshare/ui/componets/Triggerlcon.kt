package com.silvera.modshare.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Sürekli renk geçişi yapan (RGB) animasyonlu tetik/crosshair ikonu.
 * Ana ekrandaki eski düz yeşil kutunun yerine kullanılır.
 */
@Composable
fun TriggerIcon(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 72.dp) {
    val infinite = rememberInfiniteTransition(label = "trigger-rgb")

    val hue by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "hue"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val c1 = hslColor(hue)
    val c2 = hslColor((hue + 120f) % 360f)
    val c3 = hslColor((hue + 240f) % 360f)

    Box2(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(c1, c2, c3)))
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = size.toPx() * 0.06f)
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension * 0.30f * pulse

            // Dış halka
            drawCircle(color = Color.White.copy(alpha = 0.9f), radius = radius, center = center, style = stroke)
            // İç nokta
            drawCircle(color = Color.White, radius = radius * 0.12f, center = center)

            // Tetik/crosshair çizgileri
            val armLen = radius * 0.55f
            val gap = radius * 1.15f
            listOf(0.0, 90.0, 180.0, 270.0).forEach { deg ->
                val rad = Math.toRadians(deg)
                val start = Offset(
                    center.x + (gap * cos(rad)).toFloat(),
                    center.y + (gap * sin(rad)).toFloat()
                )
                val end = Offset(
                    center.x + ((gap + armLen) * cos(rad)).toFloat(),
                    center.y + ((gap + armLen) * sin(rad)).toFloat()
                )
                drawLine(color = Color.White, start = start, end = end, strokeWidth = stroke.width)
            }
        }
    }
}

@Composable
private fun Box2(modifier: Modifier, content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
    androidx.compose.foundation.layout.Box(modifier = modifier, content = content)
}

/** Basit HSL(hue, sabit doygunluk/parlaklık) -> RGB Color dönüşümü, ek kütüphane gerektirmez. */
private fun hslColor(hueDegrees: Float, saturation: Float = 0.85f, lightness: Float = 0.55f): Color {
    val h = hueDegrees / 360f
    val c = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
    val x = c * (1f - kotlin.math.abs((h * 6f) % 2f - 1f))
    val m = lightness - c / 2f
    val (r1, g1, b1) = when {
        h < 1f / 6f -> Triple(c, x, 0f)
        h < 2f / 6f -> Triple(x, c, 0f)
        h < 3f / 6f -> Triple(0f, c, x)
        h < 4f / 6f -> Triple(0f, x, c)
        h < 5f / 6f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(r1 + m, g1 + m, b1 + m)
}
    
