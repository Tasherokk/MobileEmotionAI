package com.example.emotionsai.ui.hr.analytics.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.emotionsai.R
import com.example.emotionsai.ui.hr.analytics.theme.EmotionColors
import kotlin.math.roundToInt

@Composable
fun EmotionPieChart(
    data: List<Pair<String, Float>>, // emotion -> 0..1
    modifier: Modifier = Modifier
) {
    val stroke = 18.dp

    val textPrimary = colorResource(R.color.text_primary)
    val textSecondary = colorResource(R.color.text_secondary)

    Column(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val total = data.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1e-6f)
            var startAngle = -90f

            val diameter = size.minDimension
            val topLeft = androidx.compose.ui.geometry.Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            data.forEach { (emotion, value) ->
                val sweep = (value / total) * 360f
                if (sweep > 0.5f) {
                    drawArc(
                        color = EmotionColors.color(emotion),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
                    )
                }
                startAngle += sweep
            }
        }

        Spacer(Modifier.height(12.dp))

        // легенда (цвет эмоции сохраняем, но текст не “дефолтный”)
        data
            .filter { it.second > 0f }
            .sortedByDescending { it.second }
            .take(6)
            .forEach { (emotion, value) ->
                val pct = (value * 100).roundToInt()
                Text(
                    text = "${emotion.lowercase()} — $pct%",
                    color = EmotionColors.color(emotion) // это твоя палитра эмоций
                )
            }

        // если вдруг вообще нет данных — легкая подсказка
        if (data.all { it.second <= 0f }) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "No emotion data for this period",
                color = textSecondary
            )
        }
    }
}
