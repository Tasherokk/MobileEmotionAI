package com.example.emotionsai.ui.hr.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.emotionsai.R
import com.example.emotionsai.data.remote.HrEventDto

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
    val selectedBg = colorResource(R.color.selected_button)

    // единые цвета для OutlinedTextField
    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primary,
        unfocusedBorderColor = stroke,
        focusedLabelColor = primary,
        unfocusedLabelColor = textSecondary,
        cursorColor = primary,
        focusedTextColor = cardBg,
        unfocusedTextColor = textPrimary,
        focusedPlaceholderColor = hint,
        unfocusedPlaceholderColor = hint,
        focusedContainerColor = primary,
        unfocusedContainerColor = cardBg,

    )

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // dates
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = startDate,
                onValueChange = onStartChange,
                label = { Text("Start date", color = textSecondary) },
                placeholder = { Text("yyyy-MM-dd", color = hint) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = tfColors
            )
            OutlinedTextField(
                value = endDate,
                onValueChange = onEndChange,
                label = { Text("End date", color = textSecondary) },
                placeholder = { Text("yyyy-MM-dd", color = hint) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = tfColors
            )
        }

        // has_event toggle
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

        // event dropdown
        EventDropdown(
            events = events,
            selectedEventId = selectedEventId,
            onSelect = onSelectEventId,
            primary = primary,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            hint = hint,
            stroke = stroke,
            container = cardBg,

        )

        // emotions chips
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

        // departments chips
        MultiChoiceChips(
            title = "Departments",
            options = departments.map { it.second },
            selected = selectedDepartments.mapNotNull { id ->
                departments.firstOrNull { it.first == id }?.second
            },
            onToggleLabel = { label ->
                val id = departments.firstOrNull { it.second == label }?.first ?: return@MultiChoiceChips
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
        focusedBorderColor = primary,
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

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Event", color = textSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true,
            colors = tfColors
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            Modifier.background(container)
        ) {
            DropdownMenuItem(
                text = { Text("All events", color = textPrimary) },
                onClick = { onSelect(null); expanded = false },
                Modifier.background(container)
            )
            events.forEach { e ->
                DropdownMenuItem(
                    text = { Text(e.title, color = textPrimary) },
                    onClick = { onSelect(e.id); expanded = false },
                    Modifier.background(container)
                )
            }
        }
    }
}

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
        style = MaterialTheme.typography.labelLarge,
        color = textSecondary
    )

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = primary.copy(alpha = 0.14f),
        selectedLabelColor = textPrimary,
        selectedLeadingIconColor = primary,
        containerColor = chipBg,
        labelColor = textPrimary
    )
    val chipBorder = FilterChipDefaults.filterChipBorder(
        enabled = true,
        selected = false,
        borderColor = stroke,
        selectedBorderColor = primary
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
                colors = chipColors,
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
        style = MaterialTheme.typography.labelLarge,
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
                    selectedContainerColor = primary.copy(alpha = 0.14f),
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
