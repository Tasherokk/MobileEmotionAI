package com.example.emotionsai.ui.hr.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.emotionsai.R
import com.example.emotionsai.ui.hr.analytics.transforms.Kpis

@Composable
fun KpiCards(kpis: Kpis) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        KpiCard("Total", kpis.total, Modifier.weight(1f))
        KpiCard("Top", kpis.topEmotion, Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        KpiCard("With event", kpis.withEvent, Modifier.weight(1f))
        KpiCard("No event", kpis.withoutEvent, Modifier.weight(1f))
    }
}

@Composable
private fun KpiCard(title: String, value: String, modifier: Modifier = Modifier) {
    // твои цвета
    val cardBg = colorResource(R.color.card_primary)
    val cardSurface = colorResource(R.color.card_surface)      // мягкая подложка/бордер
    val primary = colorResource(R.color.primary)
    val textPrimary = colorResource(R.color.text_primary)
    val textSecondary = colorResource(R.color.text_secondary)

    val shape = RoundedCornerShape(18.dp)

    OutlinedCard(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.outlinedCardColors(containerColor = cardBg),
        border = CardDefaults.outlinedCardBorder(enabled = true) // без copy(color=...) чтобы не ругалось
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // заголовок
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = textSecondary
            )

            // значение
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )

            // маленький акцент-бар снизу (минималистичный, но “дорого” выглядит)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = primary,
                trackColor = cardSurface
            )
        }
    }
}
