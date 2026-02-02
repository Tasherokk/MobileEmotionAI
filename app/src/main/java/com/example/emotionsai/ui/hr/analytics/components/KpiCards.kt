package com.example.emotionsai.ui.hr.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}
