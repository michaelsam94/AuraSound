package com.example.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PulsingOrb(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    masterVolume: Float = 1.0f
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val colorAccent = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val colorAccentSecondary = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    val colorAccentTertiary = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.06f)

    Canvas(modifier = modifier) {
        val baseRadius = size.minDimension * 0.35f
        val radiusMultiplier = if (isPlaying) 1.0f + (masterVolume * 0.15f) else 1.0f
        val animatedRadius = baseRadius * radiusMultiplier * pulseScale

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colorAccent, Color.Transparent),
                radius = animatedRadius * 1.8f
            ),
            radius = animatedRadius * 1.8f
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colorAccentSecondary, Color.Transparent),
                radius = animatedRadius * 1.3f
            ),
            radius = animatedRadius * 1.3f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colorAccentTertiary, Color.Transparent),
                radius = animatedRadius
            ),
            radius = animatedRadius
        )
    }
}

@Composable
fun TimerArc(
    remainingSeconds: Long,
    totalSeconds: Long,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)
    
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorTrack = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            
            // Draw background track
            drawArc(
                color = colorTrack,
                startAngle = -220f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Draw active progress
            drawArc(
                color = colorPrimary,
                startAngle = -220f,
                sweepAngle = 260f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeString,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "minutes remaining",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}
