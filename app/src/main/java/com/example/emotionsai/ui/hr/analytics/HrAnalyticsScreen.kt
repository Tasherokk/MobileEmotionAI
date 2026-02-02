package com.example.emotionsai.ui.hr.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.emotionsai.ui.hr.analytics.charts.DepartmentStackedBars
import com.example.emotionsai.ui.hr.analytics.charts.EmotionPieChart
import com.example.emotionsai.ui.hr.analytics.components.FilterBar
import com.example.emotionsai.ui.hr.analytics.components.KpiCards
import com.example.emotionsai.ui.hr.analytics.transforms.toDepartmentEmotionCounts
import com.example.emotionsai.ui.hr.analytics.transforms.toEmotionPiePercent
import com.example.emotionsai.ui.hr.analytics.transforms.toKpis

@Composable
fun HrAnalyticsScreen(vm: HrAnalyticsViewModel) {

    val data by vm.data.observeAsState(emptyList())
    val loading by vm.loading.observeAsState(false)
    val error by vm.error.observeAsState()
    val events by vm.events.observeAsState(emptyList())

    // hardcoded departments & emotions (как ты просила)
    val departments = remember {
        listOf(
            1 to "IT",
            2 to "Sales",
            3 to "HR"
        )
    }
    val emotionsOrder = remember {
        listOf("happy", "neutral", "sad", "angry", "disgust", "fear", "surprised")
    }

    val pie = remember(data) { data.toEmotionPiePercent(emotionsOrder) }
    val matrix = remember(data) { data.toDepartmentEmotionCounts(departments, emotionsOrder) }
    val kpis = remember(data) { data.toKpis() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("HR Analytics", style = MaterialTheme.typography.headlineSmall)

        FilterBar(
            startDate = vm.startDate.value,
            endDate = vm.endDate.value,
            onStartChange = { vm.startDate.value = it; vm.onFiltersChanged() },
            onEndChange = { vm.endDate.value = it; vm.onFiltersChanged() },

            emotionsOrder = emotionsOrder,
            selectedEmotions = vm.selectedEmotions.value,
            onToggleEmotion = { emo ->
                val cur = vm.selectedEmotions.value
                vm.selectedEmotions.value = if (cur.contains(emo)) cur - emo else cur + emo
                vm.onFiltersChanged()
            },

            departments = departments,
            selectedDepartments = vm.selectedDepartments.value,
            onToggleDepartment = { id ->
                val cur = vm.selectedDepartments.value
                vm.selectedDepartments.value = if (cur.contains(id)) cur - id else cur + id
                vm.onFiltersChanged()
            },

            events = events,
            selectedEventId = vm.selectedEventId.value,
            onSelectEventId = { id -> vm.selectedEventId.value = id; vm.onFiltersChanged() },

            hasEvent = vm.selectedHasEvent.value,
            onSelectHasEvent = { v -> vm.selectedHasEvent.value = v; vm.onFiltersChanged() }
        )

        if (loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        error?.takeIf { it.isNotBlank() }?.let {
            Text("Ошибка: $it", color = MaterialTheme.colorScheme.error)
        }

        KpiCards(kpis)

        Card {
            Column(Modifier.padding(12.dp)) {
                Text("Emotions distribution", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                EmotionPieChart(pie, modifier = Modifier.fillMaxWidth())
            }
        }

        Card {
            Column(Modifier.padding(12.dp)) {
                Text("Departments mood map", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                DepartmentStackedBars(
                    items = matrix,
                    emotionsOrder = emotionsOrder,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
