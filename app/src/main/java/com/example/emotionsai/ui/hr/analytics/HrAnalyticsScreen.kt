package com.example.emotionsai.ui.hr.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.emotionsai.R
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

    // hardcoded departments & emotions
    val departments = remember {
        listOf(
            1 to "IT",
            2 to "Sales",
            3 to "HR"
        )
    }
    val emotionsOrder = remember {
        listOf("happy", "neutral", "sad", "angry", "disgust", "fear", "surprise")
    }

    val pie = remember(data) { data.toEmotionPiePercent(emotionsOrder) }
    val matrix = remember(data) { data.toDepartmentEmotionCounts(departments, emotionsOrder) }
    val kpis = remember(data) { data.toKpis() }

    val scrollState = rememberScrollState()
    val shapeXL = RoundedCornerShape(22.dp)

    // ===== твої цвета из colors.xml =====
    val pageBg = colorResource(R.color.bg_surface)
    val cardBg = colorResource(R.color.card_primary)
    val softSurface = colorResource(R.color.card_surface)
    val primary = colorResource(R.color.primary)
    val textPrimary = colorResource(R.color.text_primary)
    val textSecondary = colorResource(R.color.text_secondary)
    val errorColor = colorResource(R.color.error)
    val borderSoft = colorResource(R.color.stroke_soft)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ===== Header =====
            HeaderCard(
                title = "HR Analytics",
                subtitle = buildSubtitle(
                    start = vm.startDate.value,
                    end = vm.endDate.value,
                    count = data.size
                ),
                loading = loading,
                onRefresh = { vm.onFiltersChanged() },
                cardBg = cardBg,
                softSurface = softSurface,
                primary = primary,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            // ===== Filters =====
            SectionTitle(
                title = "Filters",
                icon = Icons.Default.FilterAlt,
                primary = primary,
                textPrimary = textPrimary,
                modifier = Modifier.padding(top = 2.dp)
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = shapeXL,
                colors = CardDefaults.elevatedCardColors(containerColor = cardBg),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AssistLine(
                        text = "Tip: choose date range first. Then narrow by emotions / departments / events.",
                        bg = softSurface,
                        textColor = textSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    FilterBar(
                        startDate = vm.startDate.value,
                        endDate = vm.endDate.value,
                        onStartChange = { vm.startDate.value = it; vm.onFiltersChanged() },
                        onEndChange = { vm.endDate.value = it; vm.onFiltersChanged() },

                        emotionsOrder = emotionsOrder,
                        selectedEmotions = vm.selectedEmotions.value,
                        onToggleEmotion = { emo ->
                            val cur = vm.selectedEmotions.value
                            vm.selectedEmotions.value =
                                if (cur.contains(emo)) cur - emo else cur + emo
                            vm.onFiltersChanged()
                        },

                        departments = departments,
                        selectedDepartments = vm.selectedDepartments.value,
                        onToggleDepartment = { id ->
                            val cur = vm.selectedDepartments.value
                            vm.selectedDepartments.value =
                                if (cur.contains(id)) cur - id else cur + id
                            vm.onFiltersChanged()
                        },

                        events = events,
                        selectedEventId = vm.selectedEventId.value,
                        onSelectEventId = { id ->
                            vm.selectedEventId.value = id
                            vm.onFiltersChanged()
                        },

                        hasEvent = vm.selectedHasEvent.value,
                        onSelectHasEvent = { v ->
                            vm.selectedHasEvent.value = v
                            vm.onFiltersChanged()
                        }
                    )
                }
            }

            // ===== Loading / Error =====
            if (loading) {
                LoadingBanner(
                    text = "Loading analytics…",
                    bg = softSurface,
                    primary = primary,
                    textColor = textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            error?.takeIf { it.isNotBlank() }?.let { msg ->
                ErrorBanner(
                    text = msg,
                    bg = softSurface,
                    errorColor = errorColor,
                    textColor = textPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ===== KPI =====
            SectionTitle(
                title = "Overview",
                icon = Icons.Default.Analytics,
                primary = primary,
                textPrimary = textPrimary
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = shapeXL,
                colors = CardDefaults.outlinedCardColors(containerColor = cardBg),
                border = CardDefaults.outlinedCardBorder()
                    .copy(brush = SolidColor(borderSoft))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = "Key metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    KpiCards(kpis)
                }
            }

            // ===== Charts =====
            SectionTitle(
                title = "Charts",
                icon = Icons.Default.Analytics,
                primary = primary,
                textPrimary = textPrimary
            )

            // Pie
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = shapeXL,
                colors = CardDefaults.outlinedCardColors(containerColor = cardBg),
                border = CardDefaults.outlinedCardBorder()
                    .copy(brush = SolidColor(borderSoft))
            ) {
                Column(Modifier.padding(14.dp)) {
                    ChartHeader(
                        title = "Emotions distribution",
                        subtitle = "Share of each emotion in the selected period",
                        titleColor = textPrimary,
                        subtitleColor = textSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    EmotionPieChart(pie, modifier = Modifier.fillMaxWidth())
                }
            }

            // Stacked bars
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = shapeXL,
                colors = CardDefaults.outlinedCardColors(containerColor = cardBg),
                border = CardDefaults.outlinedCardBorder()
                    .copy(brush = SolidColor(borderSoft))
            ) {
                Column(Modifier.padding(14.dp)) {
                    ChartHeader(
                        title = "Departments mood map",
                        subtitle = "Emotion breakdown per department",
                        titleColor = textPrimary,
                        subtitleColor = textSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    DepartmentStackedBars(
                        items = matrix,
                        emotionsOrder = emotionsOrder,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/* -------------------- UI building blocks -------------------- */

@Composable
private fun HeaderCard(
    title: String,
    subtitle: String,
    loading: Boolean,
    onRefresh: () -> Unit,
    cardBg: androidx.compose.ui.graphics.Color,
    softSurface: androidx.compose.ui.graphics.Color,
    primary: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)

    ElevatedCard(
        modifier = modifier,
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // icon badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(softSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = primary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary
                )
            }

            Spacer(Modifier.width(10.dp))

            FilledTonalIconButton(
                onClick = onRefresh,
                enabled = !loading,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = softSurface,
                    contentColor = primary,
                    disabledContainerColor = softSurface,
                    disabledContentColor = textSecondary
                )
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            }
        }

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = primary,
                trackColor = softSurface
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primary: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary
        )
    }
}

@Composable
private fun ChartHeader(
    title: String,
    subtitle: String,
    titleColor: androidx.compose.ui.graphics.Color,
    subtitleColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = titleColor
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = subtitleColor
        )
    }
}

@Composable
private fun LoadingBanner(
    text: String,
    bg: androidx.compose.ui.graphics.Color,
    primary: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier,
        shape = shape,
        color = bg,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = primary
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

@Composable
private fun ErrorBanner(
    text: String,
    bg: androidx.compose.ui.graphics.Color,
    errorColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier,
        shape = shape,
        color = bg,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = errorColor
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Ошибка: $text",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

@Composable
private fun AssistLine(
    text: String,
    bg: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = modifier,
        shape = shape,
        color = bg,
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}

private fun buildSubtitle(start: String?, end: String?, count: Int): String {
    val s = start?.takeIf { it.isNotBlank() } ?: "—"
    val e = end?.takeIf { it.isNotBlank() } ?: "—"
    return "Period: $s → $e • Records: $count"
}
