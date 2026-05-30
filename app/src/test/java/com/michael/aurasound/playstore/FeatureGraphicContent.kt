package com.michael.aurasound.playstore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 1024×500 Play Store feature graphic — brand midnight gradient + name + tagline. */
@Composable
fun FeatureGraphicContent() {
    val deepNavy = Color(0xFF080C11)
    val slate = Color(0xFF0F151F)
    val luminousBlue = Color(0xFF9ECAFF)
    val lavender = Color(0xFFD4BBE8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(deepNavy, slate, Color(0xFF004880)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(luminousBlue.copy(alpha = 0.35f), Color.Transparent),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = luminousBlue,
                    modifier = Modifier.size(52.dp),
                )
            }
            Text(
                text = "AuraSound",
                color = Color(0xFFE2E2E6),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Mix calming soundscapes for focus & sleep",
                color = lavender,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
    }
}
