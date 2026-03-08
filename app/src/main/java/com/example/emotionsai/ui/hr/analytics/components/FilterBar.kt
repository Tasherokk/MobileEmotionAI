package com.example.emotionsai.ui.hr.analytics.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emotionsai.R
import com.example.emotionsai.data.remote.HrEventDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBar(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    emotionsOrder: List<String>,
    selectedEmotions: List<String>,
    onToggleEmotion: (String) -> Unit,
    departments: List<Pair<Int, String>>,
    selectedDepartments: List<Int>,
    onToggleDepartment: (Int) -> Unit,
    events: List<HrEventDto>,
    selectedEventId: Int?,
    onSelectEventId: (Int?) -> Unit,
    hasEvent: Boolean?,
    onSelectHasEvent: (Boolean?) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val primary = colorResource(R.color.primary)
    val textPrimary = colorResource(R.color.on_surface)
    val textSecondary = colorResource(R.color.on_surface_variant)
    val surface = colorResource(R.color.surface)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }
            
            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = textSecondary
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FilterSection(title = "Date Range") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CompactDatePicker(
                            label = "From",
                            value = startDate,
                            onDateSelected = onStartChange,
                            modifier = Modifier.weight(1f)
                        )
                        CompactDatePicker(
                            label = "To",
                            value = endDate,
                            onDateSelected = onEndChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                FilterSection(title = "Event Scope") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SegmentedChip(
                            label = "All",
                            isSelected = hasEvent == null,
                            onClick = { onSelectHasEvent(null) }
                        )
                        SegmentedChip(
                            label = "With Event",
                            isSelected = hasEvent == true,
                            onClick = { onSelectHasEvent(true) }
                        )
                        SegmentedChip(
                            label = "No Event",
                            isSelected = hasEvent == false,
                            onClick = { onSelectHasEvent(false) }
                        )
                    }
                }

                FilterSection(title = "Categories") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Emotions", style = MaterialTheme.typography.labelSmall, color = textSecondary)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            emotionsOrder.forEach { emotion ->
                                FilterChip(
                                    selected = selectedEmotions.contains(emotion),
                                    onClick = { onToggleEmotion(emotion) },
                                    label = { Text(emotion.replaceFirstChar { it.uppercase() }) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primary.copy(alpha = 0.15f),
                                        selectedLabelColor = primary
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        
                        Text("Departments", style = MaterialTheme.typography.labelSmall, color = textSecondary)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            departments.forEach { (id, name) ->
                                FilterChip(
                                    selected = selectedDepartments.contains(id),
                                    onClick = { onToggleDepartment(id) },
                                    label = { Text(name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primary.copy(alpha = 0.15f),
                                        selectedLabelColor = primary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.primary),
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactDatePicker(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = value.toDateMillisOrNull()
    )

    val primaryColor = colorResource(R.color.primary)
    val surfaceColor = colorResource(R.color.surface)
    val onSurfaceColor = colorResource(R.color.on_surface)

    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { showPicker = true },
        color = colorResource(R.color.background),
        border = if (value.isNotEmpty()) null else FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = colorResource(R.color.stroke_soft))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (value.isNotEmpty()) primaryColor else colorResource(R.color.on_surface_variant)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = value.ifEmpty { label },
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isNotEmpty()) onSurfaceColor else colorResource(R.color.on_surface_variant)
            )
        }
    }

    if (showPicker) {
        val customColorScheme = MaterialTheme.colorScheme.copy(
            surface = surfaceColor,
            onSurface = onSurfaceColor,
            primary = primaryColor,
            onPrimary = Color.White
        )

        MaterialTheme(colorScheme = customColorScheme) {
            DatePickerDialog(
                onDismissRequest = { showPicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { onDateSelected(it.toIsoDateString()) }
                            showPicker = false
                        }
                    ) { Text("Apply", color = primaryColor, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showPicker = false }
                    ) { Text("Cancel", color = onSurfaceColor.copy(alpha = 0.6f)) }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = surfaceColor,
                        titleContentColor = onSurfaceColor,
                        headlineContentColor = onSurfaceColor,
                        selectedDayContainerColor = primaryColor,
                        selectedDayContentColor = Color.White,
                        todayContentColor = primaryColor,
                        todayDateBorderColor = primaryColor,
                        yearContentColor = onSurfaceColor,
                        selectedYearContainerColor = primaryColor,
                        selectedYearContentColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun SegmentedChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colorResource(R.color.primary),
            selectedLabelColor = Color.White,
            containerColor = colorResource(R.color.background)
        ),
        border = null,
        shape = RoundedCornerShape(12.dp)
    )
}

private fun String.toDateMillisOrNull(): Long? = try {
    if (isBlank()) null else {
        val ld = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
        ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
} catch (_: Throwable) {
    null
}

private fun Long.toIsoDateString(): String {
    val ld = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    return ld.format(DateTimeFormatter.ISO_LOCAL_DATE)
}
