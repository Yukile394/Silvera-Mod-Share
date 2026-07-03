package com.silvera.modshare.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.silvera.modshare.ui.theme.SilveraAccent
import com.silvera.modshare.ui.theme.SilveraBackground
import com.silvera.modshare.ui.theme.SilveraPurple
import kotlin.math.sin

/**
 * Masaüstü/"hacker panel" havası veren, yavaşça hareket eden bulanık gradyan kutulardan
 * oluşan arka plan. content bu arka planın üzerine çizilir.
 */
@Composable
fun AnimatedPcBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "bg")
    val t by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 6.2832f, // 2*PI
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "bg-t"
    )

    Box(modifier = modifier.fillMaxSize().background(SilveraBackground)) {
        // Sağ üstte yavaşça yüzen mor blur kutu
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (240 + 30 * sin(t.toDouble())).dp, y = (-60 + 20 * sin(t.toDouble() * 1.3)).dp)
                .clip(RoundedCornerShape(60.dp))
                .background(SilveraPurple.copy(alpha = 0.22f))
                .blur(70.dp)
        )
        // Sol altta yavaşça yüzen yeşil/aksan blur kutu
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-40 + 25 * sin(t.toDouble() * 0.8 + 1)).dp, y = (560 + 20 * sin(t.toDouble())).dp)
                .clip(RoundedCornerShape(50.dp))
                .background(SilveraAccent.copy(alpha = 0.16f))
                .blur(70.dp)
        )
        // İnce "devre kartı" hissi veren köşe kutucuğu
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = (10 + 15 * sin(t.toDouble() * 1.6)).dp, y = (120 + 15 * sin(t.toDouble() * 1.1)).dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(SilveraPurple.copy(alpha = 0.10f), Color.Transparent)
                    )
                )
                .blur(40.dp)
        )
        content()
    }
}

