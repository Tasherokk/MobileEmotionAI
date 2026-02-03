package com.example.emotionsai.ui.hr.analytics.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.emotionsai.R
import com.example.emotionsai.ui.hr.analytics.theme.EmotionColors
import com.example.emotionsai.ui.hr.analytics.transforms.DepartmentEmotionCount

@Composable
fun DepartmentStackedBars(
    items: List<DepartmentEmotionCount>,
    emotionsOrder: List<String>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val textPrimary = colorResource(R.color.text_primary)
    val textSecondary = colorResource(R.color.text_secondary)

    Column(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val paddingLeft = 8.dp.toPx()
            val paddingRight = 8.dp.toPx()
            val paddingTop = 10.dp.toPx()
            val paddingBottom = 24.dp.toPx()

            val w = size.width - paddingLeft - paddingRight
            val h = size.height - paddingTop - paddingBottom

            val barCount = items.size
            val gap = 10.dp.toPx()
            val barWidth = ((w - gap * (barCount - 1)) / barCount).coerceAtLeast(6.dp.toPx())

            items.forEachIndexed { index, dept ->
                val x = paddingLeft + index * (barWidth + gap)
                val total = emotionsOrder.sumOf { e -> dept.emotionCounts[e] ?: 0 }.coerceAtLeast(1)

                var yTop = paddingTop
                emotionsOrder.forEach { emo ->
                    val c = dept.emotionCounts[emo] ?: 0
                    if (c == 0) return@forEach
                    val segH = (c.toFloat() / total) * h

                    drawRect(
                        color = EmotionColors.color(emo),
                        topLeft = androidx.compose.ui.geometry.Offset(x, yTop),
                        size = androidx.compose.ui.geometry.Size(barWidth, segH)
                    )
                    yTop += segH
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // подписи
        items.forEach { dept ->
            val total = emotionsOrder.sumOf { e -> dept.emotionCounts[e] ?: 0 }
            Text(
                text = "${dept.departmentName} — $total",
                // вместо дефолта: твои цвета
                color = if (total > 0) textPrimary else textSecondary
            )
        }
    }
}
