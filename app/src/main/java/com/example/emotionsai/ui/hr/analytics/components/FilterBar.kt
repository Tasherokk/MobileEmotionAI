package com.example.emotionsai.ui.hr.analytics.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.emotionsai.R
import com.example.emotionsai.data.remote.HrEventDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    // ===== твои цвета =====
    val primary = colorResource(R.color.primary)
    val textPrimary = colorResource(R.color.text_primary)
    val textSecondary = colorResource(R.color.text_secondary)
    val hint = colorResource(R.color.text_hint)
    val stroke = colorResource(R.color.stroke_soft)
    val chipBg = colorResource(R.color.card_surface)
    val cardBg = colorResource(R.color.card_primary)

    // ЕДИНЫЕ цвета TextField + добавлены disabled-* (важно! потому что мы делаем enabled=false)
    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primary,
        unfocusedBorderColor = stroke,
        focusedLabelColor = primary,
        unfocusedLabelColor = textSecondary,
        cursorColor = primary,
        focusedTextColor = textPrimary,
        unfocusedTextColor = textPrimary,
        focusedPlaceholderColor = hint,
        unfocusedPlaceholderColor = hint,
        focusedContainerColor = cardBg,
        unfocusedContainerColor = cardBg,

        // ключевое: чтобы disabled выглядел как обычный (мы отключаем поле, кликаем по overlay)
        disabledBorderColor = stroke,
        disabledLabelColor = textSecondary,
        disabledTextColor = textPrimary,
        disabledPlaceholderColor = hint,
        disabledContainerColor = cardBg
    )

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // ===== dates =====
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DatePickerField(
                label = "Start date",
                value = startDate,
                onValueChange = onStartChange,
                modifier = Modifier.weight(1f),
                primary = primary,
                textSecondary = textSecondary,
                tfColors = tfColors
            )

            DatePickerField(
                label = "End date",
                value = endDate,
                onValueChange = onEndChange,
                modifier = Modifier.weight(1f),
                primary = primary,
                textSecondary = textSecondary,
                tfColors = tfColors
            )
        }

        // ===== has_event toggle =====
        SingleChoiceSegment(
            title = "Has event",
            options = listOf(
                "All" to null,
                "Only with" to true,
                "Only without" to false
            ),
            selected = hasEvent,
            onSelect = onSelectHasEvent,
            primary = primary,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            chipBg = chipBg,
            stroke = stroke
        )

        // ===== event dropdown =====
        EventDropdown(
            events = events,
            selectedEventId = selectedEventId,
            onSelect = onSelectEventId,
            primary = primary,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            hint = hint,
            stroke = stroke,
            container = cardBg
        )

        // ===== emotions chips =====
        MultiChoiceChips(
            title = "Emotions",
            options = emotionsOrder,
            selected = selectedEmotions,
            onToggle = onToggleEmotion,
            primary = primary,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            chipBg = chipBg,
            stroke = stroke
        )

        // ===== departments chips =====
        MultiChoiceChips(
            title = "Departments",
            options = departments.map { it.second },
            selected = selectedDepartments.mapNotNull { id ->
                departments.firstOrNull { it.first == id }?.second
            },
            onToggleLabel = { label ->
                val id =
                    departments.firstOrNull { it.second == label }?.first ?: return@MultiChoiceChips
                onToggleDepartment(id)
            },
            primary = primary,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            chipBg = chipBg,
            stroke = stroke
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    primary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    tfColors: androidx.compose.material3.TextFieldColors
) {
    var open by remember { mutableStateOf(false) }

    Box(modifier = modifier) {

        // 1) Поле делаем disabled, чтобы оно НЕ перехватывало клики
        OutlinedTextField(
            value = value,
            onValueChange = {},
            enabled = false,
            readOnly = true,
            singleLine = true,
            label = { Text(label, color = textSecondary) },
            placeholder = { Text("yyyy-MM-dd") },
            modifier = Modifier.fillMaxWidth(),
            colors = tfColors
        )

        // 2) Прозрачный кликабельный слой поверх — гарантированно ловит тапы
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { open = true }
        )
    }

    if (open) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = value.toDateMillisOrNull()
        )

        // твои цвета из ресурсов
        val primary = colorResource(com.example.emotionsai.R.color.primary)
        val surface = colorResource(com.example.emotionsai.R.color.card_primary)
        val surfaceVariant = colorResource(com.example.emotionsai.R.color.card_surface)
        val textPrimary = colorResource(com.example.emotionsai.R.color.text_primary)
        val textSecondary = colorResource(com.example.emotionsai.R.color.text_secondary)
        val error = colorResource(com.example.emotionsai.R.color.error)

        // создаём кастомную схему (минимум нужных цветов)
        val customScheme = MaterialTheme.colorScheme.copy(
            primary = primary,
            onPrimary = Color.White,              // можно из ресурсов, если хочешь
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = textSecondary,
            error = error
        )

        MaterialTheme(colorScheme = customScheme) {
            DatePickerDialog(
                onDismissRequest = { open = false },
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = textPrimary,
                ),
                confirmButton = {
                    TextButton(
                        onClick = {
                            pickerState.selectedDateMillis?.let { onValueChange(it.toIsoDateString()) }
                            open = false

                        }
                    ) { Text("OK") }
                },

                dismissButton = {
                    TextButton(
                        onClick = { open = false }
                    ) { Text("Cancel") }
                }
            ) {
                // 2) Плюс точечная настройка цветов самого DatePicker
                DatePicker(
                    state = pickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = surface,
                        titleContentColor = textPrimary,
                        headlineContentColor = textPrimary,

                        weekdayContentColor = textSecondary,
                        subheadContentColor = textSecondary,

                        // дни
                        dayContentColor = textPrimary,
                        disabledDayContentColor = textSecondary.copy(alpha = 0.35f),

                        // выбранный день
                        selectedDayContainerColor = primary,
                        selectedDayContentColor = Color.White,

                        // сегодня
                        todayDateBorderColor = primary,
                        todayContentColor = primary,

                        // год (если включён выбор года)
                        yearContentColor = textPrimary,
                        selectedYearContainerColor = primary,
                        selectedYearContentColor = Color.White,
                        currentYearContentColor = primary,

                        )
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDropdown(
    events: List<HrEventDto>,
    selectedEventId: Int?,
    onSelect: (Int?) -> Unit,
    primary: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    hint: androidx.compose.ui.graphics.Color,
    stroke: androidx.compose.ui.graphics.Color,
    container: androidx.compose.ui.graphics.Color
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = events.firstOrNull { it.id == selectedEventId }?.title ?: "All events"

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primary.copy(alpha = 0.65f),
        unfocusedBorderColor = stroke,
        focusedLabelColor = primary,
        unfocusedLabelColor = textSecondary,
        cursorColor = primary,
        focusedTextColor = textPrimary,
        unfocusedTextColor = textPrimary,
        focusedPlaceholderColor = hint,
        unfocusedPlaceholderColor = hint,
        focusedContainerColor = container,
        unfocusedContainerColor = container
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Event", color = textSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = tfColors
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(container)
        ) {
            DropdownMenuItem(
                text = { Text("All events", color = textPrimary) },
                onClick = { onSelect(null); expanded = false }
            )
            events.forEach { e ->
                DropdownMenuItem(
                    text = { Text(e.title, color = textPrimary) },
                    onClick = { onSelect(e.id); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MultiChoiceChips(
    title: String,
    options: List<String>,
    selected: List<String>,
    onToggle: ((String) -> Unit)? = null,
    onToggleLabel: ((String) -> Unit)? = null,
    primary: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    chipBg: androidx.compose.ui.graphics.Color,
    stroke: androidx.compose.ui.graphics.Color
) {
    Text(
        title,
        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
        color = textSecondary
    )

    val colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = primary.copy(alpha = 0.3f),
        selectedLabelColor = textPrimary,
        containerColor = chipBg,
        labelColor = textPrimary
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { opt ->
            val isSelected = selected.contains(opt)

            FilterChip(
                selected = isSelected,
                onClick = {
                    onToggle?.invoke(opt)
                    onToggleLabel?.invoke(opt)
                },
                label = { Text(opt, color = textPrimary) },
                colors = colors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = stroke,
                    selectedBorderColor = primary
                )
            )
        }
    }
}

@Composable
private fun <T> SingleChoiceSegment(
    title: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
    primary: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    chipBg: androidx.compose.ui.graphics.Color,
    stroke: androidx.compose.ui.graphics.Color
) {
    Text(
        title,
        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
        color = textSecondary
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (label, value) ->
            val isSelected = selected == value

            FilterChip(
                selected = isSelected,
                onClick = { onSelect(value) },
                label = { Text(label, color = textPrimary) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primary.copy(alpha = 0.3f),
                    selectedLabelColor = textPrimary,
                    containerColor = chipBg,
                    labelColor = textPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = stroke,
                    selectedBorderColor = primary
                )
            )
        }
    }
}

/** yyyy-MM-dd -> millis (00:00 local) */
private fun String.toDateMillisOrNull(): Long? = try {
    if (isBlank()) null else {
        val ld = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
        ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
} catch (_: Throwable) {
    null
}

/** millis -> yyyy-MM-dd */
private fun Long.toIsoDateString(): String {
    val ld = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    return ld.format(DateTimeFormatter.ISO_LOCAL_DATE)
}
