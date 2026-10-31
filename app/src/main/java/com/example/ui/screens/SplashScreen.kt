package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onAnimationFinished: () -> Unit
) {
  val scale = remember { Animatable(0.7f) }
  val alpha = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    alpha.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
  }

  LaunchedEffect(Unit) {
    scale.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
    )
    delay(1200)
    onAnimationFinished()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFFE8F5E9), // Lembut hijau muda atas
            Color(0xFFF4FAF4),
            Color.White        // Putih bersih bawah
          )
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .padding(32.dp)
        .scale(scale.value)
        .alpha(alpha.value)
    ) {
      // Logo Aplikasi Catat Bisnisku
      Box(
        modifier = Modifier
          .size(160.dp)
          .shadow(12.dp, RoundedCornerShape(32.dp))
          .clip(RoundedCornerShape(32.dp))
          .background(Color.White),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.app_logo),
          contentDescription = "Logo Catat Bisnisku",
          modifier = Modifier.size(150.dp)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "Catat Bisnisku",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          fontSize = 28.sp,
          color = PrimaryGreen
        )
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Catat Usaha, Pantau Untungnya.",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Medium,
          color = PrimaryGreenLight,
          fontSize = 15.sp
        )
      )

      Spacer(modifier = Modifier.height(18.dp))

      // By Rafline Dev credit as requested
      Text(
        text = "by Rafline Dev",
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF6B7280),
          letterSpacing = 1.sp
        )
      )
    }

    // Tagline version at bottom
    Text(
      text = "Aplikasi Pencatatan Usaha Offline-First",
      style = MaterialTheme.typography.bodySmall,
      color = Color(0xFF9CA3AF),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 32.dp)
        .alpha(alpha.value)
    )
  }
}
