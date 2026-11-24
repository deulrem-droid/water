package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WaterFillBlue
import com.example.ui.theme.WaterFillDeepBlue
import com.example.ui.theme.WaterGlassRim
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaterCupIllustration(
  cups: Int,
  goalCups: Int = 8,
  contentDescriptionText: String,
  modifier: Modifier = Modifier,
) {
  val targetFraction = (cups.toFloat() / goalCups.toFloat()).coerceIn(0f, 1f)
  val animatedFill by
    animateFloatAsState(
      targetValue = targetFraction,
      animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
      label = "waterFillLevel",
    )

  val infiniteTransition = rememberInfiniteTransition(label = "waterWave")
  val wavePhase by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = (2f * PI).toFloat(),
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 2200, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "wavePhase",
    )

  Canvas(
    modifier =
      modifier.size(96.dp).semantics { this.contentDescription = contentDescriptionText }
  ) {
    val w = size.width
    val h = size.height

    val topInset = w * 0.14f
    val bottomInset = w * 0.24f
    val glassTop = h * 0.12f
    val glassBottom = h * 0.90f
    val glassHeight = glassBottom - glassTop

    // Trapezoid cup silhouette with gentle rounded bottom
    val cupPath =
      Path().apply {
        moveTo(topInset, glassTop)
        lineTo(w - topInset, glassTop)
        lineTo(w - bottomInset, glassBottom - h * 0.06f)
        quadraticTo(
          w - bottomInset,
          glassBottom,
          w - bottomInset - w * 0.06f,
          glassBottom,
        )
        lineTo(bottomInset + w * 0.06f, glassBottom)
        quadraticTo(bottomInset, glassBottom, bottomInset, glassBottom - h * 0.06f)
        close()
      }

    // Soft glass background
    drawPath(path = cupPath, color = PureWhite)

    // Water fill clipped inside cup shape
    if (animatedFill > 0.01f) {
      clipPath(cupPath) {
        val fillTopY = glassBottom - (glassHeight * 0.88f * animatedFill)
        val waveAmp = if (animatedFill < 0.98f) h * 0.032f else h * 0.014f

        val waterPath =
          Path().apply {
            moveTo(0f, glassBottom + 4f)
            lineTo(0f, fillTopY)
            val steps = 28
            for (i in 0..steps) {
              val x = (w / steps) * i
              val y = fillTopY + sin((x / w) * 2.5f * PI.toFloat() + wavePhase) * waveAmp
              lineTo(x, y)
            }
            lineTo(w, glassBottom + 4f)
            close()
          }

        drawPath(
          path = waterPath,
          brush =
            Brush.verticalGradient(
              colors = listOf(WaterFillBlue, WaterFillDeepBlue),
              startY = fillTopY,
              endY = glassBottom,
            ),
        )

        // Subtle surface highlight on top of water
        drawRoundRect(
          color = Color.White.copy(alpha = 0.25f),
          topLeft = Offset(w * 0.26f, fillTopY + h * 0.05f),
          size = Size(w * 0.10f, (glassBottom - fillTopY) * 0.6f),
          cornerRadius = CornerRadius(12f, 12f),
        )
      }
    }

    // Subtle measurement tick lines on right side of glass
    for (fraction in listOf(0.25f, 0.5f, 0.75f)) {
      val tickY = glassBottom - glassHeight * 0.88f * fraction
      val t = (tickY - glassTop) / glassHeight
      val rightEdgeX = (w - topInset) * (1f - t) + (w - bottomInset) * t
      drawLine(
        color = WaterGlassRim.copy(alpha = 0.35f),
        start = Offset(rightEdgeX - w * 0.14f, tickY),
        end = Offset(rightEdgeX - w * 0.03f, tickY),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }

    // Crisp glass outline
    drawPath(
      path = cupPath,
      color = WaterGlassRim,
      style =
        Stroke(
          width = 3.5.dp.toPx(),
          cap = StrokeCap.Round,
          join = StrokeJoin.Round,
        ),
    )
  }
}
